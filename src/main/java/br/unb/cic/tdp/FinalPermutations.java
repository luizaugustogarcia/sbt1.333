package br.unb.cic.tdp;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.util.ListOfCycles;
import br.unb.cic.tdp.proof.util.Move;
import br.unb.cic.tdp.proof.util.Stack;
import br.unb.cic.tdp.util.Triplet;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static br.unb.cic.tdp.proof.ProofGenerator.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class FinalPermutations {

    private static Cache<String, Set<String>> UNSUCCESSFUL_VISITED_CONFIGS;

    final static AtomicLong hits = new AtomicLong();
    final static AtomicLong misses = new AtomicLong();

    public static void main(String[] args) {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        UNSUCCESSFUL_VISITED_CONFIGS = CacheBuilder.newBuilder()
                .maximumSize(Integer.parseInt(args[0]))
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

        final var timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.gc();
                long heapSize = Runtime.getRuntime().totalMemory();
                long heapFreeSize = Runtime.getRuntime().freeMemory();
                System.out.println("Cache size: " + UNSUCCESSFUL_VISITED_CONFIGS.size() + ", " +
                        "Hit rate: " + String.format("%.2f", 1 - (misses.get() / (float) hits.get())) + ", " +
                        "Heap size GB: " + (((heapSize / 1024) / 1024) / 1024) + ", " +
                        "Heap free size GB: " + (((heapFreeSize / 1024) / 1024) / 1024)
                );
            }
        }, 0, Integer.parseInt(args[1]) * 60 * 1000);

//        Stream.of(
//                new Configuration("(0 16 14)(1 35 15)(2 6 4)(3 7 5)(8 12 10)(9 13 11)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"), //----- NO SORTING - DOUBLE CHECKED
//                new Configuration("(0 34 20)(1 5 3)(2 6 4)(7 11 9)(8 12 10)(13 35 33)(14 18 16)(15 19 17)(21 25 23)(22 26 24)(27 31 29)(28 32 30)"), //----- NO SORTING - DOUBLE CHECKED
//                new Configuration("(0 4 2)(1 35 3)(5 9 7)(6 10 8)(11 15 13)(12 28 14)(16 20 18)(17 21 19)(22 26 24)(23 27 25)(29 33 31)(30 34 32)"), //----- NO SORTING - DOUBLE CHECKED
//                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(18 22 20)(19 23 21)(24 34 26)(25 35 33)(27 31 29)(28 32 30)"), //----- NO SORTING
//                new Configuration("(0 34 26)(1 35 33)(2 6 4)(3 7 5)(8 12 10)(9 13 11)(14 18 16)(15 19 17)(20 24 22)(21 25 23)(27 31 29)(28 32 30)"), //----- NO SORTING
//                new Configuration("(0 34 2)(1 35 33)(3 7 5)(4 8 6)(9 19 17)(10 14 12)(11 15 13)(16 26 18)(20 24 22)(21 25 23)(27 31 29)(28 32 30)"), //----- NO SORTING
//                new Configuration("(0 34 32)(1 29 27)(2 30 28)(3 7 5)(4 8 6)(9 13 11)(10 14 12)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(31 35 33)"), //----- NO SORTING
//                new Configuration("(0 16 14)(1 5 3)(2 6 4)(7 11 9)(8 12 10)(13 35 15)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"), //----- NO SORTING
//                new Configuration("(0 34 2)(1 35 33)(3 7 5)(4 8 6)(9 13 11)(10 14 12)(15 25 17)(16 32 18)(19 23 21)(20 24 22)(26 30 28)(27 31 29)"), //----- NO SORTING
//                new Configuration("(0 34 32)(1 35 33)(2 6 4)(3 7 5)(8 12 10)(9 13 11)(14 18 16)(15 19 17)(20 24 22)(21 25 23)(26 30 28)(27 31 29)"), //----- NO SORTING
//                new Configuration("(0 34 32)(1 29 27)(2 6 4)(3 7 5)(8 12 10)(9 13 11)(14 30 28)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(31 35 33)") //----- NO SORTING
//        ).forEach(conf -> {
//            sort(conf, "C:/Users/Luiz/Temp/sbt1.333proof", _16_12_SEQS);
//        });

