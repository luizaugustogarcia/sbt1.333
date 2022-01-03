package br.unb.cic.tdp;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.proof.ProofGenerator;
import br.unb.cic.tdp.util.Pair;
import br.unb.cic.tdp.util.Triplet;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.Ints;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.velocity.app.Velocity;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static br.unb.cic.tdp.base.CommonOperations.*;
import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;
import static br.unb.cic.tdp.proof.ProofGenerator.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

public class FinalPermutations {

    final static Cache<String, String[]> UNSUCCESSFUL_CONFIGS = CacheBuilder.newBuilder()
            .maximumSize(120_000_000)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .build();

    final static AtomicLong hits = new AtomicLong();
    final static AtomicLong misses = new AtomicLong();

    public static void main(String[] args) {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        final var timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("Cache size: " + UNSUCCESSFUL_CONFIGS.size());
                System.out.println("Cache hits: " + hits);
                System.out.println("Cache misses: " + misses);
                long heapSize = Runtime.getRuntime().totalMemory();
                System.out.println("Heap size GB: " + (((heapSize / 1024) / 1024) / 1024));
                long heapFreeSize = Runtime.getRuntime().freeMemory();
                System.out.println("Heap free size GB: " + (((heapFreeSize / 1024) / 1024) / 1024));
            }
        }, 0, 10 * 60 * 1000);

//        Stream.of(
//                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(24 34 26)(25 35 27)(28 32 30)(29 33 31)(18 22 20)(19 23 21)"), // bad
//                new Configuration("(0 4 2)(1 35 3)(5 9 7)(6 10 8)(11 15 13)(12 16 14)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"), // bad
//                new Configuration("(0 4 2)(1 5 3)(6 16 14)(7 11 9)(8 12 10)(13 35 15)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
//                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(18 34 32)(19 35 33)(20 24 22)(21 25 23)(26 30 28)(27 31 29)"),
//                new Configuration("(0 4 2)(1 5 3)(12 34 14)(13 35 33)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(27 31 29)(28 32 30)(6 10 8)(7 11 9)"),
//                new Configuration("(0 4 2)(1 5 3)(6 34 8)(7 35 33)(9 13 11)(10 14 12)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(27 31 29)(28 32 30)"),
//                new Configuration("(0 4 2)(1 5 3)(6 16 8)(7 35 9)(10 14 12)(11 15 13)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
//                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 22 20)(13 17 15)(14 18 16)(19 35 21)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
//                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 28 14)(13 35 15)(16 20 18)(17 21 19)(29 33 31)(30 34 32)(22 26 24)(23 27 25)"),
//                new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)")
//        ).forEach(conf -> sort(conf, "/home/luiskowada/proof1.333", _16_12_SEQS));

