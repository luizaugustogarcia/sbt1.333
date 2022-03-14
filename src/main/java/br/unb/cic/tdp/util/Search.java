package br.unb.cic.tdp.util;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.unsafe.*;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.collections.impl.factory.primitive.LongLongMaps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static br.unb.cic.tdp.proof.ProofGenerator.*;
import static br.unb.cic.tdp.unsafe.UnsafeLongArray.*;
import static br.unb.cic.tdp.util.Sorter.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Search extends RecursiveAction {
    final Configuration configuration;
    final String outputDir;
    final UnsafeListOfCycles spi;
    final UnsafeByteArray pi;
    final Stack stack;
    final Move rootMove;
    final ForkJoinPool forkJoinPool;
    boolean[] hasSorting;

    public Search(final Configuration configuration, final String outputDir, final UnsafeListOfCycles spi,
                  final UnsafeByteArray pi, final Stack stack, final Move rootMove, final ForkJoinPool forkJoinPool,
                  final boolean[] hasSorting) {
        this.configuration = configuration;
        this.outputDir = outputDir;
        this.spi = spi;
        this.pi = pi;
        this.stack = stack;
        this.rootMove = rootMove;
        this.forkJoinPool = forkJoinPool;
        this.hasSorting = hasSorting;
    }

    @SneakyThrows({IOException.class, ExecutionException.class})
    @Override
    protected void compute() {
        final var stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println("compute " + this);

        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        final var parity = new UnsafeBooleanArray(configuration.getSpi().getMaxSymbol() + 1);
        final var spiIndex = new UnsafeLongArray(configuration.getSpi().getMaxSymbol() + 1);

        var newPi = pi;

        for (int i = 0; i < spi.len(); i++) {
            final var cycleAddress = spi.at(i);
            byte len = cycleLen(cycleAddress);
            if (len == 1) {
                var temp = newPi;
                newPi = newPi.removeElement(cycleAt(cycleAddress, 0));
                free(temp.getAddress());
            }
            for (int j = 0; j < len; j++) {
                final var s = cycleAt(cycleAddress, j);
                spiIndex.setLong(s, cycleAddress);
                parity.set(s, (len & 1) == 1);
            }
        }

        removeTrivialCycles(spi);

        // if passed through three zeros moves, COMPUTE DIRECTLY
        if (rootMove.numberOfZeroMovesUntilTop() > 3) {
            final var sorting = search(spi, parity, spiIndex, newPi, stack, rootMove);

            if (!sorting.isEmpty()) {
                hasSorting[0] = true;

                forkJoinPool.shutdownNow();

                System.out.println("Sorted: " + configuration.getSpi() + ", sorting: " + sorting.toList().stream().map(Arrays::toString).collect(joining(",")) + "\n");
                try (final var out = new FileWriter(outputDir + "/comb/" + configuration.getCanonical().getSpi() + ".html")) {
                    renderSorting(configuration.getCanonical(),
                            configuration.getCanonical().translatedSorting(configuration,
                                    sorting.toList().stream().map(Cycle::create).collect(toList())), out);
                }
            }
        } else {
            // else, FORK
            if (rootMove.mu == 0) {
                final var key = canonicalSignature(spi, newPi, spiIndex);
                System.out.println(key);
                final var paths = UNSUCCESSFUL_VISITED_CONFIGS.get(key, () -> new HashSet(1));
                synchronized (paths) {
                    if (paths.isEmpty()) {
                        MISSES.incrementAndGet();
                        paths.add(rootMove.pathToRoot());
                    } else {
                        if (paths.contains(rootMove.pathToRoot())) {
                            HITS.incrementAndGet();
                            return;
                        } else {
                            paths.add(rootMove.pathToRoot());
                        }
                    }
                }

                fork0Moves(parity, spiIndex, newPi);
            } else {
                fork2Moves(parity, spiIndex, newPi);
            }
        }

        free(spiIndex.getAddress());
        free(parity.getAddress());
        free(newPi.getAddress());

        for (int i = 0; i < spi.len(); i++) {
            free(spi.at(i));
        }
        free(spi.getElementDataAddress());

        if (pi.getAddress() != newPi.getAddress()) {
            free(pi.getAddress());
        }

        free(stack.getContentAddress());

        stopWatch.stop();
        System.out.println("end compute " + stopWatch.getTime());
    }

    private void fork2Moves(final UnsafeBooleanArray parity,
                            final UnsafeLongArray spiIndex,
                            final UnsafeByteArray pi) {
        System.out.println("fork2 oriented - " + parity);
        System.out.println("fork2 oriented - " + spi);
        System.out.println("fork2 oriented - " + spiIndex);

        // ===========================
        // ===== ORIENTED CYCLES =====
        // ===========================
        final var piInverseIndex = getPiInverseIndex(pi.getAddress(), pi.len(), (byte) spiIndex.size());

        final var orientedCycles = orientedCycles(spi, piInverseIndex);

        for (int l = 0; l < orientedCycles.len(); l++) {
            final var cycleAddress = orientedCycles.at(l);

            final var before = parity.getBool(cycleAt(cycleAddress, 0)) ? 1 : 0;

            for (var i = 0; i < cycleLen(cycleAddress) - 2; i++) {
                for (var j = i + 1; j < cycleLen(cycleAddress) - 1; j++) {
                    final var ab_k = j - i;

                    if (before == 1 && (ab_k & 1) == 0) {
                        continue;
                    }

                    for (var k = j + 1; k < cycleLen(cycleAddress); k++) {
                        final var bc_k = k - j;

                        if (before == 1 && (bc_k & 1) == 0) {
                            continue;
                        }

                        final var ca_k = (cycleLen(cycleAddress) - k) + i;

                        byte a = cycleAt(cycleAddress, i), b = cycleAt(cycleAddress, j), c = cycleAt(cycleAddress, k);

                        var after = ab_k & 1;
                        after += bc_k & 1;
                        after += ca_k & 1;

                        // check if it's applicable
                        if (after - before == 2 && areSymbolsInCyclicOrder(piInverseIndex, a, c, b)) {
                            final var alignedCycleAddress = startingBy(cycleAddress, a);
                            final var aCycle = create(ca_k);
                            cycleSet(aCycle, 0, a);
                            for (int m = 0; m < ca_k - 1; m++) {
                                cycleSet(aCycle, m + 1, cycleAt(alignedCycleAddress, ab_k + bc_k + 1 + m));
                            }

                            final var bCycle = create(ab_k);
                            cycleSet(bCycle, 0, b);
                            for (int m = 0; m < ab_k - 1; m++) {
                                cycleSet(bCycle, m + 1, cycleAt(alignedCycleAddress, 1 + m));
                            }

                            final var cCycle = create(bc_k);
                            cycleSet(cCycle, 0, c);
                            for (int m = 0; m < bc_k - 1; m++) {
                                cycleSet(cCycle, m + 1, cycleAt(alignedCycleAddress, ab_k + 1 + m));
                            }

                            free(alignedCycleAddress);

                            stack.push(a, b, c);

                            // == APPLY THE MOVE ===
                            spi.remove(cycleAddress);
                            var numberOfTrivialCycles = 0;
                            if (cycleLen(aCycle) > 1) spi.add(aCycle);
                            else numberOfTrivialCycles++;
                            if (cycleLen(bCycle) > 1) spi.add(bCycle);
                            else numberOfTrivialCycles++;
                            if (cycleLen(cCycle) > 1) spi.add(cCycle);
                            else numberOfTrivialCycles++;
                            update(spiIndex, parity, aCycle, bCycle, cCycle);
                            // =======================

                            for (final var nextMove : rootMove.children) {
                                final var newPi = applyTransposition(pi, a, b, c, pi.len() - numberOfTrivialCycles, spiIndex);
                                new Search(configuration, outputDir, spi.clone(), newPi, stack.clone(), nextMove, forkJoinPool, hasSorting).fork();
                            }

                            stack.pop();

                            // ==== ROLLBACK ====
                            if (cycleLen(aCycle) > 1) spi.remove(aCycle);
                            if (cycleLen(bCycle) > 1) spi.remove(bCycle);
                            if (cycleLen(cCycle) > 1) spi.remove(cCycle);

                            free(aCycle);
                            free(bCycle);
                            free(cCycle);

                            spi.add(cycleAddress);
                            update(spiIndex, parity, cycleAddress);
                            // ====================
                        }
                    }
                }
            }
        }

        free(piInverseIndex);
        free(orientedCycles);

        // ======================
        // ===== ODD CYCLES =====
        // ======================

        update(spiIndex, parity, spi);

        System.out.println("fork2 odd - " + parity);
        System.out.println("fork2 odd - " + spi);
        System.out.println("fork2 odd - " + spiIndex);


        for (int i = 0; i < pi.len() - 2; i++) {
            if (parity.getBool(pi.getByte(i))) continue;
            for (int j = i + 1; j < pi.len() - 1; j++) {
                if (parity.getBool(pi.getByte(j))) continue;
                for (int k = j + 1; k < pi.len(); k++) {
                    if (parity.getBool(pi.getByte(k))) continue;

                    byte a = pi.getByte(i), b = pi.getByte(j), c = pi.getByte(k);

                    // if it's the same cycle, skip it
                    if (spiIndex.getLong(a) == spiIndex.getLong(b) && spiIndex.getLong(b) == spiIndex.getLong(c))
                        continue;

                    final var is_2Move = spiIndex.getLong(a) != spiIndex.getLong(b) &&
                            spiIndex.getLong(b) != spiIndex.getLong(c) &&
                            spiIndex.getLong(a) != spiIndex.getLong(c);
                    if (is_2Move)
                        continue;

                    final Triplet<UnsafeListOfCycles, UnsafeListOfCycles, Integer> triplet = simulate0MoveTwoCycles(spiIndex, a, b, c);

                    if (triplet.third != 2)
                        continue;

                    stack.push(a, b, c);

                    // == APPLY THE MOVE ===
                    spi.removeAll(triplet.first);
                    var numberOfTrivialCycles = 0;

                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);

                        if (cycleLen(cycleAddress) > 1) {
                            spi.add(cycleAddress);
                        } else {
                            numberOfTrivialCycles++;
                        }
                    }

                    Sorter.update(spiIndex, parity, triplet.second);
                    // ==============================

                    for (final var nextMove : rootMove.children) {
                        final var newPi = applyTransposition(pi, a, b, c, pi.len() - numberOfTrivialCycles, spiIndex);
                        new Search(configuration, outputDir, spi.clone(), newPi, stack.clone(), nextMove, forkJoinPool, hasSorting).fork();
                    }

                    // ==== ROLLBACK ====
                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);
                        if (cycleLen(cycleAddress) > 1)
                            spi.remove(cycleAddress);
                        free(cycleAddress);
                    }
                    spi.addAll(triplet.first);
                    Sorter.update(spiIndex, parity, triplet.first);
                    // ==============================

                    free(triplet.first.getElementDataAddress());
                    free(triplet.second.getElementDataAddress());

                    stack.pop();
                }
            }
        }
    }

    private void fork0Moves(final UnsafeBooleanArray parity,
                            final UnsafeLongArray spiIndex,
                            final UnsafeByteArray pi) {
        final var piLen = pi.len();

        final var cycleIndexes = TheUnsafe.get().allocateMemory(piLen * 8);
        UnsafeLongArray.fill(cycleIndexes, piLen, (byte) 0);

        final var canonicalSignatures = new HashSet<String>();

        for (int i = 0; i < piLen - 2; i++) {
            for (int j = (i + 1); j < piLen - 1; j++) {
                for (int k = (j + 1); k < piLen; k++) {
                    byte a = pi.getByte(i), b = pi.getByte(j), c = pi.getByte(k);

                    final var is_2Move = spiIndex.getLong(a) != spiIndex.getLong(b) &&
                            spiIndex.getLong(b) != spiIndex.getLong(c) &&
                            spiIndex.getLong(a) != spiIndex.getLong(c);
                    if (is_2Move)
                        continue;

                    final Triplet<UnsafeListOfCycles, UnsafeListOfCycles, Integer> triplet;
                    // if it's the same cycle
                    if (spiIndex.getLong(a) == spiIndex.getLong(b) && spiIndex.getLong(b) == spiIndex.getLong(c)) {
                        final var cycleAddress = spiIndex.getLong(a);

                        if (getLong(cycleIndexes, a) == 0) {
                            final var index = cycleIndex(cycleAddress);
                            setLong(cycleIndexes, a, index);
                            setLong(cycleIndexes, b, index);
                            setLong(cycleIndexes, c, index);
                        }

                        final var indexAddress = getLong(cycleIndexes, a);

                        if (areSymbolsInCyclicOrderCycleIndex(indexAddress, a, b, c)) {
                            final var before = cycleLen(cycleAddress) & 1;

                            final var ab_k = getK(indexAddress, cycleAddress, a, b);
                            var after = ab_k & 1;
                            final var bc_k = getK(indexAddress, cycleAddress, b, c);
                            after += bc_k & 1;
                            final var ca_k = getK(indexAddress, cycleAddress, c, a);
                            after += ca_k & 1;

                            if (after - before == 2) {
                                // skip, it's a 2-move
                                continue;
                            }

                            final long alignedCycle = startingBy(cycleAddress, a);

                            after = 0;
                            final var aCycle = create(ca_k);
                            cycleSet(aCycle, 0, a);
                            cyclecopy(alignedCycle, ab_k + bc_k + 1, aCycle, 1, ca_k - 1);
                            after += ca_k & 1;

                            final var bCycle = create(ab_k);
                            cycleSet(bCycle, 0, b);
                            cyclecopy(alignedCycle, 1, bCycle, 1, ab_k - 1);
                            after += ab_k & 1;

                            final var cCycle = create(bc_k);
                            cycleSet(cCycle, 0, c);
                            cyclecopy(alignedCycle, ab_k + 1, cCycle, 1, bc_k - 1);
                            after += bc_k & 1;

                            TheUnsafe.get().freeMemory(alignedCycle);

                            final var second = new UnsafeListOfCycles(3);
                            second.add(aCycle);
                            second.add(bCycle);
                            second.add(cCycle);

                            triplet = new Triplet<>(UnsafeListOfCycles.singleton(cycleAddress), second, after - before);
                        } else {
                            triplet = simulate0MoveSameCycle(spiIndex, a, b, c);
                        }
                    } else {
                        triplet = simulate0MoveTwoCycles(spiIndex, a, b, c);
                    }

                    if (triplet.third != 0)
                        continue;

                    stack.push(a, b, c);

                    // == APPLY THE MOVE ===
                    var numberOfTrivialCycles = 0;
                    spi.removeAll(triplet.first);

                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);

                        if (cycleLen(cycleAddress) > 1) {
                            spi.add(cycleAddress);
                        } else {
                            numberOfTrivialCycles++;
                        }
                    }
                    Sorter.update(spiIndex, parity, triplet.second);
                    // ==============================

                    final var newPi = applyTransposition(pi, a, b, c, piLen - numberOfTrivialCycles, spiIndex);

                    final var canonicalSignature = canonicalSignature(spi, newPi, spiIndex);
                    if (!canonicalSignatures.contains(canonicalSignature)) {
                        for (final var nextMove : rootMove.children) {
                            new Search(configuration, outputDir, spi.clone(), newPi, stack.clone(), nextMove, forkJoinPool, hasSorting).fork();
                        }
                        canonicalSignatures.add(canonicalSignature);
                    }

                    // ==== ROLLBACK ====
                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);
                        if (cycleLen(cycleAddress) > 1)
                            spi.remove(cycleAddress);
                        free(cycleAddress);
                    }
                    spi.addAll(triplet.first);
                    Sorter.update(spiIndex, parity, triplet.first);
                    // ==============================

                    free(triplet.first.getElementDataAddress());
                    free(triplet.second.getElementDataAddress());

                    stack.pop();
                }
            }
        }

        final var freedIndexes = LongLongMaps.mutable.empty();

        for (int i = 0; i < piLen; i++) {
            long indexAddress = getLong(cycleIndexes, i);
            if (!freedIndexes.containsKey(indexAddress)) {
                free(indexAddress);
                freedIndexes.put(indexAddress, 0);
            }
        }

        free(cycleIndexes);
    }
}