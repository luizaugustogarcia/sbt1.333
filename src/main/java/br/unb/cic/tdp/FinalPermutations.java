package br.unb.cic.tdp;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.ProofGenerator;
import br.unb.cic.tdp.util.Pair;
import br.unb.cic.tdp.util.Triplet;
import lombok.SneakyThrows;
import org.apache.commons.lang.ArrayUtils;
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

        final var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final var completionService = new ExecutorCompletionService<List<Cycle>>(executorService);

        final var submittedTasks = new ArrayList<Future<List<Cycle>>>();

        stream.forEach(move -> {
            final var partialSorting = new Stack<Cycle>();
            partialSorting.push(move);
            for (final var root : rootMove.getChildren()) {
                submittedTasks.add(completionService.submit(() -> search(computeProduct(canonical.getSpi(), move.getInverse()),
                        CommonOperations.applyTransposition(canonical.getPi(), move).getSymbols(),
                        partialSorting,
                        root)));
            }
        });

        executorService.shutdown();

        boolean hasSorting = false;
        for (int i = 0; i < submittedTasks.size(); i++) {
            final var s = completionService.take();
            if (s.get().size() > 0) {
                hasSorting = true;
                System.out.println("Sorted: " + conf.getSpi() + ", sorting: " + s.get());
                System.out.println(is12_9(canonical.getSpi(), canonical.getPi(), s.get()));
                try (final var out = new FileWriter(outputDir + "/comb/" + canonical.getSpi() + ".html")) {
                    renderSorting(canonical, s.get(), out);
                }
                break;
            }
        }

        executorService.shutdownNow();

        if (!hasSorting)
            System.out.println("Didn't find sorting for " + conf.getSpi());
    }

    public static List<Cycle> search(final MulticyclePermutation spi,
                                     final int[] pi,
                                     final Stack<Cycle> moves,
                                     final ProofGenerator.Move root) {

        if (Thread.currentThread().isInterrupted()) {
            return Collections.emptyList();
        }

        if (root.getMu() == 0) {
            for (int i = 0; i < pi.length - 2; i++) {
                if (spi.getCycle(pi[i]).size() == 1) continue;
                for (int j = i + 1; j < pi.length - 1; j++) {
                    if (spi.getCycle(pi[j]).size() == 1) continue;
                    for (int k = j + 1; k < pi.length; k++) {
                        if (spi.getCycle(pi[k]).size() == 1) continue;

                        int a = pi[i], b = pi[j], c = pi[k];

                        final var is_2Move = spi.getCycle(a) != spi.getCycle(b) &&
                                spi.getCycle(b) != spi.getCycle(c) &&
                                spi.getCycle(a) != spi.getCycle(c);
                        if (is_2Move)
                            continue;

                        final var move = Cycle.create(a, b, c);

                        final Triplet<Set<Cycle>, Set<Cycle>, Integer> triplet;
                        if (isSameCycle(spi, a, b, c)) {
                            final var cycle = spi.getCycle(a);
                            if (areSymbolsInCyclicOrder(cycle, a, b, c)) {
                                // it's 2-move, skip
                                continue;
                            }
                            triplet = simulate0MoveSameCycle(spi, move);
                        } else {
                            triplet = simulate0MoveTwoCycles(spi, move);
                        }

                        if (triplet.third != 0)
                            continue;

                        moves.push(move);

                        // ----- apply the move
                        spi.removeAll(triplet.first);
                        spi.addAll(triplet.second);
                        // ---------------------

                        if (root.getChildren().isEmpty()) {
                            return moves;
                        } else {
                            for (final var m : root.getChildren()) {
                                final var sorting = search(spi, applyTransposition(pi, move), moves, m);
                                if (!sorting.isEmpty()) {
                                    return moves;
                                }
                            }
                        }

                        // ----------- rollback
                        spi.removeAll(triplet.second);
                        spi.addAll(triplet.first);
                        // ---------------------

                        moves.pop();
                    }
                }
            }
        } else {
            final var piInverse = pi.clone();
            ArrayUtils.reverse(piInverse);

            for (final var cycle : spi.stream().filter(c -> c.size() > 1 &&
                    isOriented(piInverse, c)).collect(Collectors.toCollection(LinkedList::new))) {
                final var before = cycle.isEven() ? 1 : 0;
                for (var i = 0; i < cycle.size() - 2; i++) {
                    for (var j = i + 1; j < cycle.size() - 1; j++) {
                        for (var k = j + 1; k < cycle.size(); k++) {
                            final var a = cycle.get(i);
                            final var b = cycle.get(j);
                            final var c = cycle.get(k);
                            if (areSymbolsInCyclicOrder(pi, a, b, c)) {
                                var after = cycle.getK(a, b) % 2 == 1 ? 1 : 0;
                                after += cycle.getK(b, c) % 2 == 1 ? 1 : 0;
                                after += cycle.getK(c, a) % 2 == 1 ? 1 : 0;
                                if (after - before == 2) {
                                    final int[] symbols = startingBy(cycle.getSymbols(), a);
                                    final var aCycle = new int[cycle.getK(c, a)];
                                    aCycle[0] = a;
                                    System.arraycopy(symbols, cycle.getK(a, b) + cycle.getK(b, c) + 1, aCycle, 1, cycle.getK(c, a) - 1);

                                    final var bCycle = new int[cycle.getK(a, b)];
                                    bCycle[0] = b;
                                    System.arraycopy(symbols, 1, bCycle, 1, cycle.getK(a, b) - 1);

                                    final var cCycle = new int[cycle.getK(b, c)];
                                    cCycle[0] = c;
                                    System.arraycopy(symbols, cycle.getK(a, b) + 1, cCycle, 1, cycle.getK(b, c) - 1);

                                    final var move = Cycle.create(a, b, c);
                                    moves.push(move);

                                    // ----- apply the move
                                    spi.remove(cycle);
                                    final var cycle1 = Cycle.create(aCycle);
                                    final var cycle2 = Cycle.create(bCycle);
                                    final var cycle3 = Cycle.create(cCycle);
                                    spi.add(cycle1);
                                    spi.add(cycle2);
                                    spi.add(cycle3);
                                    // ---------------------

                                    if (root.getChildren().isEmpty()) {
                                        return moves;
                                    } else {
                                        for (final var m : root.getChildren()) {
                                            final var sorting = search(spi, applyTransposition(pi, move), moves, m);
                                            if (!sorting.isEmpty()) {
                                                return moves;
                                            }
                                        }
                                    }

                                    // ----------- rollback
                                    spi.remove(cycle1);
                                    spi.remove(cycle2);
                                    spi.remove(cycle3);
                                    spi.add(cycle);
                                    // ---------------------

                                    moves.pop();
                                }
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < pi.length - 2; i++) {
                if (spi.getCycle(pi[i]).size() == 1 || spi.getCycle(pi[i]).isEven()) continue;
                for (int j = i + 1; j < pi.length - 1; j++) {
                    if (spi.getCycle(pi[j]).size() == 1 || spi.getCycle(pi[j]).isEven()) continue;
                    for (int k = j + 1; k < pi.length; k++) {
                        if (spi.getCycle(pi[k]).size() == 1 || spi.getCycle(pi[k]).isEven()) continue;

                        int a = pi[i], b = pi[j], c = pi[k];

                        if (isSameCycle(spi, a, b, c))
                            continue;

                        final var is_2Move = spi.getCycle(a) != spi.getCycle(b) &&
                                spi.getCycle(b) != spi.getCycle(c) &&
                                spi.getCycle(a) != spi.getCycle(c);
                        if (is_2Move)
                            continue;

                        final var move = Cycle.create(a, b, c);

                        final Triplet<Set<Cycle>, Set<Cycle>, Integer> triplet = simulate0MoveTwoCycles(spi, move);

                        if (triplet.third != 2)
                            continue;

                        moves.push(move);

                        // ----- apply the move
                        spi.removeAll(triplet.first);
                        spi.addAll(triplet.second);
                        // ---------------------

                        if (root.getChildren().isEmpty()) {
                            return moves;
                        } else {
                            for (final var m : root.getChildren()) {
                                final var sorting = search(spi, applyTransposition(pi, move), moves, m);
                                if (!sorting.isEmpty()) {
                                    return moves;
                                }
                            }
                        }

                        // ----------- rollback
                        spi.removeAll(triplet.second);
                        spi.addAll(triplet.first);
                        // ---------------------

                        moves.pop();
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    public static boolean isOriented(final int[] piInverse, final Cycle cycle) {
        return !areSymbolsInCyclicOrder(piInverse, cycle.getSymbols());
    }

    private static boolean isSameCycle(final MulticyclePermutation spi, int a, int b, int c) {
        return spi.getCycle(a) == spi.getCycle(b) && spi.getCycle(b) == spi.getCycle(c);
    }

    private static Triplet<Set<Cycle>, Set<Cycle>, Integer> simulate0MoveTwoCycles(final MulticyclePermutation spi, final Cycle move) {
        int numberOfEvenCycles = 0;
        int a, b, c;
        if (spi.getCycle(move.get(0)) == spi.getCycle(move.get(2))) {
            a = move.get(0);
            b = move.get(2);
            c = move.get(1);
            numberOfEvenCycles += spi.getCycle(move.get(0)).size() % 2;
            numberOfEvenCycles += spi.getCycle(move.get(1)).size() % 2;
        } else if (spi.getCycle(move.get(0)) == spi.getCycle(move.get(1))) {
            a = move.get(1);
            b = move.get(0);
            c = move.get(2);
            numberOfEvenCycles += spi.getCycle(move.get(0)).size() % 2;
            numberOfEvenCycles += spi.getCycle(move.get(2)).size() % 2;
        } else {
            // spi.getCycle(move.get(1)) == spi.getCycle(move.get(2))
            a = move.get(2);
            b = move.get(1);
            c = move.get(0);
            numberOfEvenCycles += spi.getCycle(move.get(0)).size() % 2;
            numberOfEvenCycles += spi.getCycle(move.get(2)).size() % 2;
        }

        final var cImage = spi.getCycle(c).image(c);
        final var abCycle = startingBy(spi.getCycle(a).getSymbols(), a);
        final var cCycle = startingBy(spi.getCycle(c).getSymbols(), cImage);

        final var newaCycle = new int[1 + spi.getCycle(a).getK(b, a) - 1];
        newaCycle[0] = a;
        System.arraycopy(abCycle,  spi.getCycle(a).getK(a, b) + 1, newaCycle, 1, spi.getCycle(a).getK(b, a) - 1);

        final var newbCycle = new int[1 + cCycle.length + (spi.getCycle(a).getK(a, b) - 1)];
        newbCycle[0] = b;
        System.arraycopy(cCycle, 0, newbCycle, 1, cCycle.length);
        System.arraycopy(abCycle, 1, newbCycle, 1 + cCycle.length, spi.getCycle(a).getK(a, b) - 1);

        var newNumberOfEvenCycles = 0;
        newNumberOfEvenCycles += newaCycle.length % 2;
        newNumberOfEvenCycles += newbCycle.length % 2;

        final var oldCycles = new HashSet<Cycle>();
        oldCycles.add(spi.getCycle(move.get(0)));
        oldCycles.add(spi.getCycle(move.get(1)));
        oldCycles.add(spi.getCycle(move.get(2)));

        final var newCycles = new HashSet<Cycle>();
        newCycles.add(Cycle.create(newaCycle));
        newCycles.add(Cycle.create(newbCycle));

        return new Triplet<>(oldCycles, newCycles, newNumberOfEvenCycles - numberOfEvenCycles);
    }

    private static Triplet<Set<Cycle>, Set<Cycle>, Integer> simulate0MoveSameCycle(final MulticyclePermutation spi, final Cycle move) {
        final int a = move.get(1), b = move.get(0), c = move.get(2);

        final var oldCycle = spi.getCycle(move.get(0));

        final int[] symbols = startingBy(oldCycle.getSymbols(), a);
        final var newSymbols = new int[oldCycle.size()];

        newSymbols[0] = a;
        System.arraycopy(symbols, oldCycle.getK(a, b) + 1, newSymbols, 1, oldCycle.getK(b, c) - 1);
        newSymbols[oldCycle.getK(b, c)] = c;

        System.arraycopy(symbols, 1, newSymbols, 1 + oldCycle.getK(b, c), oldCycle.getK(a, b) - 1);
        newSymbols[oldCycle.getK(a, b) + oldCycle.getK(b, c)] = b;

        System.arraycopy(symbols, oldCycle.getK(a, b) + oldCycle.getK(b, c) + 1,
                newSymbols,oldCycle.getK(a, b) + oldCycle.getK(b, c) + 1, oldCycle.getK(c, a) - 1);

        final var newCycle = Cycle.create(newSymbols);

        return new Triplet<>(Collections.singleton(oldCycle), Collections.singleton(newCycle), 0);
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

    private static int[] applyTransposition(final int[] pi, final Cycle move) {
        final var a = move.get(0);
        final var b = move.get(1);
        final var c = move.get(2);

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
