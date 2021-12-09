package br.unb.cic.tdp;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.ProofGenerator;
import br.unb.cic.tdp.util.Pair;
import br.unb.cic.tdp.util.Triplet;
import com.google.common.primitives.Ints;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.StopWatch;
import org.apache.velocity.app.Velocity;

import java.io.File;
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

        Stream.of(
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(24 34 26)(25 35 27)(28 32 30)(29 33 31)(18 22 20)(19 23 21)"),
                new Configuration("(0 4 2)(1 35 3)(5 9 7)(6 10 8)(11 15 13)(12 16 14)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 4 2)(1 5 3)(6 16 14)(7 11 9)(8 12 10)(13 35 15)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(18 34 32)(19 35 33)(20 24 22)(21 25 23)(26 30 28)(27 31 29)"),
                new Configuration("(0 4 2)(1 5 3)(12 34 14)(13 35 33)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(27 31 29)(28 32 30)(6 10 8)(7 11 9)"),
                new Configuration("(0 4 2)(1 5 3)(6 34 8)(7 35 33)(9 13 11)(10 14 12)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(27 31 29)(28 32 30)"),
                new Configuration("(0 4 2)(1 5 3)(6 16 8)(7 35 9)(10 14 12)(11 15 13)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 22 20)(13 17 15)(14 18 16)(19 35 21)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 28 14)(13 35 15)(16 20 18)(17 21 19)(29 33 31)(30 34 32)(22 26 24)(23 27 25)")

        ).forEach(conf -> sort(conf, "/home/luiskowada/proof1.333", _16_12_SEQS));
    }

    @SneakyThrows
    public static void sort(final Configuration conf, String outputDir, ProofGenerator.Move rootMove) {
        final var canonical = conf.getCanonical();

        final var sortingFile = new File(outputDir + "/comb/" + canonical.getSpi() + ".html");
        if (sortingFile.exists()) {
            System.out.println("Skipping " + conf.getSpi());
            return;
        }

        System.out.println("Sorting " + conf.getSpi());

        // prioritize the ones that create big components
        final var stream =
                CommonOperations.generateAll0And2Moves(canonical.getSpi(), canonical.getPi())
                        .filter(p -> p.getSecond() == 0)
                        .map(pair -> {
                            final var spi_ = computeProduct(canonical.getSpi(), pair.getFirst().getInverse());
                            final var pi_ = CommonOperations.applyTransposition(canonical.getPi(), pair.getFirst());
                            final var b_ = getComponents(spi_, pi_).stream().mapToInt(component -> component.stream().mapToInt(Cycle::size).sum()).max();
                            return new Pair<>(b_.getAsInt(), pair.getFirst());
                        }).sorted(Comparator.comparing(o -> ((Pair<Integer, Cycle>) o).getFirst()).reversed()).map(Pair::getSecond);

        final var executorService = Executors.newFixedThreadPool(1);
        final var completionService = new ExecutorCompletionService<List<int[]>>(executorService);

        final var submittedTasks = new ArrayList<Future<List<int[]>>>();

        stream.forEach(move -> {
            for (final var root : rootMove.getChildren()) {
                submittedTasks.add(completionService.submit(() -> {
                    final var partialSorting = new Stack<int[]>();
                    partialSorting.push(move.getSymbols());

                    final var spi = computeProduct(canonical.getSpi(), move.getInverse())
                            .stream().map(Cycle::getSymbols).collect(Collectors.toList());

                    int[][] spiIndex = new int[canonical.getPi().size()][];
                    spi.forEach(cycle -> {
                        for (int i : cycle) {
                            spiIndex[i] = cycle;
                        }
                    });

                    final var pi = applyTransposition(canonical.getPi().getSymbols(), move.getSymbols());

                    return search(spi, spiIndex, pi, partialSorting, root);
                }));
            }
        });

        executorService.shutdown();

        boolean hasSorting = false;
        for (int i = 0; i < submittedTasks.size(); i++) {
            final var sorting = completionService.take();
            if (sorting.get().size() > 0) {
                final var s = sorting.get().stream().map(Cycle::create).collect(Collectors.toList());
                boolean is16_12 = is16_12(canonical.getSpi(), canonical.getPi(), s);
                if (is16_12) {
                    hasSorting = true;
                    System.out.println("Sorted: " + conf.getSpi() + ", sorting: " + sorting.get().stream().map(Arrays::toString).collect(Collectors.joining(",")) + ", is 16/12: " + is16_12);
                    System.out.println();
                    try (final var out = new FileWriter(outputDir + "/comb/" + canonical.getSpi() + ".html")) {
                        renderSorting(canonical, s, out);
                    }
                    break;
                }
            }
        }

        executorService.shutdownNow();

        if (!hasSorting)
            System.out.println("Didn't find sorting for " + conf.getSpi());
    }

    public static List<int[]> search(final List<int[]> spi,
                                     final int[][] spiIndex,
                                     final int[] pi,
                                     final Stack<int[]> moves,
                                     final ProofGenerator.Move root) {

        if (Thread.currentThread().isInterrupted()) {
            return Collections.emptyList();
        }

        if (root.getMu() == 0) {
            for (int i = 0; i < pi.length - 2; i++) {
                if (spiIndex[pi[i]].length == 1) continue;
                for (int j = i + 1; j < pi.length - 1; j++) {
                    if (spiIndex[pi[j]].length == 1) continue;
                    for (int k = j + 1; k < pi.length; k++) {
                        if (spiIndex[pi[k]].length == 1) continue;

                        int a = pi[i], b = pi[j], c = pi[k];

                        final var is_2Move = spiIndex[a] != spiIndex[b] &&
                                spiIndex[b] != spiIndex[c] &&
                                spiIndex[a] != spiIndex[c];
                        if (is_2Move)
                            continue;

                        final var move = new int[]{a, b, c};

                        final Triplet<List<int[]>, List<int[]>, Integer> triplet;
                        if (isSameCycle(spiIndex, a, b, c)) {
                            final var cycle = spiIndex[a];
                            final var index = cycleIndex(cycle);
                            if (areSymbolsInCyclicOrder(index, a, b, c)) {
                                // it's 2-move, skip
                                continue;
                            }
                            triplet = simulate0MoveSameCycle(spiIndex, move);
                        } else {
                            triplet = simulate0MoveTwoCycles(spiIndex, move);
                        }

                        if (triplet.third != 0)
                            continue;

                        moves.push(move);

                        // ========== apply the move
                        spi.removeAll(triplet.first);
                        spi.addAll(triplet.second);
                        updateIndex(spiIndex, triplet.second);
                        // ==============================

                        if (root.getChildren().isEmpty()) {
                            return moves;
                        } else {
                            for (final var m : root.getChildren()) {
                                final var sorting = search(spi, spiIndex, applyTransposition(pi, move), moves, m);
                                if (!sorting.isEmpty()) {
                                    return moves;
                                }
                            }
                        }

                        // ========== rollback
                        spi.removeAll(triplet.second);
                        spi.addAll(triplet.first);
                        updateIndex(spiIndex, triplet.first);
                        // ==============================

                        moves.pop();
                    }
                }
            }
        } else {
            final var piInverseIndex = new int[pi.length];
            for (var i = 0; i < pi.length; i++) {
                piInverseIndex[pi[pi.length - i - 1]] = i;
            }

            for (final var cycle : spi.stream().filter(c -> c.length > 1 &&
                    isOriented(piInverseIndex, c)).collect(Collectors.toCollection(LinkedList::new))) {
                final var before = isEven(cycle) ? 1 : 0;
                for (var i = 0; i < cycle.length - 2; i++) {
                    for (var j = i + 1; j < cycle.length - 1; j++) {
                        for (var k = j + 1; k < cycle.length; k++) {
                            final var a = cycle[i];
                            final var b = cycle[j];
                            final var c = cycle[k];
                            // check if it's applicable
                            if (areSymbolsInCyclicOrder(piInverseIndex, a, c, b)) {
                                int[] index = cycleIndex(cycle);
                                var after = getK(index, cycle, a, b) % 2 == 1 ? 1 : 0;
                                after += getK(index, cycle, b, c) % 2 == 1 ? 1 : 0;
                                after += getK(index, cycle, c, a) % 2 == 1 ? 1 : 0;
                                if (after - before == 2) {
                                    final int[] symbols = startingBy(cycle, a);
                                    final var aCycle = new int[getK(index, cycle, c, a)];
                                    aCycle[0] = a;
                                    System.arraycopy(symbols, getK(index, cycle, a, b) + getK(index, cycle, b, c) + 1, aCycle, 1, getK(index, cycle, c, a) - 1);

                                    final var bCycle = new int[getK(index, cycle, a, b)];
                                    bCycle[0] = b;
                                    System.arraycopy(symbols, 1, bCycle, 1, getK(index, cycle, a, b) - 1);

                                    final var cCycle = new int[getK(index, cycle, b, c)];
                                    cCycle[0] = c;
                                    System.arraycopy(symbols, getK(index, cycle, a, b) + 1, cCycle, 1, getK(index, cycle, b, c) - 1);

                                    final var move = new int[]{a, b, c};
                                    moves.push(move);

                                    // ========== apply the move
                                    spi.remove(cycle);
                                    spi.add(aCycle);
                                    spi.add(bCycle);
                                    spi.add(cCycle);
                                    updateIndex(spiIndex, aCycle, bCycle, cCycle);
                                    // ==============================

                                    if (root.getChildren().isEmpty()) {
                                        return moves;
                                    } else {
                                        for (final var m : root.getChildren()) {
                                            final var sorting = search(spi, spiIndex, applyTransposition(pi, move), moves, m);
                                            if (!sorting.isEmpty()) {
                                                return moves;
                                            }
                                        }
                                    }

                                    // ========== rollback
                                    spi.remove(aCycle);
                                    spi.remove(bCycle);
                                    spi.remove(cCycle);
                                    spi.add(cycle);
                                    updateIndex(spiIndex, cycle);
                                    // ====================

                                    moves.pop();
                                }
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < pi.length - 2; i++) {
                if (spiIndex[pi[i]].length == 1 || isEven(spiIndex[pi[i]])) continue;
                for (int j = i + 1; j < pi.length - 1; j++) {
                    if (spiIndex[pi[j]].length == 1 || isEven(spiIndex[pi[j]])) continue;
                    for (int k = j + 1; k < pi.length; k++) {
                        if (spiIndex[pi[k]].length == 1 || isEven(spiIndex[pi[k]])) continue;

                        int a = pi[i], b = pi[j], c = pi[k];

                        if (isSameCycle(spiIndex, a, b, c))
                            continue;

                        final var is_2Move = spiIndex[a] != spiIndex[b] &&
                                spiIndex[b] != spiIndex[c] &&
                                spiIndex[a] != spiIndex[c];
                        if (is_2Move)
                            continue;

                        final var move = new int[]{a, b, c};

                        final Triplet<List<int[]>, List<int[]>, Integer> triplet = simulate0MoveTwoCycles(spiIndex, move);

                        if (triplet.third != 2)
                            continue;

                        moves.push(move);

                        // ========== apply the move
                        spi.removeAll(triplet.first);
                        spi.addAll(triplet.second);
                        updateIndex(spiIndex, triplet.second);
                        // ==============================

                        if (root.getChildren().isEmpty()) {
                            return moves;
                        } else {
                            for (final var m : root.getChildren()) {
                                final var sorting = search(spi, spiIndex, applyTransposition(pi, move), moves, m);
                                if (!sorting.isEmpty()) {
                                    return moves;
                                }
                            }
                        }

                        // ========== rollback
                        spi.removeAll(triplet.second);
                        spi.addAll(triplet.first);
                        updateIndex(spiIndex, triplet.first);
                        // ==============================

                        moves.pop();
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private static void updateIndex(int[][] index, List<int[]> cycles) {
        cycles.forEach(cycle -> updateIndex(index, cycle));
    }

    private static void updateIndex(int[][] index, int[]... cycles) {
        for (int[] cycle : cycles) {
            for (int k : cycle) {
                index[k] = cycle;
            }
        }
    }

    private static boolean isEven(int[] cycle) {
        return cycle.length % 2 == 1;
    }

    public static boolean isOriented(final int[] piInverseIndex, final int[] cycle) {
        return !areSymbolsInCyclicOrder(piInverseIndex, cycle);
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

    private static boolean isSameCycle(final int[][] cycleIndex, int a, int b, int c) {
        return cycleIndex[a] == cycleIndex[b] && cycleIndex[b] == cycleIndex[c];
    }

    private static Triplet<List<int[]>, List<int[]>, Integer> simulate0MoveTwoCycles(final int[][] spiIndex, final int[] move) {
        int numberOfEvenCycles = 0;
        int a, b, c;
        if (spiIndex[move[0]] == spiIndex[move[2]]) {
            a = move[0];
            b = move[2];
            c = move[1];
            numberOfEvenCycles += spiIndex[move[0]].length % 2;
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

        final var newaCycle = new int[1 + getK(abCycleIndex, abCycle, b, a) - 1];
        newaCycle[0] = a;
        System.arraycopy(abCycle,  getK(abCycleIndex, abCycle, a, b) + 1, newaCycle, 1, getK(abCycleIndex, abCycle, b, a) - 1);

        final var newbCycle = new int[1 + cCycle.length + (getK(abCycleIndex, abCycle, a, b) - 1)];
        newbCycle[0] = b;
        System.arraycopy(cCycle, 0, newbCycle, 1, cCycle.length);
        System.arraycopy(abCycle, 1, newbCycle, 1 + cCycle.length, getK(abCycleIndex, abCycle, a, b) - 1);

        var newNumberOfEvenCycles = 0;
        newNumberOfEvenCycles += newaCycle.length % 2;
        newNumberOfEvenCycles += newbCycle.length % 2;

        final var oldCycles = new LinkedList<int[]>();
        oldCycles.add(spiIndex[move[0]]);
        if (!oldCycles.contains(spiIndex[move[1]]))
            oldCycles.add(spiIndex[move[1]]);
        if (!oldCycles.contains(spiIndex[move[2]]))
            oldCycles.add(spiIndex[move[2]]);

        final var newCycles = new LinkedList<int[]>();
        newCycles.add(newaCycle);
        newCycles.add(newbCycle);

        return new Triplet<>(oldCycles, newCycles, newNumberOfEvenCycles - numberOfEvenCycles);
    }

    private static int image(int[] index, int[] cycle, int a) {
        return cycle[(index[a] + 1) % cycle.length];
    }

    private static Triplet<List<int[]>, List<int[]>, Integer> simulate0MoveSameCycle(final int[][] cycleIndex, final int[] move) {
        final int a = move[1], b = move[0], c = move[2];

        final var oldCycle = cycleIndex[move[0]];

        final int[] symbols = startingBy(oldCycle, a);
        final var newCycle = new int[oldCycle.length];

        final int[] oldCycleIndex = cycleIndex(oldCycle);

        newCycle[0] = a;
        System.arraycopy(symbols, getK(oldCycleIndex, oldCycle, a, b) + 1, newCycle, 1, getK(oldCycleIndex, oldCycle, b, c) - 1);
        newCycle[getK(oldCycleIndex, oldCycle, b, c)] = c;

        System.arraycopy(symbols, 1, newCycle, 1 + getK(oldCycleIndex, oldCycle, b, c), getK(oldCycleIndex, oldCycle, a, b) - 1);
        newCycle[getK(oldCycleIndex, oldCycle, a, b) + getK(oldCycleIndex, oldCycle, b, c)] = b;

        System.arraycopy(symbols, getK(oldCycleIndex, oldCycle, a, b) + getK(oldCycleIndex, oldCycle, b, c) + 1,
                newCycle,getK(oldCycleIndex, oldCycle, a, b) + getK(oldCycleIndex, oldCycle, b, c) + 1, getK(oldCycleIndex, oldCycle, c, a) - 1);

        return new Triplet<>(Collections.singletonList(oldCycle), Collections.singletonList(newCycle), 0);
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

    private static int[] applyTransposition(final int[] pi, final int[] move) {
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

        final var result = new int[pi.length];
        System.arraycopy(pi, 0, result, 0, indexes[0]);
        System.arraycopy(pi, indexes[1], result, indexes[0], indexes[2] - indexes[1]);
        System.arraycopy(pi, indexes[0], result, indexes[0] + (indexes[2] - indexes[1]), indexes[1] - indexes[0]);
        System.arraycopy(pi, indexes[2], result, indexes[2], pi.length - indexes[2]);

        return result;
    }
}