//        Stream.of(
//                new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)") // bad
//        ).forEach(conf -> sort(conf, "/home/luiskowada/proof1.333", _16_12_SEQS));
//
//        unsuccessfulConfigs.cleanUp();
//

        Stream.of(
                new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)")
        ).forEach(conf -> sort(conf, "/home/ubuntu/sbt", _19_14_SEQS));

        UNSUCCESSFUL_CONFIGS.cleanUp();

        Stream.of(
                new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)")
        ).forEach(conf -> sort(conf, "/home/ubuntu/sbt", _19_14_SEQS));

        UNSUCCESSFUL_CONFIGS.cleanUp();

        Stream.of(
                new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)")
        ).forEach(conf -> sort(conf, "/home/ubuntu/sbt", _20_15_SEQS));

        UNSUCCESSFUL_CONFIGS.cleanUp();

        Stream.of(
                new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)(42,46,44)(43,47,45)(48,52,50)(49,53,51)")
        ).forEach(conf -> sort(conf, "/home/ubuntu/sbt", _24_18_SEQS));

        timer.cancel();
    }

    @SneakyThrows
    public static void sort(final Configuration configuration, String outputDir, ProofGenerator.Move rootMove) {
        //Set<String> badCases = loadBadCases(outputDir);

        final var canonical = configuration.getCanonical();

        final var sortingFile = new File(outputDir + "/comb/" + canonical.getSpi() + ".html");
        if (sortingFile.exists()) {
            System.out.println("Skipping " + configuration.getSpi());
            return;
        }

        System.out.println("Sorting " + configuration.getSpi());

        var list = CommonOperations.generateAll0And2Moves(configuration.getSpi(), configuration.getPi())
                .filter(p -> p.getSecond() == 0).map(Pair::getFirst).collect(toList());

        System.out.println(list.size() + " 0-moves");

        Collections.shuffle(list);

        final var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final var completionService = new ExecutorCompletionService<List<int[]>>(executorService);

        final var submittedTasks = new ArrayList<Future<List<int[]>>>();

        list.forEach(move -> {
            for (final var root : rootMove.children) {
                //if (badCases.contains(configuration.getSpi() + "-" + move + "-" + root.getMu())) continue;

                submittedTasks.add(completionService.submit(() -> {
                    final var name = Thread.currentThread().getName();
                    Thread.currentThread().setName(Thread.currentThread().getName() + "-" + move + "-" + root.mu);

                    final var partialSorting = new ListOfCycles();
                    partialSorting.push(move.getSymbols());

                    final var spi = new ListOfCycles();
                    computeProduct(configuration.getSpi(), move.getInverse())
                            .stream().map(Cycle::getSymbols).forEach(spi::add);

                    final var parity = new boolean[configuration.getPi().size()];
                    int[][] spiIndex = new int[configuration.getPi().size()][];
                    var current = spi.head;
                    while (current != null) {
                        final var cycle = current.data;
                        for (int i : cycle) {
                            spiIndex[i] = cycle;
                            parity[i] = (cycle.length & 1) == 1;
                        }
                        current = current.next;
                    }

                    final var removed = removeTrivialCycles(spi);

                    final var pi = applyTransposition(configuration.getPi().getSymbols(), move.getSymbols(), configuration.getPi().size() - removed, spiIndex);

                    final var maxSymbol = Ints.max(pi);

                    try {
                        final var sorting = search(spi, parity, spiIndex, maxSymbol, pi, partialSorting, root);
                        if (sorting.isEmpty() && !Thread.currentThread().isInterrupted()) {
                            System.out.println(move + ", branch " + root.mu + " unsuccessful");
                        }
                        return sorting.toList();
                    } finally {
                        Thread.currentThread().setName(name);
                    }
                }));
            }
        });

        executorService.shutdown();

        boolean hasSorting = false;
        for (int i = 0; i < submittedTasks.size(); i++) {
            final var sorting = completionService.take();
            if (sorting.get().size() > 0) {
                final var s = sorting.get().stream().map(Cycle::create).collect(toList());
                boolean is16_12 = is16_12(configuration.getSpi(), configuration.getPi(), s);
                if (is16_12) {
                    hasSorting = true;
                    executorService.shutdownNow();
                    System.out.println("Sorted: " + configuration.getSpi() + ", sorting: " + sorting.get().stream().map(Arrays::toString).collect(joining(",")) + ", is 16/12: " + is16_12);
                    System.out.println();
//                    try (final var out = new FileWriter(outputDir + "/comb/" + canonical.getSpi() + ".html")) {
//                        renderSorting(canonical, canonical.translatedSorting(configuration, s), out);
//                    }
                    break;
                }
            }
        }

        if (!hasSorting)
            System.out.println("Didn't find sorting for " + configuration.getSpi());
    }

    @SneakyThrows
    private static Set<String> loadBadCases(String outputDir) {
        final var result = new HashSet<String>();

        try (final var reader = new BufferedReader(new FileReader(outputDir + "/bad-cases.txt"))) {
            var line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        }

        return result;
    }

    private static int removeTrivialCycles(ListOfCycles spi) {
        final var toRemove = new ArrayList<int[]>();
        var removed = 0;
        for (var current = spi.head; current != null; current = current.next) {
            final var cycle = current.data;
            if (cycle.length == 1) {
                toRemove.add(cycle);
                removed++;
            }
        }

        spi.removeAll(toRemove);
        return removed;
    }

    static AtomicLong total = new AtomicLong();
    static AtomicLong times = new AtomicLong();

    @SneakyThrows
    public static ListOfCycles search(final ListOfCycles spi,
                                      final boolean[] parity,
                                      final int[][] spiIndex,
                                      final int maxSymbol,
                                      final int[] pi,
                                      final ListOfCycles moves,
                                      final Move root) {
        if (Thread.currentThread().isInterrupted()) {
            return ListOfCycles.EMPTY_LIST;
        }

        String key = null;
        String[] paths = null;

        if (moves.size <= 12) {
            key = getCanonicalSignature(spi, pi, spiIndex, maxSymbol);

            paths = UNSUCCESSFUL_CONFIGS.getIfPresent(key);

            if (paths != null && contains(paths, root.pathToRoot())) {
                hits.incrementAndGet();
                return ListOfCycles.EMPTY_LIST;
            } else {
                misses.incrementAndGet();
            }
        }

        if (root.mu == 0) {
            final var sorting = analyze0Moves(spi, parity, spiIndex, maxSymbol, pi, moves, root);
            if (!sorting.isEmpty()) {
                return sorting;
            }
        } else {
            var sorting = analyzeOrientedCycles(spi, parity, spiIndex, maxSymbol, pi, moves, root);
            if (!sorting.isEmpty()) {
                return sorting;
            }

            sorting = analyzeOddCycles(spi, parity, spiIndex, maxSymbol, pi, moves, root);
            if (!sorting.isEmpty()) {
                return sorting;
            }
        }

        if (moves.size <= 12) {
            if (UNSUCCESSFUL_CONFIGS.getIfPresent(key) == null) {
                UNSUCCESSFUL_CONFIGS.put(key, new String[]{root.pathToRoot()});
            } else {
                UNSUCCESSFUL_CONFIGS.put(key, ArrayUtils.add(paths, root.pathToRoot()));
            }
        }

        return ListOfCycles.EMPTY_LIST;
    }

    private static boolean contains(final String[] array, final String string) {
        for (String s : array) {
            if (s.equals(string))
                return true;
        }
        return false;
    }

    private static String getCanonicalSignature(final ListOfCycles spi,
                                                final int[] pi,
                                                final int[][] spiIndex,
                                                final int maxSymbol) throws IOException {
        var leastHashCode = Integer.MAX_VALUE;
        float[] canonical = null;

        for (int symbol : startingBy(pi, maxSymbol)) {
            final var shifting = startingBy(pi, symbol);

            var signature = signature(spi, shifting, spiIndex, maxSymbol);

            var hashCode = Arrays.hashCode(signature);

            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = signature;
            }

            final var mirroredSignature = signature.clone();
            ArrayUtils.reverse(mirroredSignature);

            final var labelLabelMapping = new int[spi.size + 1];
            final var orientedIndexMapping = new int[spi.size + 1][];

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
                        final int[] cycle = startingBy(spiIndex[shifting[index]], shifting[index]);
                        orientedIndexMapping[newLabel] = cycleIndex(cycle);
                    }

                    final var index = Math.abs(j - shifting.length) - 1;
                    final var orientationIndex = orientedIndexMapping[newLabel][shifting[index]] + 1;
                    mirroredSignature[j] = newLabel + ((float) orientationIndex / 100);
                } else {
                    mirroredSignature[j] = newLabel;
                }
            }

            hashCode = Arrays.hashCode(mirroredSignature);
            if (hashCode < leastHashCode) {
                leastHashCode = hashCode;
                canonical = signature;
            }
        }

        return toString(canonical);
    }

    public static float[] signature(final ListOfCycles spi, final int[] pi, final int[][] spiIndex, final int maxSymbol) {
        final var piInverseIndex = getPiInverseIndex(pi, maxSymbol);
        final var orientedCycles = getOrientedCycles(spi, piInverseIndex);

        final var orientationByCycle = new boolean[maxSymbol + 1];
        Arrays.fill(orientationByCycle, false);

        var current = orientedCycles.head;
        for (int l = 0; l < orientedCycles.size; l++) {
            orientationByCycle[current.data[0]] = true;
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
                final int symbolMinIndex = Ints.asList(cycle).stream().min(comparing(integer -> piIndex[integer])).get();
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
                                                      final ListOfCycles moves,
                                                      final Move root) {
        final var piInverseIndex = getPiInverseIndex(pi, maxSymbol);

        final var orientedCycles = getOrientedCycles(spi, piInverseIndex);

        var current = orientedCycles.head;
        for (int l = 0; l < orientedCycles.size; l++) {
            final var cycle = current.data;

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

                            final var move = new int[]{a, b, c};
                            moves.push(move);

                            // ========== apply the move
                            spi.remove(cycle);
                            var numberOfTrivialCycles = 0;
                            if (aCycle.length > 1) spi.add(aCycle); else numberOfTrivialCycles++;
                            if (bCycle.length > 1) spi.add(bCycle); else numberOfTrivialCycles++;
                            if (cCycle.length > 1) spi.add(cCycle); else numberOfTrivialCycles++;
                            update(spiIndex, parity, aCycle, bCycle, cCycle);
                            // ==============================

                            if (root.children.length == 0) {
                                return moves;
                            } else {
                                for (final var m : root.children) {
                                    final var sorting = search(spi, parity, spiIndex, maxSymbol, applyTransposition(pi, move,
                                            pi.length - numberOfTrivialCycles, spiIndex), moves, m);
                                    if (!sorting.isEmpty()) {
                                        return moves;
                                    }
                                }
                            }

                            // ========== ROLLBACK
                            if (aCycle.length > 1) spi.remove(aCycle);
                            if (bCycle.length > 1) spi.remove(bCycle);
                            if (cCycle.length > 1) spi.remove(cCycle);
                            spi.add(cycle);
                            update(spiIndex, parity, cycle);
                            // ====================

                            moves.pop();
                        }
                    }
                }
            }
            current = current.next;
        }

        return ListOfCycles.EMPTY_LIST;
    }

    private static ListOfCycles analyzeOddCycles(final ListOfCycles spi,
                                                 final boolean[] parity,
                                                 final int[][] spiIndex,
                                                 final int maxSymbol,
                                                 final int[] pi,
                                                 final ListOfCycles moves,
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

                    final var move = new int[]{a, b, c};

                    final Triplet<ListOfCycles, ListOfCycles, Integer> triplet = simulate0MoveTwoCycles(spiIndex, move);

                    if (triplet.third != 2)
                        continue;

                    moves.push(move);

                    // ========== apply the move
                    spi.removeAll(triplet.first);
                    var numberOfTrivialCycles = 0;
                    spi.removeAll(triplet.first);

                    for (var current = triplet.second.head; current != null; current = current.next) {
                        final var cycle = current.data;

                        if (cycle.length > 1) {
                            spi.add(cycle);
                        } else {
                            numberOfTrivialCycles++;
                        }
                    }

                    updateIndex(spiIndex, parity, triplet.second);
                    // ==============================

                    if (root.children.length == 0) {
                        return moves;
                    } else {
                        for (final var m : root.children) {
                            final var sorting = search(spi, parity, spiIndex, maxSymbol, applyTransposition(pi, move,
                                    pi.length - numberOfTrivialCycles, spiIndex), moves, m);
                            if (!sorting.isEmpty()) {
                                return moves;
                            }
                        }
                    }

                    // ========== ROLLBACK
                    for (var current = triplet.second.head; current != null; current = current.next) {
                        final var cycle = current.data;
                        if (cycle.length > 1) spi.remove(cycle);
                    }
                    spi.addAll(triplet.first);
                    updateIndex(spiIndex, parity, triplet.first);
                    // ==============================

                    moves.pop();
                }
            }
        }

        return ListOfCycles.EMPTY_LIST;
    }

    private static ListOfCycles analyze0Moves(final ListOfCycles spi,
                                              final boolean[] parity,
                                              final int[][] spiIndex,
                                              final int maxSymbol,
                                              final int[] pi,
                                              final ListOfCycles moves,
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

                    final var move = new int[]{a, b, c};

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
                            triplet = simulate0MoveSameCycle(spiIndex, move);
                        }
                    } else {
                        triplet = simulate0MoveTwoCycles(spiIndex, move);
                    }

                    if (triplet.third != 0)
                        continue;

                    moves.push(move);

                    // ========== apply the move
                    var numberOfTrivialCycles = 0;
                    spi.removeAll(triplet.first);

                    for (var current = triplet.second.head; current != null; current = current.next) {
                        final var cycle = current.data;

                        if (cycle.length > 1) {
                            spi.add(cycle);
                        } else {
                            numberOfTrivialCycles++;
                        }
                    }
                    updateIndex(spiIndex, parity, triplet.second);
                    // ==============================

                    if (root.children.length == 0) {
                        return moves;
                    } else {
                        for (final var m : root.children) {
                            final var sorting = search(spi, parity, spiIndex, maxSymbol, applyTransposition(pi, move,
                                    pi.length - numberOfTrivialCycles, spiIndex), moves, m);
                            if (!sorting.isEmpty()) {
                                return moves;
                            }
                        }
                    }

                    // ========== ROLLBACK
                    for (var current = triplet.second.head; current != null; current = current.next) {
                        final var cycle = current.data;
                        if (cycle.length > 1) spi.remove(cycle);
                    }
                    spi.addAll(triplet.first);
                    updateIndex(spiIndex, parity, triplet.first);
                    // ==============================

                    moves.pop();
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

    private static ListOfCycles getOrientedCycles(final ListOfCycles spi, final int[] piInverseIndex) {
        final var orientedCycles = new ListOfCycles();
        var current = spi.head;
        for (int i = 0; i < spi.size; i++) {
            if (!areSymbolsInCyclicOrder(piInverseIndex, current.data))
                orientedCycles.add(current.data);
            current = current.next;
        }
        return orientedCycles;
    }

    private static void updateIndex(final int[][] index, final boolean[] parity, final ListOfCycles cycles) {
        for (var current = cycles.head; current != null; current = current.next) {
            final var cycle = current.data;
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

    private static boolean areSymbolsInCyclicOrder(final int[] index, int... symbols) {
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

    private static Triplet<ListOfCycles, ListOfCycles, Integer> simulate0MoveTwoCycles(final int[][] spiIndex, final int[] move) {
        int numberOfEvenCycles = 0;
        int a, b, c;
        if (spiIndex[move[0]] == spiIndex[move[2]]) {
            a = move[0];
            b = move[2];
            c = move[1];
            numberOfEvenCycles += spiIndex[move[0]].length & 1;
            numberOfEvenCycles += spiIndex[move[1]].length & 1;
        } else if (spiIndex[move[0]] == spiIndex[move[1]]) {
            a = move[1];
            b = move[0];
            c = move[2];
            numberOfEvenCycles += spiIndex[move[0]].length & 1;
            numberOfEvenCycles += spiIndex[move[2]].length & 1;
        } else {
            // spi.getCycle(move[1]) == spi.getCycle(move[2])
            a = move[2];
            b = move[1];
            c = move[0];
            numberOfEvenCycles += spiIndex[move[0]].length & 1;
            numberOfEvenCycles += spiIndex[move[2]].length & 1;
        }

        final var index = cycleIndex(spiIndex[c]);
        final var cImage = image(index, spiIndex[c], c);
        final var abCycle = startingBy(spiIndex[a], a);
        final var cCycle = startingBy(spiIndex[c], cImage);

        final var abCycleIndex = cycleIndex(abCycle);

        final var ba_k = getK(abCycleIndex, abCycle, b, a);
        final var newaCycle = new int[1 + ba_k - 1];
        newaCycle[0] = a;
        final var ab_k = getK(abCycleIndex, abCycle, a, b);
        System.arraycopy(abCycle,  ab_k + 1, newaCycle, 1, ba_k - 1);

        final var newbCycle = new int[1 + cCycle.length + (ab_k - 1)];
        newbCycle[0] = b;
        System.arraycopy(cCycle, 0, newbCycle, 1, cCycle.length);
        System.arraycopy(abCycle, 1, newbCycle, 1 + cCycle.length, ab_k - 1);

        var newNumberOfEvenCycles = 0;
        newNumberOfEvenCycles += newaCycle.length & 1;
        newNumberOfEvenCycles += newbCycle.length & 1;

        final var oldCycles = new ListOfCycles();
        oldCycles.add(spiIndex[move[0]]);
        if (!oldCycles.contains(spiIndex[move[1]]))
            oldCycles.add(spiIndex[move[1]]);
        if (!oldCycles.contains(spiIndex[move[2]]))
            oldCycles.add(spiIndex[move[2]]);

        final var newCycles = new ListOfCycles();
        newCycles.add(newaCycle);
        newCycles.add(newbCycle);

        return new Triplet<>(oldCycles, newCycles, newNumberOfEvenCycles - numberOfEvenCycles);
    }

    private static int image(int[] index, int[] cycle, int a) {
        return cycle[(index[a] + 1) % cycle.length];
    }

    private static Triplet<ListOfCycles, ListOfCycles, Integer> simulate0MoveSameCycle(final int[][] cycleIndex, final int[] move) {
        final int a = move[1], b = move[0], c = move[2];

        final var oldCycle = cycleIndex[move[0]];

        final int[] symbols = startingBy(oldCycle, a);
        final var newCycle = new int[oldCycle.length];

        final int[] oldCycleIndex = cycleIndex(oldCycle);

        newCycle[0] = a;
        final var ab_k = getK(oldCycleIndex, oldCycle, a, b);
        final var bc_k = getK(oldCycleIndex, oldCycle, b, c);
        System.arraycopy(symbols, ab_k + 1, newCycle, 1, bc_k - 1);
        newCycle[bc_k] = c;

        System.arraycopy(symbols, 1, newCycle, 1 + bc_k, ab_k - 1);
        newCycle[ab_k + bc_k] = b;

        final var ca_k = getK(oldCycleIndex, oldCycle, c, a);
        System.arraycopy(symbols, ab_k + bc_k + 1,
                newCycle, ab_k + bc_k + 1, ca_k - 1);

        return new Triplet<>(ListOfCycles.singleton(oldCycle), ListOfCycles.singleton(newCycle), 0);
    }

    private static int[] cycleIndex(int[] cycle) {
        final var index = new int[Ints.max(cycle) + 1];

        for (int i = 0; i < cycle.length; i++) {
            index[cycle[i]] = i;
        }

        return index;
    }

    private static int getK(int[] cycleIndex, int[] cycle, int a, int b) {
        final var aIndex = cycleIndex[a];
        final var bIndex = cycleIndex[b];

        if (bIndex >= aIndex)
            return bIndex - aIndex;

        return (cycle.length - aIndex) + bIndex;
    }

    private static int[] startingBy(int[] symbols, int a) {
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

    private static int[] applyTransposition(final int[] pi, final int[] move, int numberOfSymbols, int[][] spiIndex) {
        final var a = move[0];
        final var b = move[1];
        final var c = move[2];

        final var indexes = new int[3];
        for (var i = 0; i < pi.length; i++) {
            if (pi[i] == a)
                indexes[0] = i;
            if (pi[i] == b)
                indexes[1] = i;
            if (pi[i] == c)
                indexes[2] = i;
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

    /**
     * This list also works as a stack.
     */
    static class ListOfCycles {
        public static final ListOfCycles EMPTY_LIST = new ListOfCycles();
        int size;
        Node head;
        Node tail;

        public ListOfCycles() {
        }

        public static ListOfCycles singleton(int[] data) {
            final var singleton = new ListOfCycles();
            singleton.head = new Node(data);
            singleton.size = 1;
            return singleton;
        }

        public static ListOfCycles asList(int[]... elements) {
            final var list = new ListOfCycles();
            for (int[] element : elements) {
                list.add(element);
            }
            return list;
        }

        void add(int[] data) {
            final var newNode = new Node(data);

            if (head == null) {
                head = tail = newNode;
                head.previous = null;
            } else {
                tail.next = newNode;
                newNode.previous = tail;
                tail = newNode;
            }

            tail.next = null;

            size++;
        }

        void push(int[] data) {
            add(data);
        }

        void pop() {
            if (tail == null)
                throw new IllegalStateException("Empty stack");

            tail.previous.next = null;
            tail = tail.previous;
            size--;
        }

        public void remove(int[] data) {
            var current = tail;

            // do the search walking backwards
            while (current != null && current.data != data) {
                current = current.previous;
            }

            if (current == null) {
                return;
            }

            if (current.previous == null) {
                head = current.next;
            } else{
                current.previous.next = current.next;
            }

            if (current.next == null) {
                tail = current.previous;
            } else {
                current.next.previous = current.previous;
            }

            size--;
        }

        public boolean contains(final int[] data) {
            var current = this.head;
            for (int i = 0; i < this.size; i++) {
                if (current.data == data) {
                    return true;
                }
                current = current.next;
            }
            return false;
        }

        public void removeAll(final List<int[]> other) {
            for (int[] cycle : other) {
                remove(cycle);
            }
        }

        @Override
        public String toString() {
            final var str = new StringBuilder();
            str.append("[");

            for (var current = head; current != null; current = current.next) {
                str.append(Arrays.toString(current.data));
                str.append(" ");
            }

            str.append("]");

            return str.toString();
        }

        public void removeAll(final ListOfCycles other) {
            for (var current = other.head; current != null; current = current.next) {
                this.remove(current.data);
            }
        }

        public void addAll(final ListOfCycles other) {
            for (var current = other.head; current != null; current = current.next) {
                this.add(current.data);
            }
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public List<int[]> toList() {
            final var list = new ArrayList<int[]>();
            for (var current = head; current != null; current = current.next) {
                list.add(current.data);
            }
            return list;
        }
    }

    static class Node {
        int[] data;
        Node next;
        Node previous;
        Node(int[] d) { data = d; }

        @Override
        public String toString() {
            return Arrays.toString(data);
        }
    }
}