//        System.out.println("7 interleaving (16,12)");
//        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)"),
//                "C:/Users/Luiz/Temp/sbt1.333proof", _16_12_SEQS); //----- NO SORTING

        System.out.println("7 interleaving (19,14)");
        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)"),
                "C:/Users/Luiz/Temp/sbt1.333proof", _19_14_SEQS);

        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)"),
                "C:/Users/Luiz/Temp/sbt1.333proof", _16_12_SEQS);

        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)"),
                "C:/Users/Luiz/Temp/sbt1.333proof", _19_14_SEQS);

        sort(new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)"),
                "C:/Users/Luiz/Temp/sbt1.333proof", _20_15_SEQS);

        timer.cancel();
    }

    @SneakyThrows
    public static void sort(final Configuration configuration,
                            final String outputDir,
                            final Move root) {
        hits.set(0);
        misses.set(0);
        UNSUCCESSFUL_VISITED_CONFIGS.invalidateAll();

        final var canonical = configuration.getCanonical();

        final var sortingFile = new File(outputDir + "/comb/" + canonical.getSpi() + ".html");
        if (sortingFile.exists()) {
            System.out.println("Skipping " + configuration.getSpi());
            return;
        }

        System.out.println("Sorting " + configuration.getSpi());

        final var hasSorting = new boolean[1];
        final var forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        final var task = new Search(configuration, outputDir,
                  toListOfCycle(configuration.getSpi(), configuration.getPi()), configuration.getPi().getSymbols(),
                new Stack(root.getHeight()), root, forkJoinPool, hasSorting);
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
        final Move root;
        final ForkJoinPool forkJoinPool;
        boolean[] hasSorting;

        public Search(final Configuration configuration, final String outputDir, final ListOfCycles spi,
                      final int[] pi, final Stack stack, final Move root, final ForkJoinPool forkJoinPool,
                      final boolean[] hasSorting) {
            this.configuration = configuration;
            this.outputDir = outputDir;
            this.spi = spi;
            this.pi = pi;
            this.stack = stack;
            this.root = root;
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
                    spiIndex[s] = cycle;
                    parity[s] = (cycle.length & 1) == 1;
                }
            }

            removeTrivialCycles(spi);

            // if passed through three zeros moves, COMPUTE DIRECTLY
            if (root.numberOfZeroMovesUntilTop() > 3) {
                final var sorting = search(spi, parity, spiIndex, spiIndex.length, newPi, stack, root);
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
                if (root.mu == 0) {
                    final var key = canonicalSignature(spi, pi, spiIndex, spiIndex.length);
                    final var paths = UNSUCCESSFUL_VISITED_CONFIGS.get(key, () -> new HashSet(1));
                    synchronized (paths) {
                        if (paths.isEmpty()) {
                            misses.incrementAndGet();
                            paths.add(root.pathToRoot());
                        } else {
                            if (paths.contains(root.pathToRoot())) {
                                hits.incrementAndGet();
                                return;
                            } else {
                                paths.add(root.pathToRoot());
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
                                if (aCycle.length > 1) spi.add(aCycle); else numberOfTrivialCycles++;
                                if (bCycle.length > 1) spi.add(bCycle); else numberOfTrivialCycles++;
                                if (cCycle.length > 1) spi.add(cCycle); else numberOfTrivialCycles++;
                                update(spiIndex, parity, aCycle, bCycle, cCycle);
                                // =======================

                                for (final var nextMove : root.children) {
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
                        if (spiIndex[a] == spiIndex[b] && spiIndex[b] == spiIndex[c])
                            continue;

                        final var is_2Move = spiIndex[a] != spiIndex[b] &&
                                spiIndex[b] != spiIndex[c] &&
                                spiIndex[a] != spiIndex[c];
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

                        for (final var nextMove : root.children) {
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

                        final var is_2Move = spiIndex[a] != spiIndex[b] &&
                                spiIndex[b] != spiIndex[c] &&
                                spiIndex[a] != spiIndex[c];
                        if (is_2Move)
                            continue;

                        final Triplet<ListOfCycles, ListOfCycles, Integer> triplet;
                        // if it's the same cycle
                        if (spiIndex[a] == spiIndex[b] && spiIndex[b] == spiIndex[c]) {
                            final var cycle = spiIndex[a];

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
                            for (final var nextMove : root.children) {
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
    public static ListOfCycles search(final ListOfCycles spi,
                                      final boolean[] parity,
                                      final int[][] spiIndex,
                                      final int maxSymbol,
                                      final int[] pi,
                                      final Stack stack,
                                      final Move root) {
        if (Thread.currentThread().isInterrupted()) {
            return ListOfCycles.EMPTY_LIST;
        }

        if (root.mu == 0) {
            final var key = canonicalSignature(spi, pi, spiIndex, maxSymbol);
            final var paths = UNSUCCESSFUL_VISITED_CONFIGS.get(key, () -> new HashSet(1));
            synchronized (paths) {
                if (paths.isEmpty()) {
                    misses.incrementAndGet();
                    paths.add(root.pathToRoot());
                } else {
                    if (paths.contains(root.pathToRoot())) {
                        hits.incrementAndGet();
                        return ListOfCycles.EMPTY_LIST;
                    } else {
                        paths.add(root.pathToRoot());
                    }
                }
            }

            final var sorting = analyze0Moves(spi, parity, spiIndex, maxSymbol, pi, stack, root);
            if (!sorting.isEmpty()) {
                return sorting;
            }
        } else {
            var sorting = analyzeOrientedCycles(spi, parity, spiIndex, maxSymbol, pi, stack, root);
            if (!sorting.isEmpty()) {
                return sorting;
            }

            sorting = analyzeOddCycles(spi, parity, spiIndex, maxSymbol, pi, stack, root);
            if (!sorting.isEmpty()) {
                return sorting;
            }
        }

        return ListOfCycles.EMPTY_LIST;
    }

    private static ListOfCycles analyzeOddCycles(final ListOfCycles spi,
                                                 final boolean[] parity,
                                                 final int[][] spiIndex,
                                                 final int maxSymbol,
                                                 final int[] pi,
                                                 final Stack stack,
                                                 final Move root) {
        for (int i = 0; i < pi.length - 2; i++) {
            if (parity[pi[i]]) continue;
            for (int j = i + 1; j < pi.length - 1; j++) {
                if (parity[pi[j]]) continue;
                for (int k = j + 1; k < pi.length; k++) {
                    if (parity[pi[k]]) continue;

                    int a = pi[i], b = pi[j], c = pi[k];

                    // if it's the same cycle, skip it
                    if (spiIndex[a] == spiIndex[b] && spiIndex[b] == spiIndex[c])
                        continue;

                    final var is_2Move = spiIndex[a] != spiIndex[b] &&
                            spiIndex[b] != spiIndex[c] &&
                            spiIndex[a] != spiIndex[c];
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

        return ListOfCycles.EMPTY_LIST;
    }

    public static String canonicalSignature(final ListOfCycles spi,
                                            final int[] pi,
                                            final int[][] spiIndex,
                                            final int maxSymbol) {
        var leastHashCode = Integer.MAX_VALUE;
        float[] canonical = null;

        for (int symbol : pi) {
            final var shifting = startingBy(pi, symbol);

            var signature = signature(spi, shifting, spiIndex, maxSymbol);

            var hashCode = Arrays.hashCode(signature);

            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = signature;
            } else if (hashCode == leastHashCode) {
                canonical = least(signature, canonical);
            }

            final var mirroredSignature = signature.clone();
            ArrayUtils.reverse(mirroredSignature);

            final var labelLabelMapping = new int[spi.size + 1];
            final var orientedIndexMapping = new int[spi.size + 1][];
            final var deltas = new float[spi.size + 1];

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
                        var cycle = startingBy(spiIndex[shifting[index]], shifting[index]).clone();
                        ArrayUtils.reverse(cycle);
                        orientedIndexMapping[newLabel] = cycleIndex(cycle);
                        final var delta = cycle.length - round((label % 1) * 100);
                        deltas[newLabel] = delta;
                    }

                    final var index = Math.abs(j - shifting.length) - 1;
                    final var orientationIndex = orientedIndexMapping[newLabel][shifting[index]] + 1;
                    mirroredSignature[j] = newLabel + (((orientationIndex + deltas[newLabel]) % spiIndex[shifting[index]].length) / 100);
                    if (mirroredSignature[j] % 1 == 0)
                        mirroredSignature[j] = newLabel + (spiIndex[shifting[index]].length / 100f);
                } else {
                    mirroredSignature[j] = newLabel;
                }
            }

            hashCode = Arrays.hashCode(mirroredSignature);
            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = signature;
            } else if (hashCode == leastHashCode) {
                canonical = least(signature, canonical);
            }
        }

        return toString(canonical);
    }

    private static float round(final float value) {
        return (float) Math.round(value * 100) / 100;
    }

    private static float[] least(final float[] signature, final float[] canonical) {
        for (int i = 0; i < signature.length; i++) {
            if (signature[i] != canonical[i]) {
                if (signature[i] < canonical[i])
                    return signature;
                else
                    return canonical;
            }
        }
        return canonical;
    }

    public static float[] signature(final ListOfCycles spi, final int[] pi, final int[][] spiIndex, final int maxSymbol) {
        final var piInverseIndex = getPiInverseIndex(pi, maxSymbol);
        final var orientedCycles = orientedCycles(spi, piInverseIndex);

        final var orientationByCycle = new boolean[maxSymbol + 1];
        Arrays.fill(orientationByCycle, false);

        for (int l = 0; l < orientedCycles.size; l++) {
            orientationByCycle[orientedCycles.elementData[l][0]] = true;
        }

        final var labelByCycle = new float[maxSymbol + 1];
        Arrays.fill(labelByCycle, -1);

        final var symbolIndexByOrientedCycle = new int[maxSymbol + 1][];

        final var signature = new float[pi.length];

        // Pi index
        final var piIndex = new int[maxSymbol + 1];
        for (var i = 0; i < pi.length; i++) {
            piIndex[pi[i]] = i;
        }

        var currentLabel = 1f;

        for (var i = 0; i < signature.length; i++) {
            final int symbol = pi[i];
            final var cycle = spiIndex[symbol];

            if (orientationByCycle[cycle[0]]) {
                final var symbolIndex = new int[maxSymbol + 1];

                var minIndex = Integer.MAX_VALUE;
                var symbolMinIndex = 0;
                for (int s : cycle) {
                    if (piIndex[s] < minIndex) {
                        minIndex = piIndex[s];
                        symbolMinIndex = s;
                    }
                }

                for (int j = 0; j < cycle.length; j++) {
                    if (cycle[j] == symbolMinIndex) {
                        for (int k = 0; k < cycle.length; k++) {
                            symbolIndex[cycle[(j + k) % cycle.length]] = k + 1;
                        }
                        break;
                    }
                }
                symbolIndexByOrientedCycle[cycle[0]] = symbolIndex;
            }

            if (labelByCycle[cycle[0]] == -1) {
                labelByCycle[cycle[0]] = currentLabel;
                currentLabel++;
            }

            signature[i] = orientationByCycle[cycle[0]] ?
                    labelByCycle[cycle[0]] + (float) symbolIndexByOrientedCycle[cycle[0]][symbol] / 100 : labelByCycle[cycle[0]];
        }

        return signature;
    }

    private static String toString(final float[] signature) {
        final var builder = new StringBuilder();
        for (float v : signature) {
            if (v % 1 == 0) {
                builder.append((int) v);
            } else {
                builder.append(v);
            }
            builder.append(',');
        }
        return builder.toString();
    }

    private static ListOfCycles analyzeOrientedCycles(final ListOfCycles spi,
                                                      final boolean[] parity,
                                                      final int[][] spiIndex,
                                                      final int maxSymbol,
                                                      final int[] pi,
                                                      final Stack stack,
                                                      final Move root) {
        final var piInverseIndex = getPiInverseIndex(pi, maxSymbol);

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
                            if (aCycle.length > 1) spi.add(aCycle); else numberOfTrivialCycles++;
                            if (bCycle.length > 1) spi.add(bCycle); else numberOfTrivialCycles++;
                            if (cCycle.length > 1) spi.add(cCycle); else numberOfTrivialCycles++;
                            update(spiIndex, parity, aCycle, bCycle, cCycle);
                            // =======================

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

        return ListOfCycles.EMPTY_LIST;
    }

    private static boolean areSymbolsInCyclicOrder(final int[] piInverseIndex, final int a, final int b, final int c) {
        return (piInverseIndex[a] < piInverseIndex[b] && piInverseIndex[b] < piInverseIndex[c]) ||
                (piInverseIndex[b] < piInverseIndex[c] && piInverseIndex[c] < piInverseIndex[a]) ||
                (piInverseIndex[c] < piInverseIndex[a] && piInverseIndex[a] < piInverseIndex[b]);
    }

    private static ListOfCycles analyze0Moves(final ListOfCycles spi,
                                              final boolean[] parity,
                                              final int[][] spiIndex,
                                              final int maxSymbol,
                                              final int[] pi,
                                              final Stack stack,
                                              final Move root) {
        final var cycleIndexes = new int[maxSymbol + 1][];

        for (int i = 0; i < pi.length - 2; i++) {
            for (int j = i + 1; j < pi.length - 1; j++) {
                for (int k = j + 1; k < pi.length; k++) {

                    int a = pi[i], b = pi[j], c = pi[k];

                    final var is_2Move = spiIndex[a] != spiIndex[b] &&
                            spiIndex[b] != spiIndex[c] &&
                            spiIndex[a] != spiIndex[c];
                    if (is_2Move)
                        continue;

                    final Triplet<ListOfCycles, ListOfCycles, Integer> triplet;
                    // if it's the same cycle
                    if (spiIndex[a] == spiIndex[b] && spiIndex[b] == spiIndex[c]) {
                        final var cycle = spiIndex[a];

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

        return ListOfCycles.EMPTY_LIST;
    }

    private static void update(final int[][] index, final boolean[] parity, final int[]... cycles) {
        for (int[] cycle : cycles) {
            final var p = (cycle.length & 1) == 1;
            for (int k : cycle) {
                index[k] = cycle;
                parity[k] = p;
            }
        }
    }

    private static int[] getPiInverseIndex(final int[] pi, final int maxSymbol) {
        final var piInverseIndex = new int[maxSymbol + 1];
        for (var i = 0; i < pi.length; i++) {
            piInverseIndex[pi[pi.length - i - 1]] = i;
        }
        return piInverseIndex;
    }

    private static ListOfCycles orientedCycles(final ListOfCycles spi, final int[] piInverseIndex) {
        final var orientedCycles = new ListOfCycles(2);
        for (int i = 0; i < spi.size; i++) {
            final int[] cycle = spi.elementData[i];
            if (!areSymbolsInCyclicOrder(piInverseIndex, cycle))
                orientedCycles.add(cycle);
        }
        return orientedCycles;
    }

    private static void updateIndex(final int[][] index, final boolean[] parity, final ListOfCycles cycles) {
        for (int i = 0; i < cycles.size; i++) {
            final var cycle = cycles.elementData[i];
            updateIndex(index, parity, cycle);
        }
    }

    private static void updateIndex(final int[][] index, final boolean[] parity, final int[]... cycles) {
        for (int[] cycle : cycles) {
            final var p = (cycle.length & 1) == 1;
            for (int k : cycle) {
                index[k] = cycle;
                parity[k] = p;
            }
        }
    }

    private static boolean areSymbolsInCyclicOrder(final int[] index, int[] symbols) {
        boolean leap = false;
        for (int i = 0; i < symbols.length; i++) {
            int nextIndex = i + 1;
            if (nextIndex >= symbols.length)
                nextIndex = (i + 1) % symbols.length;
            if (index[symbols[i]] > index[symbols[nextIndex]]) {
                if (!leap) {
                    leap = true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    private static Triplet<ListOfCycles, ListOfCycles, Integer> simulate0MoveTwoCycles(final int[][] spiIndex,
                                                                                       final int a,
                                                                                       final int b,
                                                                                       final int c) {
        int numberOfEvenCycles = 0;
        int a_, b_, c_;
        if (spiIndex[a] == spiIndex[c]) {
            a_ = a;
            b_ = c;
            c_ = b;
            numberOfEvenCycles += spiIndex[a].length & 1;
            numberOfEvenCycles += spiIndex[b].length & 1;
        } else if (spiIndex[a] == spiIndex[b]) {
            a_ = b;
            b_ = a;
            c_ = c;
            numberOfEvenCycles += spiIndex[a].length & 1;
            numberOfEvenCycles += spiIndex[c].length & 1;
        } else {
            // spi.getCycle(b) == spi.getCycle(c)
            a_ = c;
            b_ = b;
            c_ = a;
            numberOfEvenCycles += spiIndex[a].length & 1;
            numberOfEvenCycles += spiIndex[c].length & 1;
        }

        final var index = cycleIndex(spiIndex[c_]);
        final var cImage = image(index, spiIndex[c_], c_);
        final var abCycle = startingBy(spiIndex[a_], a_);
        final var cCycle = startingBy(spiIndex[c_], cImage);

        final var abCycleIndex = cycleIndex(abCycle);

        final var ba_k = getK(abCycleIndex, abCycle, b_, a_);
        final var newaCycle = new int[1 + ba_k - 1];
        newaCycle[0] = a_;
        final var ab_k = getK(abCycleIndex, abCycle, a_, b_);
        System.arraycopy(abCycle,  ab_k + 1, newaCycle, 1, ba_k - 1);

        final var newbCycle = new int[1 + cCycle.length + (ab_k - 1)];
        newbCycle[0] = b_;
        System.arraycopy(cCycle, 0, newbCycle, 1, cCycle.length);
        System.arraycopy(abCycle, 1, newbCycle, 1 + cCycle.length, ab_k - 1);

        var newNumberOfEvenCycles = 0;
        newNumberOfEvenCycles += newaCycle.length & 1;
        newNumberOfEvenCycles += newbCycle.length & 1;

        final var oldCycles = new ListOfCycles(2);
        oldCycles.add(spiIndex[a]);
        if (!oldCycles.contains(spiIndex[b]))
            oldCycles.add(spiIndex[b]);
        if (!oldCycles.contains(spiIndex[c]))
            oldCycles.add(spiIndex[c]);

        final var newCycles = new ListOfCycles(2);
        newCycles.add(newaCycle);
        newCycles.add(newbCycle);

        return new Triplet<>(oldCycles, newCycles, newNumberOfEvenCycles - numberOfEvenCycles);
    }

    private static int image(int[] index, int[] cycle, int a) {
        return cycle[(index[a] + 1) % cycle.length];
    }

    private static Triplet<ListOfCycles, ListOfCycles, Integer> simulate0MoveSameCycle(final int[][] cycleIndex,
                                                                                       final int a,
                                                                                       final int b,
                                                                                       final int c) {
        final var oldCycle = cycleIndex[a];

        final int[] symbols = startingBy(oldCycle, b);
        final var newCycle = new int[oldCycle.length];

        final int[] oldCycleIndex = cycleIndex(oldCycle);

        newCycle[0] = b;
        final var ab_k = getK(oldCycleIndex, oldCycle, b, a);
        final var bc_k = getK(oldCycleIndex, oldCycle, a, c);
        System.arraycopy(symbols, ab_k + 1, newCycle, 1, bc_k - 1);
        newCycle[bc_k] = c;

        System.arraycopy(symbols, 1, newCycle, 1 + bc_k, ab_k - 1);
        newCycle[ab_k + bc_k] = a;

        final var ca_k = getK(oldCycleIndex, oldCycle, c, b);
        System.arraycopy(symbols, ab_k + bc_k + 1,
                newCycle, ab_k + bc_k + 1, ca_k - 1);

        return new Triplet<>(ListOfCycles.singleton(oldCycle), ListOfCycles.singleton(newCycle), 0);
    }

    private static int[] cycleIndex(int[] cycle) {
        final var index = new int[max(cycle) + 1];

        for (int i = 0; i < cycle.length; i++) {
            index[cycle[i]] = i;
        }

        return index;
    }

    public static int max(final int[] array) {
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    private static int getK(int[] cycleIndex, int[] cycle, int a, int b) {
        final var aIndex = cycleIndex[a];
        final var bIndex = cycleIndex[b];

        if (bIndex >= aIndex)
            return bIndex - aIndex;

        return (cycle.length - aIndex) + bIndex;
    }

    private static int[] startingBy(int[] symbols, int a) {
        if (symbols[0] == a)
            return symbols;

        final var result = new int[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            if (symbols[i] == a) {
                System.arraycopy(symbols, i, result, 0, symbols.length - i);
                System.arraycopy(symbols, 0, result, symbols.length - i, i);
                break;
            }
        }

        return result;
    }

    private static int[] applyTransposition(final int[] pi,
                                            final int a,
                                            final int b,
                                            final int c,
                                            final int numberOfSymbols,
                                            final int[][] spiIndex) {
        final var indexes = new int[3];
        Arrays.fill(indexes, -1);

        for (var i = 0; i < pi.length; i++) {
            if (pi[i] == a)
                indexes[0] = i;
            if (pi[i] == b)
                indexes[1] = i;
            if (pi[i] == c)
                indexes[2] = i;

            if (indexes[0] != -1 && indexes[1] != -1 && indexes[2] != -1)
                break;
        }

        // sort indexes - this is CPU efficient
        if (indexes[0] > indexes[2]) {
            final var temp = indexes[0];
            indexes[0] = indexes[2];
            indexes[2] = temp;
        }

        if (indexes[0] > indexes[1]) {
            final var temp = indexes[0];
            indexes[0] = indexes[1];
            indexes[1] = temp;
        }

        if (indexes[1] > indexes[2]) {
            final var temp = indexes[1];
            indexes[1] = indexes[2];
            indexes[2] = temp;
        }

        final var result = new int[numberOfSymbols];

        int counter = 0;
        for (int i = 0; i < indexes[0]; i++) {
            if (spiIndex[pi[i]].length == 1) continue;
            result[counter] = pi[i];
            counter++;
        }

        for (int i = 0; i < indexes[2] - indexes[1]; i++) {
            if (spiIndex[pi[indexes[1] + i]].length == 1) continue;
            result[counter] = pi[indexes[1] + i];
            counter++;
        }

        for (int i = 0; i < indexes[1] - indexes[0]; i++) {
            if (spiIndex[pi[indexes[0] + i]].length == 1) continue;
            result[counter] = pi[indexes[0] + i];
            counter++;
        }

        for (int i = 0; i < pi.length - indexes[2]; i++) {
            if (spiIndex[pi[indexes[2] + i]].length == 1) continue;
            result[counter] = pi[indexes[2] + i];
            counter++;
        }

        return result;
    }

    private static void removeTrivialCycles(final ListOfCycles spi) {
        final var toRemove = new ArrayList<int[]>();
        var removed = 0;
        for (int i = 0; i < spi.size; i++) {
            final var cycle = spi.elementData[i];
            if (cycle.length == 1) {
                toRemove.add(cycle);
                removed++;
            }
        }

        spi.removeAll(toRemove);
    }
}
