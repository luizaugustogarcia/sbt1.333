package br.unb.cic.tdp.util;

import br.unb.cic.tdp.Application;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.unsafe.*;
import cern.colt.list.LongArrayList;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static br.unb.cic.tdp.proof.ProofGenerator.*;
import static br.unb.cic.tdp.unsafe.UnsafeByteArray.getByte;
import static br.unb.cic.tdp.unsafe.UnsafeByteArray.setByte;
import static br.unb.cic.tdp.unsafe.UnsafeFloatArray.getFloat;
import static br.unb.cic.tdp.unsafe.UnsafeFloatArray.setFloat;
import static br.unb.cic.tdp.unsafe.UnsafeListOfCycles.EMPTY_LIST;
import static br.unb.cic.tdp.unsafe.UnsafeLongArray.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Sorter {
    public static Cache<String, Set<String>> UNSUCCESSFUL_VISITED_CONFIGS;

    public static final AtomicLong HITS = new AtomicLong();
    public static final AtomicLong MISSES = new AtomicLong();

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        final var cacheSize = (int) ((Runtime.getRuntime().maxMemory() * 0.85) / 429);
        logger.info("Cache size:" + cacheSize);

        UNSUCCESSFUL_VISITED_CONFIGS = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

        Stream.of(
                new Configuration("(0 16 14)(1 35 15)(2 6 4)(3 7 5)(8 12 10)(9 13 11)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 34 20)(1 5 3)(2 6 4)(7 11 9)(8 12 10)(13 35 33)(14 18 16)(15 19 17)(21 25 23)(22 26 24)(27 31 29)(28 32 30)"),
                new Configuration("(0 4 2)(1 35 3)(5 9 7)(6 10 8)(11 15 13)(12 28 14)(16 20 18)(17 21 19)(22 26 24)(23 27 25)(29 33 31)(30 34 32)"),
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(18 22 20)(19 23 21)(24 34 26)(25 35 33)(27 31 29)(28 32 30)"),
                new Configuration("(0 34 26)(1 35 33)(2 6 4)(3 7 5)(8 12 10)(9 13 11)(14 18 16)(15 19 17)(20 24 22)(21 25 23)(27 31 29)(28 32 30)"),
                new Configuration("(0 34 2)(1 35 33)(3 7 5)(4 8 6)(9 19 17)(10 14 12)(11 15 13)(16 26 18)(20 24 22)(21 25 23)(27 31 29)(28 32 30)"),
                new Configuration("(0 34 32)(1 29 27)(2 30 28)(3 7 5)(4 8 6)(9 13 11)(10 14 12)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(31 35 33)"),
                new Configuration("(0 16 14)(1 5 3)(2 6 4)(7 11 9)(8 12 10)(13 35 15)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 34 2)(1 35 33)(3 7 5)(4 8 6)(9 13 11)(10 14 12)(15 25 17)(16 32 18)(19 23 21)(20 24 22)(26 30 28)(27 31 29)"),
                new Configuration("(0 34 32)(1 35 33)(2 6 4)(3 7 5)(8 12 10)(9 13 11)(14 18 16)(15 19 17)(20 24 22)(21 25 23)(26 30 28)(27 31 29)"),
                new Configuration("(0 34 32)(1 29 27)(2 6 4)(3 7 5)(8 12 10)(9 13 11)(14 30 28)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(31 35 33)")
        ).forEach(conf -> sort(conf, "C:/Users/Luiz/Temp/sbt1.333proof", _16_12_SEQS));

        System.out.println("7 interleaving (16,12)");
        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)"),
                "C:/Users/Luiz/Temp/sbt1.333proof", _16_12_SEQS);

        System.out.println("7 interleaving (19,14)");
        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)"),
                "C:/Users/Luiz/Temp/sbt1.333proof", _19_14_SEQS);

        System.out.println("8 interleaving");

        System.out.println("(16,12)");
        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)"),
                args[3], _16_12_SEQS, Integer.parseInt(args[2]));

        System.out.println("(19,14)");
        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)"),
                args[3], _19_14_SEQS, Integer.parseInt(args[2]));

        System.out.println("(20,15)");
        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)"),
                args[3], _20_15_SEQS, Integer.parseInt(args[2]));
    }

    public static void sort(final Configuration configuration,
                            final String outputDir,
                            final Move rootMove) {
        sort(configuration, outputDir, rootMove, Runtime.getRuntime().availableProcessors());
    }

    @SneakyThrows
    public static void sort(final Configuration configuration,
                            final String outputDir,
                            final Move rootMove,
                            final int numberOfProcessors) {
        HITS.set(0);
        MISSES.set(0);
        UNSUCCESSFUL_VISITED_CONFIGS.invalidateAll();

        final var canonical = configuration.getCanonical();

        final var sortingFile = new File(outputDir + "/comb/" + canonical.getSpi() + ".html");
        if (sortingFile.exists()) {
            System.out.println("Skipping " + configuration.getSpi());
            return;
        }

        System.out.println("Sorting " + configuration.getSpi());

        final var hasSorting = new boolean[1];
        final var forkJoinPool = new ForkJoinPool(numberOfProcessors);
        final var task = new Search(configuration, outputDir,
                toListOfCycle(configuration.getSpi(), configuration.getPi()), new UnsafeByteArray(configuration.getPi().getSymbols()),
                new Stack(rootMove.getHeight()), rootMove, forkJoinPool, hasSorting);
        forkJoinPool.submit(task);
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        if (!hasSorting[0])
            System.out.println("Didn't find sorting for " + configuration.getSpi() + "\n");
    }

    static class Search extends RecursiveAction {
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
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            final var parity = new UnsafeBooleanArray(pi.len());
            final var spiIndex = new UnsafeLongArray(pi.len());

            for (int i = 0; i < spi.len(); i++) {
                final var cycleAddress = spi.at(i);
                byte len = len(cycleAddress);
                for (int j = 0; j < len; j++) {
                    final var s = at(cycleAddress, i);
                    spiIndex.setLong(s, cycleAddress);
                    parity.set(s, (len & 1) == 1);
                }
            }

            removeTrivialCycles(spi);

            // if passed through three zeros moves, COMPUTE DIRECTLY
            if (rootMove.numberOfZeroMovesUntilTop() > 3) {
                final var sorting = search(spi, parity, spiIndex, pi, stack, rootMove);

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

                freeResources();
            } else {
                // else, FORK
                if (rootMove.mu == 0) {
                    final var key = canonicalSignature(spi, pi, spiIndex);
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

                    fork0Moves(spi, parity, spiIndex, pi, stack);
                } else {
                    fork2Moves(spi, parity, spiIndex, pi, stack);
                }
            }
        }

        private void fork2Moves(final UnsafeListOfCycles spi,
                                final UnsafeBooleanArray parity,
                                final UnsafeLongArray spiIndex,
                                final UnsafeByteArray pi,
                                final Stack stack) {
            // ===========================
            // ===== ORIENTED CYCLES =====
            // ===========================
            final var piInverseIndex = getPiInverseIndex(pi.getAddress(), pi.len());

            final var orientedCycles = orientedCycles(spi, piInverseIndex);

            free(piInverseIndex);

            for (int l = 0; l < orientedCycles.len(); l++) {
                final var cycleAddress = orientedCycles.at(l);

                final var before = parity.getBool(at(cycleAddress, 0)) ? 1 : 0;

                for (var i = 0; i < len(cycleAddress) - 2; i++) {
                    for (var j = i + 1; j < len(cycleAddress) - 1; j++) {
                        final var ab_k = j - i;

                        if (before == 1 && (ab_k & 1) == 0) {
                            continue;
                        }

                        for (var k = j + 1; k < len(cycleAddress); k++) {
                            final var bc_k = k - j;

                            if (before == 1 && (bc_k & 1) == 0) {
                                continue;
                            }

                            final var ca_k = (len(cycleAddress) - k) + i;

                            byte a = at(cycleAddress, i), b = at(cycleAddress, j), c = at(cycleAddress, k);

                            var after = ab_k & 1;
                            after += bc_k & 1;
                            after += ca_k & 1;

                            // check if it's applicable
                            if (after - before == 2 && areSymbolsInCyclicOrder(piInverseIndex, a, c, b)) {
                                final var alignedCycleAddress = startingBy(cycleAddress, a);
                                final var aCycle = create(ca_k);
                                set(aCycle, 0, a);
                                // ======= do not use System.arrayCopy to avoid JNI overhead
                                for (int m = 0; m < ca_k - 1; m++) {
                                    set(aCycle, m + 1, at(alignedCycleAddress, ab_k + bc_k + 1 + m));
                                }

                                final var bCycle = create(ab_k);
                                set(bCycle, 0, b);
                                // ======= do not use System.arrayCopy to avoid JNI overhead
                                for (int m = 0; m < ab_k - 1; m++) {
                                    set(bCycle, m + 1, at(alignedCycleAddress, 1 + m));
                                }

                                final var cCycle = create(bc_k);
                                set(cCycle, 0, c);
                                // ======= do not use System.arrayCopy to avoid JNI overhead
                                for (int m = 0; m < bc_k - 1; m++) {
                                    set(cCycle, m + 1, at(alignedCycleAddress, ab_k + 1 + m));
                                }

                                free(alignedCycleAddress);

                                stack.push(a, b, c);

                                // == APPLY THE MOVE ===
                                spi.remove(cycleAddress);
                                if (len(aCycle) > 1) spi.add(aCycle);
                                if (len(bCycle) > 1) spi.add(bCycle);
                                if (len(cCycle) > 1) spi.add(cCycle);
                                update(spiIndex, parity, aCycle, bCycle, cCycle);
                                // =======================

                                for (final var nextMove : rootMove.children) {
                                    final var newPi = applyTransposition(pi, a, b, c);
                                    new Search(configuration, outputDir, spi.clone(), newPi, stack.clone(), nextMove, forkJoinPool, hasSorting).fork();
                                }

                                stack.pop();

                                // ==== ROLLBACK ====
                                if (len(aCycle) > 1) spi.remove(aCycle);
                                if (len(bCycle) > 1) spi.remove(bCycle);
                                if (len(cCycle) > 1) spi.remove(cCycle);

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

            // ======================
            // ===== ODD CYCLES =====
            // ======================

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

                        for (int l = 0; l < triplet.second.len(); l++) {
                            final var cycleAddress = triplet.second.at(l);

                            if (len(cycleAddress) > 1) {
                                spi.add(cycleAddress);
                            }
                        }

                        updateIndex(spiIndex, parity, triplet.second);
                        // ==============================

                        for (final var nextMove : rootMove.children) {
                            final var newPi = applyTransposition(pi, a, b, c);
                            new Search(configuration, outputDir, spi.clone(), newPi, stack.clone(), nextMove, forkJoinPool, hasSorting).fork();
                        }

                        // ==== ROLLBACK ====
                        for (int l = 0; l < triplet.second.len(); l++) {
                            final var cycleAddress = triplet.second.at(l);
                            if (len(cycleAddress) > 1)
                                spi.remove(cycleAddress);
                            free(cycleAddress);
                        }
                        spi.addAll(triplet.first);
                        updateIndex(spiIndex, parity, triplet.first);
                        // ==============================

                        triplet.first.free();
                        triplet.second.free();

                        stack.pop();
                    }
                }
            }

            freeResources();
        }

        private void fork0Moves(final UnsafeListOfCycles spi,
                                final UnsafeBooleanArray parity,
                                final UnsafeLongArray spiIndex,
                                final UnsafeByteArray pi,
                                final Stack stack) {
            final var cycleIndexes = TheUnsafe.get().allocateMemory(pi.len() * 8);
            fill(cycleIndexes, pi.len(),-1);

            final var canonicalSignatures = new HashSet<String>();

            for (int i = 0; i < pi.len() - 2; i++) {
                for (int j = (i + 1); j < pi.len() - 1; j++) {
                    for (int k = (j + 1); k < pi.len(); k++) {

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

                            if (UnsafeLongArray.getLong(cycleIndexes, a) == -1) {
                                final var index = cycleIndex(cycleAddress);
                                UnsafeLongArray.setLong(cycleIndexes, a, index);
                                UnsafeLongArray.setLong(cycleIndexes, b, index);
                                UnsafeLongArray.setLong(cycleIndexes, c, index);
                            }

                            final var indexAddress = UnsafeLongArray.getLong(cycleIndexes, a);

                            if (areSymbolsInCyclicOrder(indexAddress, a, b, c)) {
                                final var before = len(cycleAddress) & 1;

                                final var ab_k = getK(indexAddress, a, b);
                                var after = ab_k & 1;
                                final var bc_k = getK(indexAddress, b, c);
                                after += bc_k & 1;
                                final var ca_k = getK(indexAddress, c, a);
                                after += ca_k & 1;

                                if (after - before == 2) {
                                    // skip, it's a 2-move
                                    continue;
                                }

                                final long alignedCycle = startingBy(cycleAddress, a);

                                after = 0;
                                final var aCycle = create(ca_k);
                                set(aCycle, 0, a);
                                arraycopy(alignedCycle, ab_k + bc_k + 1, aCycle, 1, ca_k - 1);
                                after += ca_k & 1;

                                final var bCycle = create(ab_k);
                                set(bCycle, 0, b);
                                arraycopy(alignedCycle, 1, bCycle, 1, ab_k - 1);
                                after += ab_k & 1;

                                final var cCycle = create(bc_k);
                                set(cCycle, 0, c);
                                arraycopy(alignedCycle, ab_k + 1, cCycle, 1, bc_k - 1);
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
                        spi.removeAll(triplet.first);

                        for (int l = 0; l < triplet.second.len(); l++) {
                            final var cycleAddress = triplet.second.at(l);

                            if (len(cycleAddress) > 1) {
                                spi.add(cycleAddress);
                            }
                        }
                        updateIndex(spiIndex, parity, triplet.second);
                        // ==============================

                        final var newPi = applyTransposition(pi, a, b, c);
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
                            if (len(cycleAddress) > 1)
                                spi.remove(cycleAddress);
                            free(cycleAddress);
                        }
                        spi.addAll(triplet.first);
                        updateIndex(spiIndex, parity, triplet.first);
                        // ==============================

                        triplet.first.free();
                        triplet.second.free();

                        stack.pop();
                    }
                }
            }

            for (int i = 0; i < pi.len(); i++) {
                long indexAddress = getLong(cycleIndexes, i);
                if (indexAddress != -1) {
                    free(indexAddress);
                }
            }

            free(cycleIndexes);
            freeResources();
        }

        private void freeResources() {
            for (int i = 0; i < spi.len(); i++) {
                free(spi.at(i));
            }
            spi.free();
            pi.free();
            stack.free();
        }
    }


    private static UnsafeListOfCycles toListOfCycle(final MulticyclePermutation spi, final Cycle pi) {
        final var UnsafeListOfCycles = new UnsafeListOfCycles(pi.size());
        spi.stream().map(Cycle::getSymbols).forEach(UnsafeListOfCycles::add);
        return UnsafeListOfCycles;
    }

    @SneakyThrows
    public static UnsafeListOfCycles search(final UnsafeListOfCycles spi,
                                            final UnsafeBooleanArray parity,
                                            final UnsafeLongArray spiIndex,
                                            final UnsafeByteArray pi,
                                            final Stack stack,
                                            final Move root) {
        if (Thread.currentThread().isInterrupted()) {
            return EMPTY_LIST;
        }

        if (root.mu == 0) {
            final var key = canonicalSignature(spi, pi, spiIndex);
            final var paths = UNSUCCESSFUL_VISITED_CONFIGS.get(key, () -> new HashSet(1));
            synchronized (paths) {
                if (paths.isEmpty()) {
                    MISSES.incrementAndGet();
                    paths.add(root.pathToRoot());
                } else {
                    if (paths.contains(root.pathToRoot())) {
                        HITS.incrementAndGet();
                        return EMPTY_LIST;
                    } else {
                        paths.add(root.pathToRoot());
                    }
                }
            }

            final var sorting = analyze0Moves(spi, parity, spiIndex, pi, stack, root);
            if (!sorting.isEmpty()) {
                return sorting;
            }
        } else {
            var sorting = analyzeOrientedCycles(spi, parity, spiIndex, pi, stack, root);
            if (!sorting.isEmpty()) {
                return sorting;
            }

            sorting = analyzeOddCycles(spi, parity, spiIndex, pi, stack, root);
            if (!sorting.isEmpty()) {
                return sorting;
            }
        }

        return EMPTY_LIST;
    }

    private static UnsafeListOfCycles analyzeOddCycles(final UnsafeListOfCycles spi,
                                                       final UnsafeBooleanArray parity,
                                                       final UnsafeLongArray spiIndex,
                                                       final UnsafeByteArray pi,
                                                       final Stack stack,
                                                       final Move root) {

        // ==========================
        // ======= odd cycles =======
        // ==========================

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

                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);

                        if (len(cycleAddress) > 1) {
                            spi.add(cycleAddress);
                        }
                    }

                    updateIndex(spiIndex, parity, triplet.second);
                    // ==============================

                    if (root.children.length == 0) {
                        return stack.toListOfCycles();
                    } else {
                        for (final var m : root.children) {
                            final var newPi = applyTransposition(pi, a, b, c);
                            final var sorting = search(spi, parity, spiIndex, newPi, stack, m);
                            newPi.free();
                            if (!sorting.isEmpty()) {
                                return stack.toListOfCycles();
                            }
                        }
                    }

                    // ==== ROLLBACK ====
                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);
                        if (len(cycleAddress) > 1)
                            spi.remove(cycleAddress);
                        free(cycleAddress);
                    }
                    spi.addAll(triplet.first);
                    updateIndex(spiIndex, parity, triplet.first);
                    // ==============================

                    triplet.first.free();
                    triplet.second.free();

                    stack.pop();
                }
            }
        }

        return EMPTY_LIST;
    }

    private static UnsafeListOfCycles analyzeOrientedCycles(final UnsafeListOfCycles spi,
                                                            final UnsafeBooleanArray parity,
                                                            final UnsafeLongArray spiIndex,
                                                            final UnsafeByteArray pi,
                                                            final Stack stack,
                                                            final Move root) {
        // ===============================
        // ======= oriented cycles =======
        // ===============================

        final var piInverseIndex = getPiInverseIndex(pi.getAddress(), pi.len());

        final var orientedCycles = orientedCycles(spi, piInverseIndex);

        free(piInverseIndex);

        for (int l = 0; l < orientedCycles.len(); l++) {
            final var cycleAddress = orientedCycles.at(l);

            if (len(cycleAddress) == 3) {
                byte a = at(cycleAddress, 0), b = at(cycleAddress, 1), c = at(cycleAddress, 2);

                stack.push(a, b, c);

                // == APPLY THE MOVE ===
                spi.remove(cycleAddress);
                update(spiIndex, parity, a, b, c);
                // =======================

                if (root.children.length == 0) {
                    return stack.toListOfCycles();
                } else {
                    for (final var m : root.children) {
                        final var newPi = applyTransposition(pi, a, b, c);
                        final var sorting = search(spi, parity, spiIndex, newPi, stack, m);
                        newPi.free();
                        if (!sorting.isEmpty()) {
                            return stack.toListOfCycles();
                        }
                    }
                }

                stack.pop();

                // ==== ROLLBACK ====
                spi.add(cycleAddress);
                update(spiIndex, parity, cycleAddress);
                // ====================
            } else {
                final var before = parity.getBool(at(cycleAddress, 0)) ? 1 : 0;

                for (var i = 0; i < len(cycleAddress) - 2; i++) {
                    for (var j = i + 1; j < len(cycleAddress) - 1; j++) {
                        final var ab_k = j - i;

                        if (before == 1 && (ab_k & 1) == 0) {
                            continue;
                        }

                        for (var k = j + 1; k < len(cycleAddress); k++) {
                            final var bc_k = k - j;

                            if (before == 1 && (bc_k & 1) == 0) {
                                continue;
                            }

                            final var ca_k = (len(cycleAddress) - k) + i;

                            byte a = at(cycleAddress, i), b = at(cycleAddress, j), c = at(cycleAddress, k);

                            var after = ab_k & 1;
                            after += bc_k & 1;
                            after += ca_k & 1;

                            // check if it's applicable
                            if (after - before == 2 && areSymbolsInCyclicOrder(piInverseIndex, a, c, b)) {
                                final var alignedCycleAddress = startingBy(cycleAddress, a);
                                final var aCycle = create(ca_k);
                                set(aCycle, 0, a);
                                // ======= do not use System.arrayCopy to avoid JNI overhead
                                for (int m = 0; m < ca_k - 1; m++) {
                                    set(aCycle, m + 1, at(alignedCycleAddress, ab_k + bc_k + 1 + m));
                                }

                                final var bCycle = create(ab_k);
                                set(bCycle, 0, b);
                                // ======= do not use System.arrayCopy to avoid JNI overhead
                                for (int m = 0; m < ab_k - 1; m++) {
                                    set(bCycle, m + 1, at(alignedCycleAddress, 1 + m));
                                }

                                final var cCycle = create(bc_k);
                                set(cCycle, 0, c);
                                // ======= do not use System.arrayCopy to avoid JNI overhead
                                for (int m = 0; m < bc_k - 1; m++) {
                                    set(cCycle, m + 1, at(alignedCycleAddress, ab_k + 1 + m));
                                }

                                free(alignedCycleAddress);

                                stack.push(a, b, c);

                                // == APPLY THE MOVE ===
                                spi.remove(cycleAddress);
                                if (len(aCycle) > 1) spi.add(aCycle);
                                if (len(bCycle) > 1) spi.add(bCycle);
                                if (len(cCycle) > 1) spi.add(cCycle);
                                update(spiIndex, parity, aCycle, bCycle, cCycle);
                                // =======================

                                if (root.children.length == 0) {
                                    return stack.toListOfCycles();
                                } else {
                                    for (final var m : root.children) {
                                        final var newPi = applyTransposition(pi, a, b, c);
                                        final var sorting = search(spi, parity, spiIndex, newPi, stack, m);
                                        newPi.free();
                                        if (!sorting.isEmpty()) {
                                            return stack.toListOfCycles();
                                        }
                                    }
                                }

                                stack.pop();

                                // ==== ROLLBACK ====
                                if (len(aCycle) > 1) spi.remove(aCycle);
                                if (len(bCycle) > 1) spi.remove(bCycle);
                                if (len(cCycle) > 1) spi.remove(cCycle);

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
        }

        orientedCycles.free();

        return EMPTY_LIST;
    }

    private static UnsafeListOfCycles analyze0Moves(final UnsafeListOfCycles spi,
                                                    final UnsafeBooleanArray parity,
                                                    final UnsafeLongArray spiIndex,
                                                    final UnsafeByteArray pi,
                                                    final Stack stack,
                                                    final Move root) {
        // =======================
        // ======= 0 Moves =======
        // =======================

        final var cycleIndexes = TheUnsafe.get().allocateMemory(pi.len() * 8);
        fill(cycleIndexes, pi.len(),-1);

        for (int i = 0; i < pi.len() - 2; i++) {
            for (int j = (i + 1); j < pi.len() - 1; j++) {
                for (int k = (j + 1); k < pi.len(); k++) {

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

                        if (UnsafeLongArray.getLong(cycleIndexes, a) == -1) {
                            final var index = cycleIndex(cycleAddress);
                            UnsafeLongArray.setLong(cycleIndexes, a, index);
                            UnsafeLongArray.setLong(cycleIndexes, b, index);
                            UnsafeLongArray.setLong(cycleIndexes, c, index);
                        }

                        final var indexAddress = UnsafeLongArray.getLong(cycleIndexes, a);

                        if (areSymbolsInCyclicOrder(indexAddress, a, b, c)) {
                            final var before = len(cycleAddress) & 1;

                            final var ab_k = getK(indexAddress, a, b);
                            var after = ab_k & 1;
                            final var bc_k = getK(indexAddress, b, c);
                            after += bc_k & 1;
                            final var ca_k = getK(indexAddress, c, a);
                            after += ca_k & 1;

                            if (after - before == 2) {
                                // skip, it's a 2-move
                                continue;
                            }

                            final long alignedCycle = startingBy(cycleAddress, a);

                            after = 0;
                            final var aCycle = create(ca_k);
                            set(aCycle, 0, a);
                            arraycopy(alignedCycle, ab_k + bc_k + 1, aCycle, 1, ca_k - 1);
                            after += ca_k & 1;

                            final var bCycle = create(ab_k);
                            set(bCycle, 0, b);
                            arraycopy(alignedCycle, 1, bCycle, 1, ab_k - 1);
                            after += ab_k & 1;

                            final var cCycle = create(bc_k);
                            set(cCycle, 0, c);
                            arraycopy(alignedCycle, ab_k + 1, cCycle, 1, bc_k - 1);
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
                    spi.removeAll(triplet.first);

                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);

                        if (len(cycleAddress) > 1) {
                            spi.add(cycleAddress);
                        }
                    }
                    updateIndex(spiIndex, parity, triplet.second);
                    // ==============================

                    if (root.children.length == 0) {
                        return stack.toListOfCycles();
                    } else {
                        for (final var m : root.children) {
                            final var newPi = applyTransposition(pi, a, b, c);
                            final var sorting = search(spi, parity, spiIndex, newPi, stack, m);
                            newPi.free();
                            if (!sorting.isEmpty()) {
                                return stack.toListOfCycles();
                            }
                        }
                    }

                    // ==== ROLLBACK ====
                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);
                        if (len(cycleAddress) > 1)
                            spi.remove(cycleAddress);
                        free(cycleAddress);
                    }
                    spi.addAll(triplet.first);
                    updateIndex(spiIndex, parity, triplet.first);
                    // ==============================

                    triplet.first.free();
                    triplet.second.free();

                    stack.pop();
                }
            }
        }

        for (int i = 0; i < pi.len(); i++) {
            long indexAddress = getLong(cycleIndexes, i);
            if (indexAddress != -1) {
                free(indexAddress);
            }
        }

        free(cycleIndexes);

        return EMPTY_LIST;
    }

    public static String canonicalSignature(final UnsafeListOfCycles spi,
                                            final UnsafeByteArray pi,
                                            final UnsafeLongArray spiIndex) {
        var leastHashCode = Integer.MAX_VALUE;
        long canonical = -1;

        for (int i = 0; i < pi.len(); i++) {
            final var symbol = pi.getByte(i);
            
            final var shifting = startingBy(pi.getAddress(), symbol);

            // float
            var signature = signature(spi, shifting, spiIndex);

            var hashCode = UnsafeFloatArray.hashCode(signature, pi.len());

            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = signature;
            } else if (hashCode == leastHashCode) {
                canonical = least(signature, pi.len(), canonical);
            }

            if (canonical != signature) {
                free(signature);
            }

            final var mirroredSignature = UnsafeFloatArray.clone(signature, pi.len());
            UnsafeFloatArray.reverse(mirroredSignature, pi.len());

            final var labelLabelMapping = TheUnsafe.get().allocateMemory(spi.len());
            final var orientedIndexMapping = TheUnsafe.get().allocateMemory((long) spi.len() * 8);
            UnsafeLongArray.fill(orientedIndexMapping, spi.len(), -1);
            final var deltas = TheUnsafe.get().allocateMemory((long) spi.len() * 4);

            byte nextLabel = 1;
            for (byte j = 0; j < pi.len(); j++) {
                final var label = getByte(mirroredSignature, j);

                byte mappedLabel = getByte(labelLabelMapping, label);
                if (mappedLabel == 0) {
                    setByte(labelLabelMapping, label, nextLabel++);
                }

                final var newLabel = getByte(labelLabelMapping, label);

                if (label % 1 > 0) {
                    if (getLong(orientedIndexMapping, newLabel) == -1) {
                        final var index = Math.abs(j - pi.len()) - 1;
                        var alignedCycleAddress = startingBy(spiIndex.getLong(getByte(shifting, index)), getByte(shifting, index));
                        reverse(alignedCycleAddress);
                        setLong(orientedIndexMapping, newLabel, cycleIndex(alignedCycleAddress));
                        final var delta = len(alignedCycleAddress) - round((label % 1) * 100);
                        setFloat(deltas, newLabel, delta);

                        free(alignedCycleAddress);
                    }

                    final var index = Math.abs(j - pi.len()) - 1;
                    final var orientationIndex = getByte(getLong(orientedIndexMapping, newLabel), index) + 1;
                    setFloat(mirroredSignature, j, newLabel + (((orientationIndex + getFloat(deltas, newLabel)) % len(spiIndex.getLong((getByte(shifting, index))))) / 100));
                    if (getFloat(mirroredSignature, j) % 1 == 0)
                        setFloat(mirroredSignature, j, newLabel + len(spiIndex.getLong(getByte(shifting, index))) / 100f);
                } else {
                    setFloat(mirroredSignature, j, newLabel);
                }
            }

            free(orientedIndexMapping);
            free(deltas);
            free(labelLabelMapping);

            hashCode = UnsafeFloatArray.hashCode(mirroredSignature, pi.len());
            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = mirroredSignature;
            } else if (hashCode == leastHashCode) {
                canonical = least(mirroredSignature, pi.len(), canonical);
            }

            if (canonical != mirroredSignature) {
                free(mirroredSignature);
            }

            free(shifting);
        }

        try {
            return toString(canonical, pi.len());
        } finally {
            free(canonical);
        }
    }

    public static String toString(long cycleAddress) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < len(cycleAddress); i++) {
            sb.append(at(cycleAddress, i));
            if (i != len(cycleAddress) - 1)
                sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    private static void reverse(final long cycleAddress) {
        final var len = len(cycleAddress);
        for (byte i = 0; i < len / 2; i++) {
            byte t = at(cycleAddress, i);
            set(cycleAddress, i, at(cycleAddress, len - i - 1));
            set(cycleAddress, (byte) (len - i - 1), t);
        }
    }

    private static float round(final float value) {
        return (float) Math.round(value * 100) / 100;
    }

    private static long least(final long signatureAddress, final byte len, final long canonicalAddress) {
        for (int i = 0; i < len; i++) {
            if (getFloat(signatureAddress, i) != getFloat(canonicalAddress, i)) {
                if (getFloat(signatureAddress, i) < getFloat(canonicalAddress, i))
                    return signatureAddress;
                else
                    return canonicalAddress;
            }
        }
        return canonicalAddress;
    }

    public static long signature(final UnsafeListOfCycles spi, final long pi, final UnsafeLongArray spiIndex) {
        final var piInverseIndex = getPiInverseIndex(pi, spiIndex.size());
        final var orientedCycles = orientedCycles(spi, piInverseIndex);

        byte len = spiIndex.size();
        final var orientationByCycle = TheUnsafe.get().allocateMemory(len);

        for (int l = 0; l < orientedCycles.len(); l++) {
            UnsafeBooleanArray.set(orientationByCycle, at(orientedCycles.at(l),0), true);
        }

        free(piInverseIndex);
        orientedCycles.free();

        // Array of floats
        final var labelByCycle = TheUnsafe.get().allocateMemory(len * 4);
        for (int i = 0; i < len; i++) {
            TheUnsafe.get().putFloat(labelByCycle + (i * 4), -1);
        }

        // Array of longs
        final var symbolIndexByOrientedCycle = TheUnsafe.get().allocateMemory(len * 8);

        final var signatureAddress = TheUnsafe.get().allocateMemory(len * 4);

        // Pi index
        final var piIndex = TheUnsafe.get().allocateMemory(len);
        for (byte i = 0; i < len; i++) {
            setByte(piIndex, at(pi, i), i);
        }

        var currentLabel = 1f;

        for (var i = 0; i < len; i++) {
            final int symbol = at(pi, i);
            final var cycleAddress = spiIndex.getLong(symbol);

            if (UnsafeBooleanArray.getBool(orientationByCycle, at(cycleAddress, 0))) {
                final var cycleIndex = TheUnsafe.get().allocateMemory(len);

                var minIndex = Byte.MAX_VALUE;
                var symbolMinIndex = 0;
                byte cycleLen = len(cycleAddress);
                for (int s = 0; s < cycleLen; s++) {
                    if (getByte(piIndex, (byte) s) < minIndex) {
                        minIndex = getByte(piIndex, (byte) s);
                        symbolMinIndex = s;
                    }
                }

                for (int j = 0; j < cycleLen; j++) {
                    if (at(cycleAddress, j) == symbolMinIndex) {
                        for (int k = 0; k < cycleLen; k++) {
                            setByte(cycleIndex, at(cycleAddress, (j + k) % cycleLen), (byte)(k + 1));
                        }
                        break;
                    }
                }
                setLong(symbolIndexByOrientedCycle, at(cycleAddress, 0), cycleIndex);
            }

            final var label = getFloat(labelByCycle, at(cycleAddress, 0));

            if (label == -1) {
                setFloat(labelByCycle, at(cycleAddress, 0), currentLabel);
                currentLabel++;
            }

            setFloat(signatureAddress, at(cycleAddress, i), UnsafeBooleanArray.getBool(orientationByCycle, at(cycleAddress, 0)) ?
                    label + (float) getByte(getLong(symbolIndexByOrientedCycle, at(cycleAddress, 0)), symbol) / 100 :
                    label);
        }

        free(labelByCycle);
        free(orientationByCycle);
        free(piIndex);
        for (int i = 0; i < len; i++) {
            free(getLong(symbolIndexByOrientedCycle, i));
        }
        free(symbolIndexByOrientedCycle);

        return signatureAddress;
    }

    private static String toString(final long signature, final byte len) {
        final var builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            final var v = getFloat(signature, i);
            if (v % 1 == 0) {
                builder.append((int) v);
            } else {
                builder.append(v);
            }
            builder.append(',');
        }
        return builder.toString();
    }

    public static boolean areSymbolsInCyclicOrder(final long piInverseIndex, final byte a, final byte b, final byte c) {
        return (getByte(piInverseIndex, a) < getByte(piInverseIndex, b) && getByte(piInverseIndex, b) < getByte(piInverseIndex, c)) ||
                (getByte(piInverseIndex, b) < getByte(piInverseIndex, c) && getByte(piInverseIndex, c) < getByte(piInverseIndex, a)) ||
                (getByte(piInverseIndex, c) < getByte(piInverseIndex, a) && getByte(piInverseIndex, a) < getByte(piInverseIndex, b));
    }

    private static void update(final UnsafeLongArray spiIndex, final UnsafeBooleanArray parity, final long cycleAddress) {
        byte len = len(cycleAddress);
        if (len == 1) {
            spiIndex.setLong(at(cycleAddress, 0), -1);
            parity.set(at(cycleAddress, 0), true);
        } else {
            final var p = (len & 1) == 1;
            for (byte k = 0; k < len; k++) {
                spiIndex.setLong(k, cycleAddress);
                parity.set(k, p);
            }
        }
    }

    private static void update(final UnsafeLongArray spiIndex,
                               final UnsafeBooleanArray parity,
                               final long aCycle,
                               final long bCycle,
                               final long cCycle) {
        update(spiIndex, parity, aCycle);
        update(spiIndex, parity, bCycle);
        update(spiIndex, parity, cCycle);
    }

    public static long getPiInverseIndex(final long pi, final byte len) {
        final var piInverseIndex = TheUnsafe.get().allocateMemory(len);
        for (byte i = 0; i < len; i++) {
            setByte(piInverseIndex, at(pi, len - i - 1), i);
        }
        return piInverseIndex;
    }

    private static UnsafeListOfCycles orientedCycles(final UnsafeListOfCycles spi, final long piInverseIndex) {
        final var orientedCycles = new UnsafeListOfCycles(2);
        for (int i = 0; i < spi.len(); i++) {
            final long cycleAddress = spi.at(i);
            if (!areSymbolsInCyclicOrder(piInverseIndex, cycleAddress))
                orientedCycles.add(cycleAddress);
        }
        return orientedCycles;
    }

    private static void updateIndex(final UnsafeLongArray spiIndex, final UnsafeBooleanArray parity, final UnsafeListOfCycles cycles) {
        for (int i = 0; i < cycles.len(); i++) {
            final var cycleAddress = cycles.at(i);
            if (len(cycleAddress) == 1) {
                spiIndex.setLong(at(cycleAddress, 0), -1);
                parity.set(at(cycleAddress, 0), true);
            } else {
                final var p = (len(cycleAddress) & 1) == 1;
                for (byte k = 0; k < len(cycleAddress); k++) {
                    spiIndex.setLong(k, cycleAddress);
                    parity.set(k, p);
                }
            }
        }
    }

    public static boolean areSymbolsInCyclicOrder(final long indexAddress, final long cycleAddress) {
        boolean leap = false;
        final var len = len(cycleAddress);
        for (int i = 0; i < len; i++) {
            int nextIndex = i + 1;
            if (nextIndex >= len)
                nextIndex = (i + 1) % len;
            if (getByte(indexAddress, at(cycleAddress, i)) >
                getByte(indexAddress, at(cycleAddress, nextIndex))) {
                if (!leap) {
                    leap = true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    private static Triplet<UnsafeListOfCycles, UnsafeListOfCycles, Integer> simulate0MoveTwoCycles(final UnsafeLongArray spiIndex,
                                                                                                   final int a,
                                                                                                   final int b,
                                                                                                   final int c) {
        int numberOfEvenCycles = 0;
        int a_, b_, c_;
        if (spiIndex.getLong(a) == spiIndex.getLong(c)) {
            a_ = a;
            b_ = c;
            c_ = b;
            numberOfEvenCycles += len(spiIndex.getLong(a)) & 1;
            numberOfEvenCycles += len(spiIndex.getLong(b)) & 1;
        } else if (spiIndex.getLong(a) == spiIndex.getLong(b)) {
            a_ = b;
            b_ = a;
            c_ = c;
            numberOfEvenCycles += len(spiIndex.getLong(a)) & 1;
            numberOfEvenCycles += len(spiIndex.getLong(c)) & 1;
        } else {
            // spi.getCycle(b) == spi.getCycle(c)
            a_ = c;
            b_ = b;
            c_ = a;
            numberOfEvenCycles += len(spiIndex.getLong(a)) & 1;
            numberOfEvenCycles += len(spiIndex.getLong(c)) & 1;
        }

        final var index = cycleIndex(spiIndex.getLong(c_));
        final var cImage = image(index, spiIndex.getLong(c_), c_);
        final var abCycle = startingBy(spiIndex.getLong(a_), a_);
        final var cCycle = startingBy(spiIndex.getLong(c_), cImage);

        final var abCycleIndex = cycleIndex(abCycle);

        final var ba_k = getK(abCycleIndex, b_, a_);
        final var newaCycle = create(ba_k);
        set(newaCycle, 0, (byte) a_);
        final var ab_k = getK(abCycleIndex, a_, b_);
        cyclecopy(abCycle, ab_k + 1, newaCycle, 1, ba_k - 1);

        final var cCycleLen = len(cCycle);
        final var newbCycle = create(cCycleLen + ab_k);
        set(newbCycle, 0, (byte) b_);
        cyclecopy(cCycle, 0, newbCycle, 1, cCycleLen);
        cyclecopy(abCycle, 1, newbCycle, 1 + cCycleLen, ab_k - 1);

        var newNumberOfEvenCycles = 0;
        newNumberOfEvenCycles += ba_k & 1;
        newNumberOfEvenCycles += ba_k & 1;

        final var oldCycles = new UnsafeListOfCycles(2);
        oldCycles.add(spiIndex.getLong(a));
        if (!oldCycles.contains(spiIndex.getLong(b)))
            oldCycles.add(spiIndex.getLong(b));
        if (!oldCycles.contains(spiIndex.getLong(c)))
            oldCycles.add(spiIndex.getLong(c));

        final var newCycles = new UnsafeListOfCycles(2);
        newCycles.add(newaCycle);
        newCycles.add(newbCycle);

        free(index);

        return new Triplet<>(oldCycles, newCycles, newNumberOfEvenCycles - numberOfEvenCycles);
    }

    private static int image(final long indexAddress, final long cycleAddress, final int a) {
        return at(cycleAddress, ((at(indexAddress, a) + 1) % len(cycleAddress)));
    }

    private static Triplet<UnsafeListOfCycles, UnsafeListOfCycles, Integer> simulate0MoveSameCycle(final UnsafeLongArray cycleIndex,
                                                                                                   final byte a,
                                                                                                   final byte b,
                                                                                                   final byte c) {
        final var oldCycle = cycleIndex.getLong(a);

        final var alignedCycle = startingBy(oldCycle, b);
        final var newCycle = create(len(oldCycle));

        final long oldCycleIndex = cycleIndex(oldCycle);

        set(newCycle,  0, b);
        final var ab_k = getK(oldCycleIndex, b, a);
        final var bc_k = getK(oldCycleIndex, a, c);
        cyclecopy(alignedCycle, ab_k + 1, newCycle, 1, bc_k - 1);
        set(newCycle,  bc_k, c);

        cyclecopy(alignedCycle, 1, newCycle, 1 + bc_k, ab_k - 1);
        set(newCycle,  (ab_k + bc_k), a);

        final var ca_k = getK(oldCycleIndex, c, b);
        cyclecopy(alignedCycle, ab_k + bc_k + 1,
                newCycle, ab_k + bc_k + 1, ca_k - 1);

        free(oldCycleIndex);
        free(alignedCycle);

        return new Triplet<>(UnsafeListOfCycles.singleton(oldCycle), UnsafeListOfCycles.singleton(newCycle), 0);
    }

    // returns a reference to a "cycle", meaning that the first position is the length
    private static long cycleIndex(final long cycleAddress) {
        final var indexAddress = create((max(cycleAddress) + 1));

        for (byte i = 0; i < len(cycleAddress); i++) {
            set(indexAddress, at(cycleAddress, i), i);
        }

        return indexAddress;
    }

    public static byte max(final long cycleAddress) {
        byte max = at(cycleAddress,  0);
        for (byte i = 1; i < len(cycleAddress); i++) {
            if (at(cycleAddress,i) > max) {
                max = at(cycleAddress, i);
            }
        }
        return max;
    }

    private static int getK(final long cycleIndexAddress, int a, int b) {
        final var aIndex = at(cycleIndexAddress, a);
        final var bIndex = at(cycleIndexAddress, b);

        if (bIndex >= aIndex)
            return bIndex - aIndex;

        return (len(cycleIndexAddress) - aIndex) + bIndex;
    }

    public static long create(final int length) {
        final var cycleAddress = TheUnsafe.get().allocateMemory(length + 1);
        TheUnsafe.get().putByte(cycleAddress, (byte) length);
        return cycleAddress;
    }

    public static void set(final long cycleAddress, final int i, final byte value) {
        TheUnsafe.get().putByte(cycleAddress + i + 1, value);
    }

    public static byte at(final long cycleAddress, int i) {
        return TheUnsafe.get().getByte(cycleAddress + i + 1);
    }

    public static byte len(final long cycleAddress) {
        return TheUnsafe.get().getByte(cycleAddress);
    }

    private static void free(final long address) {
        TheUnsafe.get().freeMemory(address);
    }

    private static long startingBy(long cycleAddress, int a) {
        if (at(cycleAddress,  0) == a)
            return cloneCycle(cycleAddress);

        final var length = len(cycleAddress);

        final var result = create(length);

        for (byte i = 0; i < length; i++) {
            if (at(cycleAddress, i) == a) {
                cyclecopy(cycleAddress, i, result, 0, length - i);
                cyclecopy(cycleAddress, 0, result, length - i, i);
                break;
            }
        }

        return result;
    }

    private static long cloneCycle(final long cycleAddress) {
        byte length = len(cycleAddress);
        final var clone = create(length);
        cyclecopy(cycleAddress, 0, clone, 0, length);
        return clone;
    }

    private static UnsafeByteArray applyTransposition(final UnsafeByteArray pi,
                                                      final int a,
                                                      final int b,
                                                      final int c) {
        byte index0 = -1, index1 = -1, index2 = -1;

        for (byte i = 0; i < pi.len(); i++) {
            if (pi.getByte(i) == a)
                index0 = i;
            if (pi.getByte(i) == b)
                index1 = i;
            if (pi.getByte(i) == c)
                index2 = i;

            if (index0 != -1 && index1 != -1 && index2 != -1)
                break;
        }

        // sort indexes - this is CPU efficient
        if (index0 > index2) {
            final var temp = index0;
            index0 = index2;
            index2 = temp;
        }

        if (index0 > index1) {
            final var temp = index0;
            index0 = index1;
            index1 = temp;
        }

        if (index1 > index2) {
            final var temp = index1;
            index1 = index2;
            index2 = temp;
        }

        final var result = new UnsafeByteArray(pi.len());

        arraycopy(pi.getAddress(), 0, result.getAddress(), 0, index0);
        arraycopy(pi.getAddress(), index1, result.getAddress(), index0, index2 - index1);
        arraycopy(pi.getAddress(), index0, result.getAddress(), index0 + (index2 - index1), index1 - index0);
        arraycopy(pi.getAddress(), index2, result.getAddress(), index2, pi.len() - index2);

        return result;
    }

    private static void cyclecopy(final long srcAddress, int srcPos, long destAddress, int destPost, int length) {
        TheUnsafe.get().copyMemory(srcAddress + srcPos + 1, destAddress + destPost + 1, length);
    }

    public static void arraycopy(final long srcAddress, int srcPos, long destAddress, int destPost, int length) {
        TheUnsafe.get().copyMemory(srcAddress + srcPos, destAddress + destPost, length);
    }

    private static void removeTrivialCycles(final UnsafeListOfCycles spi) {
        final var toRemove = new LongArrayList();

        for (int i = 0; i < spi.len(); i++) {
            final var cycleAddress = spi.at(i);
            if (len(cycleAddress) == 1) {
                toRemove.add(cycleAddress);
            }
        }

        spi.removeAll(toRemove);
    }
}
