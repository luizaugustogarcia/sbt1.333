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
import org.eclipse.collections.impl.factory.primitive.LongLongMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
import static br.unb.cic.tdp.util.Sorter.toString;

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
        final var forkJoinPool = new ForkJoinPool(1);
        final var spi = toUnsafeListOfCycle(configuration.getSpi(), configuration.getPi());
        final var pi = new UnsafeByteArray(configuration.getPi().getSymbols());
        final var stack = new Stack(rootMove.getHeight());
        final var task = new Search(configuration, outputDir, spi, pi, stack, rootMove, forkJoinPool, hasSorting);
        forkJoinPool.submit(task);
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        if (!hasSorting[0])
            System.out.println("Didn't find sorting for " + configuration.getSpi() + "\n");
    }

    private static UnsafeListOfCycles toUnsafeListOfCycle(final MulticyclePermutation spi, final Cycle pi) {
        final var list = new UnsafeListOfCycles(pi.size());
        for (final var cycle: spi) {
            list.add(cycle.getSymbols());
        }
        return list;
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

                    updateIndex(spiIndex, parity, triplet.second);
                    // ==============================

                    if (root.children.length == 0) {
                        return stack.toListOfCycles();
                    } else {
                        for (final var m : root.children) {
                            final var newPi = applyTransposition(pi, a, b, c, pi.len() - numberOfTrivialCycles, spiIndex);
                            final var sorting = search(spi, parity, spiIndex, newPi, stack, m);
                            free(newPi.getAddress());
                            if (!sorting.isEmpty()) {
                                return stack.toListOfCycles();
                            }
                        }
                    }

                    // ==== ROLLBACK ====
                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);
                        if (cycleLen(cycleAddress) > 1)
                            spi.remove(cycleAddress);
                        free(cycleAddress);
                    }
                    spi.addAll(triplet.first);
                    updateIndex(spiIndex, parity, triplet.first);
                    // ==============================

                    free(triplet.first.getElementDataAddress());
                    free(triplet.second.getElementDataAddress());

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

        final var piInverseIndex = getPiInverseIndex(pi.getAddress(), pi.len(), spiIndex.size());

        final var orientedCycles = orientedCycles(spi, piInverseIndex);

        free(piInverseIndex);

        for (int l = 0; l < orientedCycles.len(); l++) {
            final var cycleAddress = orientedCycles.at(l);

            if (cycleLen(cycleAddress) == 3) {
                byte a = cycleAt(cycleAddress, 0), b = cycleAt(cycleAddress, 1), c = cycleAt(cycleAddress, 2);

                stack.push(a, b, c);

                // == APPLY THE MOVE ===
                spi.remove(cycleAddress);
                update(spiIndex, parity, a, b, c);
                // =======================

                if (root.children.length == 0) {
                    return stack.toListOfCycles();
                } else {
                    for (final var m : root.children) {
                        final var newPi = applyTransposition(pi, a, b, c, pi.len() - 3, spiIndex);
                        final var sorting = search(spi, parity, spiIndex, newPi, stack, m);
                        free(newPi.getAddress());
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
                                // ======= do not use System.arrayCopy to avoid JNI overhead
                                for (int m = 0; m < ca_k - 1; m++) {
                                    cycleSet(aCycle, m + 1, cycleAt(alignedCycleAddress, ab_k + bc_k + 1 + m));
                                }

                                final var bCycle = create(ab_k);
                                cycleSet(bCycle, 0, b);
                                // ======= do not use System.arrayCopy to avoid JNI overhead
                                for (int m = 0; m < ab_k - 1; m++) {
                                    cycleSet(bCycle, m + 1, cycleAt(alignedCycleAddress, 1 + m));
                                }

                                final var cCycle = create(bc_k);
                                cycleSet(cCycle, 0, c);
                                // ======= do not use System.arrayCopy to avoid JNI overhead
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

                                if (root.children.length == 0) {
                                    return stack.toListOfCycles();
                                } else {
                                    for (final var m : root.children) {
                                        final var newPi = applyTransposition(pi, a, b, c, pi.len() - numberOfTrivialCycles, spiIndex);
                                        final var sorting = search(spi, parity, spiIndex, newPi, stack, m);
                                        free(newPi.getAddress());
                                        if (!sorting.isEmpty()) {
                                            return stack.toListOfCycles();
                                        }
                                    }
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
        }

        free(orientedCycles.getElementDataAddress());

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
        UnsafeLongArray.fill(cycleIndexes, pi.len(), (byte) 0);

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
                            arraycopy(alignedCycle, ab_k + bc_k + 1, aCycle, 1, ca_k - 1);
                            after += ca_k & 1;

                            final var bCycle = create(ab_k);
                            cycleSet(bCycle, 0, b);
                            arraycopy(alignedCycle, 1, bCycle, 1, ab_k - 1);
                            after += ab_k & 1;

                            final var cCycle = create(bc_k);
                            cycleSet(cCycle, 0, c);
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
                    var numberOfTrivialCycles = 0;

                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);

                        if (cycleLen(cycleAddress) > 1) {
                            spi.add(cycleAddress);
                        } else {
                            numberOfTrivialCycles++;
                        }
                    }
                    updateIndex(spiIndex, parity, triplet.second);
                    // ==============================

                    if (root.children.length == 0) {
                        return stack.toListOfCycles();
                    } else {
                        for (final var m : root.children) {
                            final var newPi = applyTransposition(pi, a, b, c, pi.len() - numberOfTrivialCycles, spiIndex);
                            final var sorting = search(spi, parity, spiIndex, newPi, stack, m);
                            free(newPi.getAddress());
                            if (!sorting.isEmpty()) {
                                return stack.toListOfCycles();
                            }
                        }
                    }

                    // ==== ROLLBACK ====
                    for (int l = 0; l < triplet.second.len(); l++) {
                        final var cycleAddress = triplet.second.at(l);
                        if (cycleLen(cycleAddress) > 1)
                            spi.remove(cycleAddress);
                        free(cycleAddress);
                    }
                    spi.addAll(triplet.first);
                    updateIndex(spiIndex, parity, triplet.first);
                    // ==============================

                    free(triplet.first.getElementDataAddress());
                    free(triplet.second.getElementDataAddress());

                    stack.pop();
                }
            }
        }

        final var freedIndexes = LongLongMaps.mutable.empty();

        for (int i = 0; i < pi.len(); i++) {
            long indexAddress = getLong(cycleIndexes, i);
            if (!freedIndexes.containsKey(indexAddress)) {
                free(indexAddress);
                freedIndexes.put(indexAddress, 0);
            }
        }

        free(cycleIndexes);

        return EMPTY_LIST;
    }

    public static String canonicalSignature(final UnsafeListOfCycles spi,
                                            final UnsafeByteArray pi,
                                            final UnsafeLongArray spiIndex) {
        var leastHashCode = Integer.MAX_VALUE;
        long canonical = 0;

        final byte len = pi.len();

        for (int i = 0; i < len; i++) {
            final var symbol = pi.getByte(i);

            final var shifting = startingByArray(pi.getAddress(), len, symbol);

            // float
            var signature = signature(spi, shifting, len, spiIndex);

            var hashCode = UnsafeFloatArray.hashCode(signature, len);

            var temp = canonical;
            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = signature;
            } else if (hashCode == leastHashCode) {
                canonical = least(signature, len, canonical);
            }

            final var mirroredSignature = UnsafeFloatArray.clone(signature, len);
            UnsafeFloatArray.reverse(mirroredSignature, len);

            if (temp == canonical) {
                free(signature);
            }

            final var labelLabelMapping = TheUnsafe.get().allocateMemory(spi.len() + 1);
            UnsafeByteArray.fill(labelLabelMapping, spi.len() + 1, (byte) 0);

            final var orientedIndexMapping = TheUnsafe.get().allocateMemory((long) (spi.len() + 1) * 8);
            UnsafeLongArray.fill(orientedIndexMapping, spi.len() + 1, (byte) 0);

            final var deltas = TheUnsafe.get().allocateMemory((long) (spi.len() + 1) * 4);

            byte nextLabel = 1;
            for (byte j = 0; j < len; j++) {
                final var label = getFloat(mirroredSignature, j);

                byte mappedLabel = getByte(labelLabelMapping, (int) label);
                if (mappedLabel == 0) {
                    setByte(labelLabelMapping, (int) label, nextLabel++);
                }

                final var newLabel = (float) getByte(labelLabelMapping, (int) label);

                if (label % 1 > 0) {
                    if (getLong(orientedIndexMapping, (byte) newLabel) == 0) {
                        final var index = Math.abs(j - len) - 1;

                        byte shiftingSymbol = getByte(shifting, index);

                        var alignedCycleAddress = startingBy(spiIndex.getLong(shiftingSymbol), shiftingSymbol);
                        reverse(alignedCycleAddress);
                        setLong(orientedIndexMapping, (byte) newLabel, cycleIndex(alignedCycleAddress));
                        final var delta = cycleLen(alignedCycleAddress) - round((label % 1) * 100);
                        setFloat(deltas, (byte) newLabel, delta);

                        free(alignedCycleAddress);
                    }

                    final var index = Math.abs(j - len) - 1;
                    byte shiftingSymbol = getByte(shifting, index);

                    System.out.println(spi.toString());
                    System.out.println("getting orientedIndexMapping at position " + (byte) newLabel);
                    System.out.println("address = " + getLong(orientedIndexMapping, (byte) newLabel));
                    System.out.println("bytes = " + UnsafeByteArray.toString(getLong(orientedIndexMapping, (byte) newLabel), (byte) 35));
                    System.out.println("shiftingSymbol = " + shiftingSymbol);
                    final var orientationIndex = cycleAt(getLong(orientedIndexMapping, (byte) newLabel), shiftingSymbol) + 1;

                    setFloat(mirroredSignature, j, newLabel + (((orientationIndex + getFloat(deltas, (byte) newLabel)) % cycleLen(spiIndex.getLong(shiftingSymbol))) / 100));
                    if (getFloat(mirroredSignature, j) % 1 == 0)
                        setFloat(mirroredSignature, j, newLabel + cycleLen(spiIndex.getLong(shiftingSymbol)) / 100f);
                } else {
                    setFloat(mirroredSignature, j, newLabel);
                }
            }

            free(labelLabelMapping);
            free(deltas);

            for (int j = 0; j < spi.len(); j++) {
                final var indexAddress = getLong(orientedIndexMapping, j);
                if (indexAddress > 0)
                    free(indexAddress);
            }
            free(orientedIndexMapping);

            temp = canonical;
            hashCode = UnsafeFloatArray.hashCode(mirroredSignature, len);
            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = mirroredSignature;
            } else if (hashCode == leastHashCode) {
                canonical = least(mirroredSignature, len, canonical);
            }

            if (temp == canonical) {
                free(mirroredSignature);
            }

            free(shifting);
        }

        try {
            return toString(canonical, len);
        } finally {
            free(canonical);
        }
    }

    public static String toString(long cycleAddress) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < cycleLen(cycleAddress); i++) {
            sb.append(cycleAt(cycleAddress, i));
            if (i != cycleLen(cycleAddress) - 1)
                sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    private static void reverse(final long cycleAddress) {
        final var len = cycleLen(cycleAddress);
        for (byte i = 0; i < len / 2; i++) {
            byte t = cycleAt(cycleAddress, i);
            cycleSet(cycleAddress, i, cycleAt(cycleAddress, len - i - 1));
            cycleSet(cycleAddress, (byte) (len - i - 1), t);
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

    public static long signature(final UnsafeListOfCycles spi, final long pi, byte len, final UnsafeLongArray spiIndex) {
        final var piInverseIndex = getPiInverseIndex(pi, len, spiIndex.size());
        final var orientedCycles = orientedCycles(spi, piInverseIndex);

        final var orientationByCycle = TheUnsafe.get().allocateMemory(len);
        UnsafeBooleanArray.fill(orientationByCycle, len, false);

        for (int l = 0; l < orientedCycles.len(); l++) {
            UnsafeBooleanArray.set(orientationByCycle, cycleAt(orientedCycles.at(l), 0), true);
        }

        free(piInverseIndex);
        free(orientedCycles.getElementDataAddress());

        // Array of floats
        final var labelByCycle = TheUnsafe.get().allocateMemory(len * 4);
        for (int i = 0; i < len; i++) {
            TheUnsafe.get().putFloat(labelByCycle + (i * 4), -1);
        }

        // Array of longs
        final var symbolIndexByOrientedCycle = TheUnsafe.get().allocateMemory(len * 8);
        UnsafeLongArray.fill(symbolIndexByOrientedCycle, len, (byte) 0);

        final var signatureAddress = TheUnsafe.get().allocateMemory(len * 4);

        // Pi index
        final var piIndex = TheUnsafe.get().allocateMemory(len);
        for (byte i = 0; i < len; i++) {
            setByte(piIndex, getByte(pi, i), i);
        }

        var currentLabel = 1f;

        for (byte i = 0; i < len; i++) {
            final int symbol = getByte(pi, i);
            final var cycleAddress = spiIndex.getLong(symbol);

            byte firstSymbol = cycleAt(cycleAddress, 0);
            if (UnsafeBooleanArray.getBool(orientationByCycle, firstSymbol)) {
                final var symbolIndex = TheUnsafe.get().allocateMemory(len);

                var minIndex = Byte.MAX_VALUE;
                var symbolMinIndex = 0;
                byte cycleLen = cycleLen(cycleAddress);
                for (int j = 0; j < cycleLen; j++) {
                    final var s = getByte(cycleAddress, j);
                    if (getByte(piIndex, s) < minIndex) {
                        minIndex = getByte(piIndex, s);
                        symbolMinIndex = s;
                    }
                }

                for (int j = 0; j < cycleLen; j++) {
                    if (cycleAt(cycleAddress, j) == symbolMinIndex) {
                        for (int k = 0; k < cycleLen; k++) {
                            setByte(symbolIndex, cycleAt(cycleAddress, (j + k) % cycleLen), (byte) (k + 1));
                        }
                        break;
                    }
                }
                setLong(symbolIndexByOrientedCycle, firstSymbol, symbolIndex);
            }

            var label = getFloat(labelByCycle, firstSymbol);

            if (label == -1) {
                setFloat(labelByCycle, firstSymbol, currentLabel);
                currentLabel++;
            }

            label = getFloat(labelByCycle, firstSymbol);
            setFloat(signatureAddress, i, UnsafeBooleanArray.getBool(orientationByCycle, firstSymbol) ?
                    label + (float) getByte(getLong(symbolIndexByOrientedCycle, firstSymbol), symbol) / 100 :
                    label);
        }

        free(labelByCycle);
        free(orientationByCycle);
        free(piIndex);

        for (int i = 0; i < len; i++) {
            final var cycleIndex = getLong(symbolIndexByOrientedCycle, i);
            if (cycleIndex != 0) {
                free(getLong(symbolIndexByOrientedCycle, i));
            }
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
            if (i < len -1)
                builder.append(',');
        }
        return builder.toString();
    }

    public static boolean areSymbolsInCyclicOrder(final long piInverseIndex, final byte a, final byte b, final byte c) {
        return (getByte(piInverseIndex, a) < getByte(piInverseIndex, b) && getByte(piInverseIndex, b) < getByte(piInverseIndex, c)) ||
                (getByte(piInverseIndex, b) < getByte(piInverseIndex, c) && getByte(piInverseIndex, c) < getByte(piInverseIndex, a)) ||
                (getByte(piInverseIndex, c) < getByte(piInverseIndex, a) && getByte(piInverseIndex, a) < getByte(piInverseIndex, b));
    }

    public static boolean areSymbolsInCyclicOrderCycleIndex(final long cycleIndex, final byte a, final byte b, final byte c) {
        return (cycleAt(cycleIndex, a) < cycleAt(cycleIndex, b) && cycleAt(cycleIndex, b) < cycleAt(cycleIndex, c)) ||
                (cycleAt(cycleIndex, b) < cycleAt(cycleIndex, c) && cycleAt(cycleIndex, c) < cycleAt(cycleIndex, a)) ||
                (cycleAt(cycleIndex, c) < cycleAt(cycleIndex, a) && cycleAt(cycleIndex, a) < cycleAt(cycleIndex, b));
    }

    public static void update(final UnsafeLongArray spiIndex, final UnsafeBooleanArray parity, final long cycleAddress) {
        byte len = cycleLen(cycleAddress);
        if (len == 1) {
            spiIndex.setLong(cycleAt(cycleAddress, 0), 0);
            parity.set(cycleAt(cycleAddress, 0), true);
        } else {
            final var p = (len & 1) == 1;
            for (byte k = 0; k < len; k++) {
                spiIndex.setLong(k, cycleAddress);
                parity.set(k, p);
            }
        }
    }

    public static void update(final UnsafeLongArray spiIndex,
                              final UnsafeBooleanArray parity,
                              final long aCycle,
                              final long bCycle,
                              final long cCycle) {
        update(spiIndex, parity, aCycle);
        update(spiIndex, parity, bCycle);
        update(spiIndex, parity, cCycle);
    }

    public static long getPiInverseIndex(final long pi, final byte len, final byte originalPiLength) {
        final var piInverseIndex = TheUnsafe.get().allocateMemory(originalPiLength);
        for (byte i = 0; i < len; i++) {
            setByte(piInverseIndex, getByte(pi, len - i - 1), i);
        }
        return piInverseIndex;
    }

    public static UnsafeListOfCycles orientedCycles(final UnsafeListOfCycles spi, final long piInverseIndex) {
        final var orientedCycles = new UnsafeListOfCycles(2);
        for (int i = 0; i < spi.len(); i++) {
            final long cycleAddress = spi.at(i);
            if (!areSymbolsInCyclicOrder(piInverseIndex, cycleAddress))
                orientedCycles.add(cycleAddress);
        }
        return orientedCycles;
    }

    public static void updateIndex(final UnsafeLongArray spiIndex, final UnsafeBooleanArray parity, final UnsafeListOfCycles cycles) {
        for (int i = 0; i < cycles.len(); i++) {
            final var cycleAddress = cycles.at(i);
            if (cycleLen(cycleAddress) == 1) {
                spiIndex.setLong(cycleAt(cycleAddress, 0), 0);
                parity.set(cycleAt(cycleAddress, 0), true);
            } else {
                final var p = (cycleLen(cycleAddress) & 1) == 1;
                for (int j = 0; j < cycleLen(cycleAddress); j++) {
                    final var k = cycleAt(cycleAddress, j);
                    spiIndex.setLong(k, cycleAddress);
                    parity.set(k, p);
                }
            }
        }
    }

    public static boolean areSymbolsInCyclicOrder(final long indexAddress, final long cycleAddress) {
        boolean leap = false;
        final var len = cycleLen(cycleAddress);
        for (int i = 0; i < len; i++) {
            int nextIndex = i + 1;
            if (nextIndex >= len)
                nextIndex = (i + 1) % len;
            if (getByte(indexAddress, cycleAt(cycleAddress, i)) >
                    getByte(indexAddress, cycleAt(cycleAddress, nextIndex))) {
                if (!leap) {
                    leap = true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    public static Triplet<UnsafeListOfCycles, UnsafeListOfCycles, Integer> simulate0MoveTwoCycles(final UnsafeLongArray spiIndex,
                                                                                                  final int a,
                                                                                                  final int b,
                                                                                                  final int c) {
        int numberOfEvenCycles = 0;
        int a_, b_, c_;
        if (spiIndex.getLong(a) == spiIndex.getLong(c)) {
            a_ = a;
            b_ = c;
            c_ = b;
            numberOfEvenCycles += cycleLen(spiIndex.getLong(a)) & 1;
            numberOfEvenCycles += cycleLen(spiIndex.getLong(b)) & 1;
        } else if (spiIndex.getLong(a) == spiIndex.getLong(b)) {
            a_ = b;
            b_ = a;
            c_ = c;
            numberOfEvenCycles += cycleLen(spiIndex.getLong(a)) & 1;
            numberOfEvenCycles += cycleLen(spiIndex.getLong(c)) & 1;
        } else {
            // spi.getCycle(b) == spi.getCycle(c)
            a_ = c;
            b_ = b;
            c_ = a;
            numberOfEvenCycles += cycleLen(spiIndex.getLong(a)) & 1;
            numberOfEvenCycles += cycleLen(spiIndex.getLong(c)) & 1;
        }

        final var index = cycleIndex(spiIndex.getLong(c_));
        final var cImage = image(index, spiIndex.getLong(c_), c_);
        final var abCycle = startingBy(spiIndex.getLong(a_), a_);
        final var cCycle = startingBy(spiIndex.getLong(c_), cImage);

        final var abCycleIndex = cycleIndex(abCycle);

        final var ba_k = getK(abCycleIndex, abCycle, b_, a_);
        final var newaCycle = create(ba_k);
        cycleSet(newaCycle, 0, (byte) a_);
        final var ab_k = getK(abCycleIndex, abCycle, a_, b_);
        cyclecopy(abCycle, ab_k + 1, newaCycle, 1, ba_k - 1);

        final var cCycleLen = cycleLen(cCycle);
        final var newbCycle = create(cCycleLen + ab_k);
        cycleSet(newbCycle, 0, (byte) b_);
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

        free(abCycleIndex);
        free(index);

        return new Triplet<>(oldCycles, newCycles, newNumberOfEvenCycles - numberOfEvenCycles);
    }

    private static int image(final long indexAddress, final long cycleAddress, final int a) {
        return cycleAt(cycleAddress, ((cycleAt(indexAddress, a) + 1) % cycleLen(cycleAddress)));
    }

    public static Triplet<UnsafeListOfCycles, UnsafeListOfCycles, Integer> simulate0MoveSameCycle(final UnsafeLongArray cycleIndex,
                                                                                                  final byte a,
                                                                                                  final byte b,
                                                                                                  final byte c) {
        final var oldCycle = cycleIndex.getLong(a);

        final var alignedCycle = startingBy(oldCycle, b);
        final var newCycle = create(cycleLen(oldCycle));

        final long oldCycleIndex = cycleIndex(oldCycle);

        cycleSet(newCycle, 0, b);
        final var ab_k = getK(oldCycleIndex, oldCycle, b, a);
        final var bc_k = getK(oldCycleIndex, oldCycle, a, c);
        cyclecopy(alignedCycle, ab_k + 1, newCycle, 1, bc_k - 1);
        cycleSet(newCycle, bc_k, c);

        cyclecopy(alignedCycle, 1, newCycle, 1 + bc_k, ab_k - 1);
        cycleSet(newCycle, (ab_k + bc_k), a);

        final var ca_k = getK(oldCycleIndex, oldCycle, c, b);
        cyclecopy(alignedCycle, ab_k + bc_k + 1,
                newCycle, ab_k + bc_k + 1, ca_k - 1);

        free(oldCycleIndex);
        free(alignedCycle);

        return new Triplet<>(UnsafeListOfCycles.singleton(oldCycle), UnsafeListOfCycles.singleton(newCycle), 0);
    }

    // returns a reference to a "cycle", meaning that the first position is the length
    public static long cycleIndex(final long cycleAddress) {
        final var max = max(cycleAddress);
        final var indexAddress = TheUnsafe.get().allocateMemory(max + 2);
        UnsafeByteArray.fill(indexAddress, max + 2, (byte) -1);

        byte cycleLen = cycleLen(cycleAddress);
        setByte(indexAddress, 0, (byte) (max + 1));

        for (byte i = 0; i < cycleLen; i++) {
            cycleSet(indexAddress, cycleAt(cycleAddress, i), i);
        }

        return indexAddress;
    }

    public static byte max(final long cycleAddress) {
        byte max = cycleAt(cycleAddress, 0);
        for (byte i = 1; i < cycleLen(cycleAddress); i++) {
            if (cycleAt(cycleAddress, i) > max) {
                max = cycleAt(cycleAddress, i);
            }
        }
        return max;
    }

    public static int getK(final long cycleIndexAddress, final long cycleAddress, int a, int b) {
        final var aIndex = cycleAt(cycleIndexAddress, a);
        final var bIndex = cycleAt(cycleIndexAddress, b);

        if (bIndex >= aIndex)
            return bIndex - aIndex;

        return (cycleLen(cycleAddress) - aIndex) + bIndex;
    }

    public static long create(final int length) {
        final var cycleAddress = TheUnsafe.get().allocateMemory(length + 1);
        TheUnsafe.get().putByte(cycleAddress, (byte) length);
        return cycleAddress;
    }

    public static void cycleSet(final long cycleAddress, final int i, final byte value) {
        TheUnsafe.get().putByte(cycleAddress + i + 1, value);
    }

    public static byte cycleAt(final long cycleAddress, int i) {
        return TheUnsafe.get().getByte(cycleAddress + i + 1);
    }

    public static byte cycleLen(final long cycleAddress) {
        return TheUnsafe.get().getByte(cycleAddress);
    }

    public static void free(final long address) {
// TODO remove
//        System.out.println("Stack trace:");
//        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
//        for (int i = 1; i < stackTraces.length; i++) {
//            System.out.println(stackTraces[i]);
//        }
        TheUnsafe.get().freeMemory(address);
// TODO remove
//        System.out.println("freed!");
    }

    private static long startingByArray(final long arrayAddress, final int len, final int a) {
        if (getByte(arrayAddress, 0) == a) {
            final var clone = TheUnsafe.get().allocateMemory(len);
            arraycopy(arrayAddress, 0, clone, 0, len);
            return clone;
        }

        final var result = TheUnsafe.get().allocateMemory(len);
        for (byte i = 0; i < len; i++) {
            if (getByte(arrayAddress, i) == a) {
                arraycopy(arrayAddress, i, result, 0, len - i);
                arraycopy(arrayAddress, 0, result, len - i, i);
                break;
            }
        }

        return result;
    }

    public static long startingBy(final long cycleAddress, final int a) {
        if (cycleAt(cycleAddress, 0) == a)
            return cloneCycle(cycleAddress);

        final var len = cycleLen(cycleAddress);

        final var result = create(len);

        for (byte i = 0; i < len; i++) {
            if (cycleAt(cycleAddress, i) == a) {
                cyclecopy(cycleAddress, i, result, 0, len - i);
                cyclecopy(cycleAddress, 0, result, len - i, i);
                break;
            }
        }

        return result;
    }

    private static long cloneCycle(final long cycleAddress) {
        byte length = cycleLen(cycleAddress);
        final var clone = create(length);
        cyclecopy(cycleAddress, 0, clone, 0, length);
        return clone;
    }

    public static UnsafeByteArray applyTransposition(final UnsafeByteArray pi,
                                                     final int a,
                                                     final int b,
                                                     final int c,
                                                     final int numberOfSymbols,
                                                     final UnsafeLongArray spiIndex) {
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

        final var result = new UnsafeByteArray((byte) numberOfSymbols);

        int counter = 0;
        for (int i = 0; i < index0; i++) {
            if (spiIndex.getLong(pi.getByte(i)) == 0) continue;
            result.set(counter, pi.getByte(i));
            counter++;
        }

        for (int i = 0; i < index2 - index1; i++) {
            if (spiIndex.getLong(pi.getByte(index1 + i)) == 0) continue;
            result.set(counter, pi.getByte(index1 + i));
            counter++;
        }

        for (int i = 0; i < index1 - index0; i++) {
            if (spiIndex.getLong(pi.getByte(index0 + i)) == 0) continue;
            result.set(counter, pi.getByte(index0 + i));
            counter++;
        }

        for (int i = 0; i < pi.len() - index2; i++) {
            if (spiIndex.getLong(pi.getByte(index2 + i)) == 0) continue;
            result.set(counter, pi.getByte(index2 + i));
            counter++;
        }

        return result;
    }

    private static void cyclecopy(final long srcAddress, int srcPos, long destAddress, int destPost, int length) {
        TheUnsafe.get().copyMemory(srcAddress + srcPos + 1, destAddress + destPost + 1, length);
    }

    public static void arraycopy(final long srcAddress, int srcPos, long destAddress, int destPost, int length) {
        TheUnsafe.get().copyMemory(srcAddress + srcPos, destAddress + destPost, length);
    }

    public static void removeTrivialCycles(final UnsafeListOfCycles spi) {
        final var toRemove = new LongArrayList();

        for (int i = 0; i < spi.len(); i++) {
            final var cycleAddress = spi.at(i);
            if (cycleLen(cycleAddress) == 1) {
                toRemove.add(cycleAddress);
                free(cycleAddress);
            }
        }

        spi.removeAll(toRemove);
    }
}
