package br.unb.cic.tdp.util;

import br.unb.cic.tdp.Application;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.util.ListOfCycles;
import br.unb.cic.tdp.proof.util.Move;
import br.unb.cic.tdp.proof.util.Stack;
import br.unb.cic.tdp.unsafe.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static br.unb.cic.tdp.proof.ProofGenerator.*;
import static br.unb.cic.tdp.unsafe.UnsafeListOfCycles.EMPTY_LIST;
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
        ).forEach(conf -> {
            sort(conf, "C:/Users/Luiz/Temp/sbt1.333proof", _16_12_SEQS);
        });

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
                toListOfCycle(configuration.getSpi(), configuration.getPi()), configuration.getPi().getSymbols(),
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
        final ListOfCycles spi;
        final int[] pi;
        final Stack stack;
        final Move rootMove;
        final ForkJoinPool forkJoinPool;
        boolean[] hasSorting;

        public Search(final Configuration configuration, final String outputDir, final ListOfCycles spi,
                      final int[] pi, final Stack stack, final Move rootMove, final ForkJoinPool forkJoinPool,
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

            final var parity = new boolean[configuration.getSpi().getMaxSymbol() + 1];
            final var spiIndex = new int[configuration.getSpi().getMaxSymbol() + 1][];

            var newPi = pi;

            for (int i = 0; i < spi.size; i++) {
                final var cycle = spi.elementData[i];
                if (cycle.length == 1) {
                    newPi = ArrayUtils.removeElement(newPi, cycle[0]);
                }
                for (int s : cycle) {
                    spiIndex.get(s] = cycle;
                    parity[s] = (cycle.length & 1) == 1;
                }
            }

            removeTrivialCycles(spi);

            // if passed through three zeros moves, COMPUTE DIRECTLY
            if (rootMove.numberOfZeroMovesUntilTop() > 3) {
                final var sorting = search(spi, parity, spiIndex, spiIndex.length, newPi, stack, rootMove);

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
                    final var key = canonicalSignature(spi, pi, spiIndex, spiIndex.length);
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

                    fork0Moves(spi, spiIndex, parity, stack);
                } else {
                    fork2Moves(spi, spiIndex, parity, stack);
                }
            }
        }

        private void fork2Moves(final ListOfCycles spi, final int[][] spiIndex, final boolean[] parity, final Stack stack) {
            // ===========================
            // ===== ORIENTED CYCLES =====
            // ===========================
            final var piInverseIndex = getPiInverseIndex(pi, spiIndex.length);

            final var orientedCycles = orientedCycles(spi, piInverseIndex);

            for (int l = 0; l < orientedCycles.size; l++) {
                final var cycle = orientedCycles.elementData[l];

                final var before = parity[cycle[0]] ? 1 : 0;

                for (var i = 0; i < cycle.length - 2; i++) {
                    for (var j = i + 1; j < cycle.length - 1; j++) {
                        final var ab_k = j - i;

                        if (before == 1 && (ab_k & 1) == 0) {
                            continue;
                        }

                        for (var k = j + 1; k < cycle.length; k++) {
                            final var bc_k = k - j;

                            if (before == 1 && (bc_k & 1) == 0) {
                                continue;
                            }

                            final var ca_k = (cycle.length - k) + i;

                            int a = cycle[i], b = cycle[j], c = cycle[k];

                            var after = ab_k & 1;
                            after += bc_k & 1;
                            after += ca_k & 1;

                            // check if it's applicable
                            if (after - before == 2 && areSymbolsInCyclicOrder(piInverseIndex, a, c, b)) {
                                final int[] symbols = startingBy(cycle, a);
                                final var aCycle = new int[ca_k];
                                aCycle[0] = a;
                                System.arraycopy(symbols, ab_k + bc_k + 1, aCycle, 1, ca_k - 1);

                                final var bCycle = new int[ab_k];
                                bCycle[0] = b;
                                System.arraycopy(symbols, 1, bCycle, 1, ab_k - 1);

                                final var cCycle = new int[bc_k];
                                cCycle[0] = c;
                                System.arraycopy(symbols, ab_k + 1, cCycle, 1, bc_k - 1);

                                stack.push(a, b, c);

                                // == APPLY THE MOVE ===
                                spi.remove(cycle);
                                var numberOfTrivialCycles = 0;
                                if (aCycle.length > 1) spi.add(aCycle);
                                else numberOfTrivialCycles++;
                                if (bCycle.length > 1) spi.add(bCycle);
                                else numberOfTrivialCycles++;
                                if (cCycle.length > 1) spi.add(cCycle);
                                else numberOfTrivialCycles++;
                                update(spiIndex, parity, aCycle, bCycle, cCycle);
                                // =======================

                                for (final var nextMove : rootMove.children) {
                                    int[] newPi = applyTransposition(pi, a, b, c, pi.length - numberOfTrivialCycles, spiIndex);
                                    new Search(configuration, outputDir, spi.clone(), newPi, stack.clone(), nextMove, forkJoinPool, hasSorting).fork();
                                }

                                stack.pop();

                                // ==== ROLLBACK ====
                                if (aCycle.length > 1) spi.remove(aCycle);
                                if (bCycle.length > 1) spi.remove(bCycle);
                                if (cCycle.length > 1) spi.remove(cCycle);
                                spi.add(cycle);
                                update(spiIndex, parity, cycle);
                                // ====================
                            }
                        }
                    }
                }
            }

            // ======================
            // ===== ODD CYCLES =====
            // ======================

            for (int i = 0; i < pi.length - 2; i++) {
                if (parity[pi[i]]) continue;
                for (int j = i + 1; j < pi.length - 1; j++) {
                    if (parity[pi[j]]) continue;
                    for (int k = j + 1; k < pi.length; k++) {
                        if (parity[pi[k]]) continue;

                        int a = pi[i], b = pi[j], c = pi[k];

                        // if it's the same cycle, skip it
                        if (spiIndex.get(a] == spiIndex.get(b] && spiIndex.get(b] == spiIndex.get(c])
                            continue;

                        final var is_2Move = spiIndex.get(a] != spiIndex.get(b] &&
                                spiIndex.get(b] != spiIndex.get(c] &&
                                spiIndex.get(a] != spiIndex.get(c];
                        if (is_2Move)
                            continue;

                        final Triplet<ListOfCycles, ListOfCycles, Integer> triplet = simulate0MoveTwoCycles(spiIndex, a, b, c);

                        if (triplet.third != 2)
                            continue;

                        stack.push(a, b, c);

                        // == APPLY THE MOVE ===
                        spi.removeAll(triplet.first);
                        var numberOfTrivialCycles = 0;

                        for (int l = 0; l < triplet.second.size; l++) {
                            final var cycle = triplet.second.elementData[l];

                            if (cycle.length > 1) {
                                spi.add(cycle);
                            } else {
                                numberOfTrivialCycles++;
                            }
                        }

                        updateIndex(spiIndex, parity, triplet.second);
                        // ==============================

                        for (final var nextMove : rootMove.children) {
                            int[] newPi = applyTransposition(pi, a, b, c, pi.length - numberOfTrivialCycles, spiIndex);
                            new Search(configuration, outputDir, spi.clone(), newPi, stack.clone(), nextMove, forkJoinPool, hasSorting).fork();
                        }

                        // ==== ROLLBACK ====
                        for (int l = 0; l < triplet.second.size; l++) {
                            final var cycle = triplet.second.elementData[l];
                            if (cycle.length > 1)
                                spi.remove(cycle);
                        }
                        spi.addAll(triplet.first);
                        updateIndex(spiIndex, parity, triplet.first);
                        // ==============================

                        stack.pop();
                    }
                }
            }
        }

        private void fork0Moves(final ListOfCycles spi, final int[][] spiIndex, final boolean[] parity, final Stack stack) {
            final var cycleIndexes = new int[spiIndex.length + 1][];

            final var canonicalSignatures = new HashSet<String>();

            for (int i = 0; i < pi.length - 2; i++) {
                for (int j = i + 1; j < pi.length - 1; j++) {
                    for (int k = j + 1; k < pi.length; k++) {

                        int a = pi[i], b = pi[j], c = pi[k];

                        final var is_2Move = spiIndex.get(a] != spiIndex.get(b] &&
                                spiIndex.get(b] != spiIndex.get(c] &&
                                spiIndex.get(a] != spiIndex.get(c];
                        if (is_2Move)
                            continue;

                        final Triplet<ListOfCycles, ListOfCycles, Integer> triplet;
                        // if it's the same cycle
                        if (spiIndex.get(a] == spiIndex.get(b] && spiIndex.get(b] == spiIndex.get(c]) {
                            final var cycle = spiIndex.get(a];

                            if (cycleIndexes[a] == null) {
                                final var index = cycleIndex(cycle);
                                cycleIndexes[a] = index;
                                cycleIndexes[b] = index;
                                cycleIndexes[c] = index;
                            }

                            final var index = cycleIndexes[a];

                            if (areSymbolsInCyclicOrder(index, a, b, c)) {
                                final var before = cycle.length & 1;

                                final var ab_k = getK(index, cycle, a, b);
                                var after = ab_k & 1;
                                final var bc_k = getK(index, cycle, b, c);
                                after += bc_k & 1;
                                final var ca_k = getK(index, cycle, c, a);
                                after += ca_k & 1;

                                if (after - before == 2) {
                                    // skip, it's a 2-move
                                    continue;
                                }

                                after = 0;
                                final int[] symbols = startingBy(cycle, a);
                                final var aCycle = new int[ca_k];
                                aCycle[0] = a;
                                System.arraycopy(symbols, ab_k + bc_k + 1, aCycle, 1, ca_k - 1);
                                after += aCycle.length & 1;

                                final var bCycle = new int[ab_k];
                                bCycle[0] = b;
                                System.arraycopy(symbols, 1, bCycle, 1, ab_k - 1);
                                after += bCycle.length & 1;

                                final var cCycle = new int[bc_k];
                                cCycle[0] = c;
                                System.arraycopy(symbols, ab_k + 1, cCycle, 1, bc_k - 1);
                                after += cCycle.length & 1;

                                triplet = new Triplet<>(ListOfCycles.singleton(cycle), ListOfCycles.asList(aCycle, bCycle, cCycle), after - before);
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

                        for (int l = 0; l < triplet.second.size; l++) {
                            final var cycle = triplet.second.elementData[l];

                            if (cycle.length > 1) {
                                spi.add(cycle);
                            } else {
                                numberOfTrivialCycles++;
                            }
                        }
                        updateIndex(spiIndex, parity, triplet.second);
                        // ==============================

                        int[] newPi = applyTransposition(pi, a, b, c, pi.length - numberOfTrivialCycles, spiIndex);
                        final var canonicalSignature = canonicalSignature(spi, newPi, spiIndex, spiIndex.length);
                        if (!canonicalSignatures.contains(canonicalSignature)) {
                            for (final var nextMove : rootMove.children) {
                                new Search(configuration, outputDir, spi.clone(), newPi, stack.clone(), nextMove, forkJoinPool, hasSorting).fork();
                            }
                            canonicalSignatures.add(canonicalSignature);
                        }

                        // ==== ROLLBACK ====
                        for (int l = 0; l < triplet.second.size; l++) {
                            final var cycle = triplet.second.elementData[l];
                            if (cycle.length > 1)
                                spi.remove(cycle);
                        }
                        spi.addAll(triplet.first);
                        updateIndex(spiIndex, parity, triplet.first);
                        // ==============================

                        stack.pop();
                    }
                }
            }
        }
    }

    private static ListOfCycles toListOfCycle(final MulticyclePermutation spi, final Cycle pi) {
        final var listOfCycles = new ListOfCycles(pi.size());
        spi.stream().map(Cycle::getSymbols).forEach(listOfCycles::add);
        return listOfCycles;
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

            // =======================
            // ======= 0 Moves =======
            // =======================

            final var cycleIndexes = new int[(int) pi.len()][];

            for (byte i = 0; i < pi.len() - 2; i++) {
                for (byte j = (i + 1); j < pi.len() - 1; j++) {
                    for (byte k = (j + 1); k < pi.len(); k++) {

                        byte a = pi.at(i), b = pi.at(j), c = pi.at(k);

                        final var is_2Move = spiIndex.at(a) != spiIndex.at(b) &&
                                spiIndex.at(b) != spiIndex.at(c) &&
                                spiIndex.at(a) != spiIndex.at(c);
                        if (is_2Move)
                            continue;

                        final Triplet<ListOfCycles, ListOfCycles, Integer> triplet;
                        // if it's the same cycle
                        if (spiIndex.at(a) == spiIndex.at(b) && spiIndex.at(b) == spiIndex.at(c)) {
                            final var cycle = spiIndex.at(a);

                            if (cycleIndexes[a] == null) {
                                final var index = cycleIndex(cycle);
                                cycleIndexes[a] = index;
                                cycleIndexes[b] = index;
                                cycleIndexes[c] = index;
                            }

                            final var index = cycleIndexes[a];

                            if (areSymbolsInCyclicOrder(index, a, b, c)) {
                                final var before = cycle.length & 1;

                                final var ab_k = getK(index, cycle, a, b);
                                var after = ab_k & 1;
                                final var bc_k = getK(index, cycle, b, c);
                                after += bc_k & 1;
                                final var ca_k = getK(index, cycle, c, a);
                                after += ca_k & 1;

                                if (after - before == 2) {
                                    // skip, it's a 2-move
                                    continue;
                                }

                                final long alignedCycle = startingBy(cycle, a);

                                after = 0;
                                final var aCycle = new int[ca_k];
                                aCycle[0] = a;
                                System.arraycopy(alignedCycle, ab_k + bc_k + 1, aCycle, 1, ca_k - 1);
                                after += aCycle.length & 1;

                                final var bCycle = new int[ab_k];
                                bCycle[0] = b;
                                System.arraycopy(alignedCycle, 1, bCycle, 1, ab_k - 1);
                                after += bCycle.length & 1;

                                final var cCycle = new int[bc_k];
                                cCycle[0] = c;
                                System.arraycopy(alignedCycle, ab_k + 1, cCycle, 1, bc_k - 1);
                                after += cCycle.length & 1;

                                TheUnsafe.get().freeMemory(alignedCycle);

                                triplet = new Triplet<>(ListOfCycles.singleton(cycle), ListOfCycles.asList(aCycle, bCycle, cCycle), after - before);
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

                        for (int l = 0; l < triplet.second.size; l++) {
                            final var cycle = triplet.second.elementData[l];

                            if (cycle.length > 1) {
                                spi.add(cycle);
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
                                final var newPi = applyTransposition(pi, a, b, c);
                                final var sorting = search(spi, parity, spiIndex, newPi, stack, m);
                                newPi.free();
                                if (!sorting.isEmpty()) {
                                    return stack.toListOfCycles();
                                }
                            }
                        }

                        // ==== ROLLBACK ====
                        for (int l = 0; l < triplet.second.size; l++) {
                            final var cycle = triplet.second.elementData[l];
                            if (cycle.length > 1)
                                spi.remove(cycle);
                        }
                        spi.addAll(triplet.first);
                        updateIndex(spiIndex, parity, triplet.first);
                        // ==============================

                        stack.pop();
                    }
                }
            }
        } else {
            // ===============================
            // ======= oriented cycles =======
            // ===============================

            final var piInverseIndex = getPiInverseIndex(pi.getAddress());

            final var orientedCycles = orientedCycles(spi, piInverseIndex);

            free(piInverseIndex);

            for (int l = 0; l < orientedCycles.len(); l++) {
                final var cycle = orientedCycles.at(l);

                if (len(cycle) == 3) {
                    byte a = at(cycle, 0), b = at(cycle, 1), c = at(cycle, 2);

                    stack.push(a, b, c);

                    // == APPLY THE MOVE ===
                    spi.remove(cycle);
                    update(spiIndex, parity, a, b, c);
                    // =======================

                    if (root.children.length == 0) {
                        return stack.toListOfCycles();
                    } else {
                        for (final var m : root.children) {
                            final var newPi = applyTransposition(pi, a, b, c);
                            final var sorting = search(spi, parity, spiIndex, maxSymbol, newPi, stack, m);
                            newPi.free();
                            if (!sorting.isEmpty()) {
                                return stack.toListOfCycles();
                            }
                        }
                    }

                    stack.pop();

                    // ==== ROLLBACK ====
                    spi.add(cycle);
                    update(spiIndex, parity, cycle);
                    // ====================
                } else {
                    final var before = parity[cycle[0]] ? 1 : 0;

                    for (var i = 0; i < cycle.length - 2; i++) {
                        for (var j = i + 1; j < cycle.length - 1; j++) {
                            final var ab_k = j - i;

                            if (before == 1 && (ab_k & 1) == 0) {
                                continue;
                            }

                            for (var k = j + 1; k < cycle.length; k++) {
                                final var bc_k = k - j;

                                if (before == 1 && (bc_k & 1) == 0) {
                                    continue;
                                }

                                final var ca_k = (cycle.length - k) + i;

                                int a = cycle[i], b = cycle[j], c = cycle[k];

                                var after = ab_k & 1;
                                after += bc_k & 1;
                                after += ca_k & 1;

                                // check if it's applicable
                                if (after - before == 2 && areSymbolsInCyclicOrder(piInverseIndex, a, c, b)) {
                                    final int[] symbols = startingBy(cycle, a);
                                    final var aCycle = new int[ca_k];
                                    aCycle[0] = a;
                                    // ======= do not use System.arrayCopy to avoid JNI overhead
                                    for (int m = 0; m < ca_k - 1; m++) {
                                        aCycle[m + 1] = symbols[ab_k + bc_k + 1 + m];
                                    }

                                    final var bCycle = new int[ab_k];
                                    bCycle[0] = b;
                                    // ======= do not use System.arrayCopy to avoid JNI overhead
                                    for (int m = 0; m < ab_k - 1; m++) {
                                        bCycle[m + 1] = symbols[1 + m];
                                    }

                                    final var cCycle = new int[bc_k];
                                    cCycle[0] = c;
                                    // ======= do not use System.arrayCopy to avoid JNI overhead
                                    for (int m = 0; m < bc_k - 1; m++) {
                                        cCycle[m + 1] = symbols[ab_k + 1 + m];
                                    }

                                    stack.push(a, b, c);

                                    // == APPLY THE MOVE ===
                                    spi.remove(cycle);
                                    var numberOfTrivialCycles = 0;
                                    if (aCycle.length > 1) spi.add(aCycle);
                                    else numberOfTrivialCycles++;
                                    if (bCycle.length > 1) spi.add(bCycle);
                                    else numberOfTrivialCycles++;
                                    if (cCycle.length > 1) spi.add(cCycle);
                                    else numberOfTrivialCycles++;
                                    update(spiIndex, parity, aCycle, bCycle, cCycle);
                                    // =======================

                                    if (root.children.length == 0) {
                                        return stack.toListOfCycles();
                                    } else {
                                        for (final var m : root.children) {
                                            int[] newPi = applyTransposition(pi, a, b, c, spiIndex);
                                            final var sorting = search(spi, parity, spiIndex, maxSymbol, newPi, stack, m);
                                            if (!sorting.isEmpty()) {
                                                return stack.toListOfCycles();
                                            }
                                        }
                                    }

                                    stack.pop();

                                    // ==== ROLLBACK ====
                                    if (aCycle.length > 1) spi.remove(aCycle);
                                    if (bCycle.length > 1) spi.remove(bCycle);
                                    if (cCycle.length > 1) spi.remove(cCycle);
                                    spi.add(cycle);
                                    update(spiIndex, parity, cycle);
                                    // ====================
                                }
                            }
                        }
                    }
                }
            }

            // ==========================
            // ======= odd cycles =======
            // ==========================

            for (int i = 0; i < pi.length - 2; i++) {
                if (parity[pi[i]]) continue;
                for (int j = i + 1; j < pi.length - 1; j++) {
                    if (parity[pi[j]]) continue;
                    for (int k = j + 1; k < pi.length; k++) {
                        if (parity[pi[k]]) continue;

                        int a = pi[i], b = pi[j], c = pi[k];

                        // if it's the same cycle, skip it
                        if (spiIndex.at(a] == spiIndex.at(b] && spiIndex.at(b] == spiIndex.at(c])
                            continue;

                        final var is_2Move = spiIndex.at(a] != spiIndex.at(b] &&
                                spiIndex.at(b] != spiIndex.at(c] &&
                                spiIndex.at(a] != spiIndex.at(c];
                        if (is_2Move)
                            continue;

                        final Triplet<ListOfCycles, ListOfCycles, Integer> triplet = simulate0MoveTwoCycles(spiIndex, a, b, c);

                        if (triplet.third != 2)
                            continue;

                        stack.push(a, b, c);

                        // == APPLY THE MOVE ===
                        spi.removeAll(triplet.first);
                        var numberOfTrivialCycles = 0;

                        for (int l = 0; l < triplet.second.size; l++) {
                            final var cycle = triplet.second.elementData[l];

                            if (cycle.length > 1) {
                                spi.add(cycle);
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
                                int[] newPi = applyTransposition(pi, a, b, c, pi.length - numberOfTrivialCycles, spiIndex);
                                final var sorting = search(spi, parity, spiIndex, maxSymbol, newPi, stack, m);
                                if (!sorting.isEmpty()) {
                                    return stack.toListOfCycles();
                                }
                            }
                        }

                        // ==== ROLLBACK ====
                        for (int l = 0; l < triplet.second.size; l++) {
                            final var cycle = triplet.second.elementData[l];
                            if (cycle.length > 1)
                                spi.remove(cycle);
                        }
                        spi.addAll(triplet.first);
                        updateIndex(spiIndex, parity, triplet.first);
                        // ==============================

                        stack.pop();
                    }
                }
            }
        }

        return EMPTY_LIST;
    }

    public static String canonicalSignature(final UnsafeListOfCycles spi,
                                            final UnsafeByteArray pi,
                                            final UnsafeLongArray spiIndex) {
        var leastHashCode = Integer.MAX_VALUE;
        long canonical = -1;

        for (int symbol : pi) {
            final var shifting = startingBy(pi.getAddress(), symbol);

            var signature = signature(spi, shifting, spiIndex);

            var hashCode = Arrays.hashCode(signature);

            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = signature;
            } else if (hashCode == leastHashCode) {
                canonical = least(signature, pi.len(), canonical);
            }

            if (canonical != signature) {
                free(signature);
            }

            final var mirroredSignature = signature.clone();
            ArrayUtils.reverse(mirroredSignature);

            final var labelLabelMapping = TheUnsafe.get().allocateMemory(spi.len());
            final var orientedIndexMapping = TheUnsafe.get().allocateMemory((long) spi.len() * 8);;
            final var deltas = TheUnsafe.get().allocateMemory((long) spi.len() * 4);

            var nextLabel = 1;
            for (int j = 0; j < mirroredSignature.length; j++) {
                final var label = mirroredSignature[j];

                if (labelLabelMapping[(int) label] == 0) {
                    labelLabelMapping[(int) label] = nextLabel++;
                }

                final var newLabel = labelLabelMapping[(int) label];

                if (label % 1 > 0) {
                    if (orientedIndexMapping[newLabel] == null) {
                        final var index = Math.abs(j - shifting.length) - 1;
                        var cycle = startingBy(spiIndex.get(shifting[index]], shifting[index]).clone();
                        ArrayUtils.reverse(cycle);
                        orientedIndexMapping[newLabel] = cycleIndex(cycle);
                        final var delta = cycle.length - round((label % 1) * 100);
                        deltas[newLabel] = delta;
                    }

                    final var index = Math.abs(j - shifting.length) - 1;
                    final var orientationIndex = orientedIndexMapping[newLabel][shifting[index]] + 1;
                    mirroredSignature[j] = newLabel + (((orientationIndex + deltas[newLabel]) % spiIndex.get(shifting[index]].length) / 100);
                    if (mirroredSignature[j] % 1 == 0)
                        mirroredSignature[j] = newLabel + (spiIndex.get(shifting[index]].length / 100f);
                } else {
                    mirroredSignature[j] = newLabel;
                }
            }

            free(orientedIndexMapping);
            free(deltas);
            free(labelLabelMapping);

            hashCode = Arrays.hashCode(mirroredSignature);
            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = mirroredSignature;
            } else if (hashCode == leastHashCode) {
                canonical = least(mirroredSignature, canonical);
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

    private static float round(final float value) {
        return (float) Math.round(value * 100) / 100;
    }

    private static long least(final long signatureAddress, final byte len, final long canonicalAddress) {
        for (int i = 0; i < len; i++) {
            if (UnsafeFloatArray.at(signatureAddress, i) != UnsafeFloatArray.at(canonicalAddress, i)) {
                if (UnsafeFloatArray.at(signatureAddress, i) < UnsafeFloatArray.at(canonicalAddress, i))
                    return signatureAddress;
                else
                    return canonicalAddress;
            }
        }
        return canonicalAddress;
    }

    public static long signature(final UnsafeListOfCycles spi, final long pi, final UnsafeLongArray spiIndex) {
        final var piInverseIndex = getPiInverseIndex(pi);
        final var orientedCycles = orientedCycles(spi, piInverseIndex);

        byte len = len(pi);
        final var orientationByCycle = TheUnsafe.get().allocateMemory(len);

        for (int l = 0; l < orientedCycles.len(); l++) {
            UnsafeBooleanArray.set(orientationByCycle, at(UnsafeLongArray.at(orientedCycles.getAddress(), l),0), true);
        }

        free(piInverseIndex);
        free(orientedCycles.getAddress());

        // Array of floats
        final var labelByCycle = TheUnsafe.get().allocateMemory(len * 4);
        for (int i = 0; i < len; i++) {
            TheUnsafe.get().putFloat(labelByCycle + (i * 4), -1);
        }

        // Array of floats
        final var symbolIndexByOrientedCycle = TheUnsafe.get().allocateMemory(len * 8);

        final var signatureAddress = TheUnsafe.get().allocateMemory(len * 4);

        // Pi index
        final var piIndex = TheUnsafe.get().allocateMemory(len);
        for (byte i = 0; i < len; i++) {
            UnsafeByteArray.set(piIndex, at(pi, i), i);
        }

        var currentLabel = 1f;

        for (var i = 0; i < len; i++) {
            final int symbol = at(pi, i);
            final var cycleAddress = spiIndex.at(symbol);

            if (UnsafeBooleanArray.at(orientationByCycle, at(cycleAddress, 0))) {
                final var symbolIndex = TheUnsafe.get().allocateMemory(len);

                var minIndex = Byte.MAX_VALUE;
                var symbolMinIndex = 0;
                byte cycleLen = len(cycleAddress);
                for (int s = 0; s < cycleLen; s++) {
                    if (UnsafeByteArray.at(piIndex, (byte) s) < minIndex) {
                        minIndex = UnsafeByteArray.at(piIndex, (byte) s);
                        symbolMinIndex = s;
                    }
                }

                for (int j = 0; j < cycleLen; j++) {
                    if (at(cycleAddress, j) == symbolMinIndex) {
                        for (int k = 0; k < cycleLen; k++) {
                            UnsafeByteArray.set(symbolIndex, at(cycleAddress, (j + k) % cycleLen), (byte)(k + 1));
                        }
                        break;
                    }
                }
                UnsafeLongArray.set(symbolIndexByOrientedCycle, at(cycleAddress, 0), symbolIndex);
            }

            final var label = UnsafeFloatArray.at(labelByCycle, at(cycleAddress, 0));

            if (label == -1) {
                UnsafeFloatArray.set(labelByCycle, at(cycleAddress, 0), currentLabel);
                currentLabel++;
            }

            UnsafeFloatArray.set(signatureAddress, at(cycleAddress, i), UnsafeBooleanArray.at(orientationByCycle, at(cycleAddress, 0)) ?
                    label + (float) UnsafeByteArray.at(UnsafeLongArray.at(symbolIndexByOrientedCycle, at(cycleAddress, 0)), symbol) / 100 :
                    label);
        }

        free(labelByCycle);
        free(orientationByCycle);
        free(piIndex);
        for (int i = 0; i < UnsafeLongArray.len(symbolIndexByOrientedCycle); i++) {
            free(UnsafeLongArray.at(symbolIndexByOrientedCycle, i));
        }
        free(symbolIndexByOrientedCycle);

        return signatureAddress;
    }

    private static String toString(final long signature, final byte len) {
        final var builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            final var v = UnsafeFloatArray.at(signature, i);
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
        return (UnsafeByteArray.at(piInverseIndex, a) < UnsafeByteArray.at(piInverseIndex, b) && UnsafeByteArray.at(piInverseIndex, b) < UnsafeByteArray.at(piInverseIndex, c)) ||
                (UnsafeByteArray.at(piInverseIndex, b) < UnsafeByteArray.at(piInverseIndex, c) && UnsafeByteArray.at(piInverseIndex, c) < UnsafeByteArray.at(piInverseIndex, a)) ||
                (UnsafeByteArray.at(piInverseIndex, c) < UnsafeByteArray.at(piInverseIndex, a) && UnsafeByteArray.at(piInverseIndex, a) < UnsafeByteArray.at(piInverseIndex, b));
    }

    private static void update(final UnsafeLongArray spiIndex, final UnsafeBooleanArray parity, final long cycleAddress) {
        byte len = len(cycleAddress);
        if (len == 1) {
            spiIndex.set(at(cycleAddress, 0), -1);
            parity.set(at(cycleAddress, 0), true);
        } else {
            final var p = (len & 1) == 1;
            for (byte k = 0; k < len; k++) {
                spiIndex.set(k, cycleAddress);
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

    public static long getPiInverseIndex(final long pi) {
        final byte len = len(pi);
        final var piInverseIndex = TheUnsafe.get().allocateMemory(len);
        for (byte i = 0; i < len; i++) {
            UnsafeByteArray.set(piInverseIndex, at(pi, len - i - 1), i);
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
                spiIndex.set(at(cycleAddress, 0), -1);
                parity.set(at(cycleAddress, 0), true);
            } else {
                final var p = (len(cycleAddress) & 1) == 1;
                for (byte k = 0; k < len(cycleAddress); k++) {
                    spiIndex.set(k, cycleAddress);
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
            if (UnsafeByteArray.at(indexAddress, at(cycleAddress, i)) >
                UnsafeByteArray.at(indexAddress, at(cycleAddress, nextIndex))) {
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
        if (spiIndex.at(a) == spiIndex.at(c)) {
            a_ = a;
            b_ = c;
            c_ = b;
            numberOfEvenCycles += len(spiIndex.at(a)) & 1;
            numberOfEvenCycles += len(spiIndex.at(b)) & 1;
        } else if (spiIndex.at(a) == spiIndex.at(b)) {
            a_ = b;
            b_ = a;
            c_ = c;
            numberOfEvenCycles += len(spiIndex.at(a)) & 1;
            numberOfEvenCycles += len(spiIndex.at(c)) & 1;
        } else {
            // spi.getCycle(b) == spi.getCycle(c)
            a_ = c;
            b_ = b;
            c_ = a;
            numberOfEvenCycles += len(spiIndex.at(a)) & 1;
            numberOfEvenCycles += len(spiIndex.at(c)) & 1;
        }

        final var index = cycleIndex(spiIndex.at(c_));
        final var cImage = image(index, spiIndex.at(c_), c_);
        final var abCycle = startingBy(spiIndex.at(a_), a_);
        final var cCycle = startingBy(spiIndex.at(c_), cImage);

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
        oldCycles.add(spiIndex.at(a));
        if (!oldCycles.contains(spiIndex.at(b)))
            oldCycles.add(spiIndex.at(b));
        if (!oldCycles.contains(spiIndex.at(c)))
            oldCycles.add(spiIndex.at(c));

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
        final var oldCycle = cycleIndex.at(a);

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

    private static long create(final int length) {
        final var cycleAddress = TheUnsafe.get().allocateMemory(length + 1);
        TheUnsafe.get().putByte(cycleAddress, (byte) length);
        return cycleAddress;
    }

    private static void set(final long cycleAddress, final int i, final byte value) {
        TheUnsafe.get().putByte(cycleAddress + i + 1, value);
    }

    private static byte at(final long cycleAddress, int i) {
        return TheUnsafe.get().getByte(cycleAddress + i + 1);
    }

    private static byte len(final long cycleAddress) {
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
            if (pi.at(i) == a)
                index0 = i;
            if (pi.at(i) == b)
                index1 = i;
            if (pi.at(i) == c)
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

    private static void arraycopy(final long srcAddress, int srcPos, long destAddress, int destPost, int length) {
        TheUnsafe.get().copyMemory(srcAddress + srcPos, destAddress + destPost, length);
    }

    private static void removeTrivialCycles(final ListOfCycles spi) {
        final var toRemove = new ArrayList<int[]>();

        for (int i = 0; i < spi.size; i++) {
            final var cycle = spi.elementData[i];
            if (cycle.length == 1) {
                toRemove.add(cycle);
            }
        }

        spi.removeAll(toRemove);
    }
}
