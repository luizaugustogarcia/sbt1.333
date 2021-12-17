package br.unb.cic.tdp;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.proof.ProofGenerator;
import br.unb.cic.tdp.util.Pair;
import br.unb.cic.tdp.util.Triplet;
import com.google.common.primitives.Ints;
import lombok.SneakyThrows;
import org.apache.velocity.app.Velocity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.unb.cic.tdp.base.CommonOperations.*;
import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;
import static br.unb.cic.tdp.proof.ProofGenerator.*;

public class FinalPermutations {

    public static void main(String[] args) {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

//        Stream.of(
//                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(24 34 26)(25 35 27)(28 32 30)(29 33 31)(18 22 20)(19 23 21)"), // bad-case
//                new Configuration("(0 4 2)(1 35 3)(5 9 7)(6 10 8)(11 15 13)(12 16 14)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
//                new Configuration("(0 4 2)(1 5 3)(6 16 14)(7 11 9)(8 12 10)(13 35 15)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
//                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(18 34 32)(19 35 33)(20 24 22)(21 25 23)(26 30 28)(27 31 29)"),
//                new Configuration("(0 4 2)(1 5 3)(12 34 14)(13 35 33)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(27 31 29)(28 32 30)(6 10 8)(7 11 9)"),
//                new Configuration("(0 4 2)(1 5 3)(6 34 8)(7 35 33)(9 13 11)(10 14 12)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(27 31 29)(28 32 30)"),
//                new Configuration("(0 4 2)(1 5 3)(6 16 8)(7 35 9)(10 14 12)(11 15 13)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
//                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 22 20)(13 17 15)(14 18 16)(19 35 21)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
//                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 28 14)(13 35 15)(16 20 18)(17 21 19)(29 33 31)(30 34 32)(22 26 24)(23 27 25)")
//        ).forEach(conf -> sort(conf, "/home/luiskowada/proof1.333", _16_12_SEQS));

        Stream.of(
                new Configuration("(0,4,2)(1,5,3)(6,10,8)(7,11,9)(12,16,14)(13,17,15)(18,22,20)(19,23,21)(24,28,26)(25,29,27)(30,34,32)(31,35,33)(36,40,38)(37,41,39)")
        ).forEach(conf -> sort(conf, "/home/luiskowada/proof1.333", _16_12_SEQS));
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
                .filter(p -> p.getSecond() == 0).map(Pair::getFirst).collect(Collectors.toList());

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

                    final var partialSorting = new Stack<int[]>();
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
                final var s = sorting.get().stream().map(Cycle::create).collect(Collectors.toList());
                boolean is16_12 = is16_12(configuration.getSpi(), configuration.getPi(), s);
                if (is16_12) {
                    hasSorting = true;
                    executorService.shutdownNow();
                    System.out.println("Sorted: " + configuration.getSpi() + ", sorting: " + sorting.get().stream().map(Arrays::toString).collect(Collectors.joining(",")) + ", is 16/12: " + is16_12);
                    System.out.println();
                    try (final var out = new FileWriter(outputDir + "/comb/" + canonical.getSpi() + ".html")) {
                        renderSorting(canonical, canonical.translatedSorting(configuration, s), out);
                    }
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

    public static ListOfCycles search(final ListOfCycles spi,
                                     final boolean[] parity, final int[][] spiIndex,
                                     final int maxSymbol, final int[] pi,
                                     final Stack<int[]> moves,
                                     final Move root) {

        if (Thread.currentThread().isInterrupted()) {
            return ListOfCycles.emptyList;
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

        return ListOfCycles.emptyList;
    }

    private static ListOfCycles analyzeOddCycles(final ListOfCycles spi,
                                                 final boolean[] parity, final int[][] spiIndex,
                                                 final int maxSymbol, final int[] pi,
                                                 final Stack<int[]> moves,
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
                        return toListOfCycles(moves);
                    } else {
                        for (final var m : root.children) {
                            final var sorting = search(spi, parity, spiIndex, maxSymbol, applyTransposition(pi, move,
                                    pi.length - numberOfTrivialCycles, spiIndex), moves, m);
                            if (!sorting.isEmpty()) {
                                return toListOfCycles(moves);
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

        return ListOfCycles.emptyList;
    }

    private static ListOfCycles analyzeOrientedCycles(final ListOfCycles spi,
                                                      final boolean[] parity, final int[][] spiIndex,
                                                      final int maxSymbol, final int[] pi,
                                                      final Stack<int[]> moves,
                                                      final Move root) {
        final var piInverseIndex = getPiInverseIndex(pi, maxSymbol);

        final var orientedCycles = getOrientedCycles(spi, piInverseIndex);

        for (var current = orientedCycles.head; current != null; current = current.next) {
            final var cycle = current.data;

            final var before = parity[cycle[0]] ? 1 : 0;

            for (var i = 0; i < cycle.length - 2; i++) {
                for (var j = i + 1; j < cycle.length - 1; j++) {
                    for (var k = j + 1; k < cycle.length; k++) {
                        int a = cycle[i], b = cycle[j], c = cycle[k];

                        // check if it's applicable
                        if (areSymbolsInCyclicOrder(piInverseIndex, a, c, b)) {
                            final var ab_k = j - i;
                            var after = ab_k & 1;

                            final var bc_k = k - j;
                            after += bc_k & 1;

                            final var ca_k = (cycle.length - k) + i;
                            after += ca_k & 1;

                            if (after - before == 2) {
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
                                    return toListOfCycles(moves);
                                } else {
                                    for (final var m : root.children) {
                                        final var sorting = search(spi, parity, spiIndex, maxSymbol, applyTransposition(pi, move,
                                                pi.length - numberOfTrivialCycles, spiIndex), moves, m);
                                        if (!sorting.isEmpty()) {
                                            return toListOfCycles(moves);
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
            }
        }

        return ListOfCycles.emptyList;
    }

    private static ListOfCycles toListOfCycles(final Stack<int[]> moves) {
        final var list = new ListOfCycles();
        moves.forEach(list::add);
        return list;
    }

    private static ListOfCycles analyze0Moves(final ListOfCycles spi,
                                              final boolean[] parity, final int[][] spiIndex,
                                              final int maxSymbol, final int[] pi,
                                              final Stack<int[]> moves,
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
                        return toListOfCycles(moves);
                    } else {
                        for (final var m : root.children) {
                            final var sorting = search(spi, parity, spiIndex, maxSymbol, applyTransposition(pi, move,
                                    pi.length - numberOfTrivialCycles, spiIndex), moves, m);
                            if (!sorting.isEmpty()) {
                                return toListOfCycles(moves);
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

        return ListOfCycles.emptyList;
    }

    private static void update(final int[][] index, final boolean[] parity, final int[]... cycles) {
        for (int[] cycle : cycles) {
            for (int k : cycle) {
                index[k] = cycle;
                parity[k] = (cycle.length & 1) == 1;
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
        for (var current = spi.head; current != null; current = current.next) {
            final int[] cycle = current.data;
            if (!areSymbolsInCyclicOrder(piInverseIndex, cycle))
                orientedCycles.add(cycle);
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
            for (int k : cycle) {
                index[k] = cycle;
                parity[k] = (cycle.length & 1) == 1;
            }
        }
    }

    private static boolean areSymbolsInCyclicOrder(final int[] index, int... symbols) {
        boolean leap = false;
        for (int i = 0; i < symbols.length; i++) {
            if (index[symbols[i]] > index[symbols[(i + 1) % symbols.length]]) {
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
            numberOfEvenCycles += spiIndex[move[1]].length % 2;
        } else if (spiIndex[move[0]] == spiIndex[move[1]]) {
            a = move[1];
            b = move[0];
            c = move[2];
            numberOfEvenCycles += spiIndex[move[0]].length % 2;
            numberOfEvenCycles += spiIndex[move[2]].length % 2;
        } else {
            // spi.getCycle(move[1]) == spi.getCycle(move[2])
            a = move[2];
            b = move[1];
            c = move[0];
            numberOfEvenCycles += spiIndex[move[0]].length % 2;
            numberOfEvenCycles += spiIndex[move[2]].length % 2;
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
        newNumberOfEvenCycles += newaCycle.length % 2;
        newNumberOfEvenCycles += newbCycle.length % 2;

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

        Arrays.sort(indexes);

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

    static class ListOfCycles {
        public static ListOfCycles emptyList = new ListOfCycles();

        Node head;
        Node tail;

        public ListOfCycles() {
        }

        public static ListOfCycles singleton(int[] data) {
            final var singleton = new ListOfCycles();
            singleton.head = new Node(data);
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
        }

        public void remove(int[] data) {
            var current = head;

            while (current != null && current.data != data) {
                current = current.next;
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
        }

        public boolean contains(final int[] data) {
            for (var current = head; current != null; current = current.next) {
                if (current.data == data) {
                    return true;
                }
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
                str.append(current.data);
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
            return head == null;
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
