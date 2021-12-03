package br.unb.cic.tdp;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.permutation.PermutationGroups;
import br.unb.cic.tdp.proof.ProofGenerator;
import br.unb.cic.tdp.util.Pair;
import br.unb.cic.tdp.util.Triplet;
import lombok.SneakyThrows;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static br.unb.cic.tdp.base.CommonOperations.*;
import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;
import static br.unb.cic.tdp.proof.ProofGenerator.renderSorting;

public class FinalPermutations {

    public static void main(String[] args) {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        Stream.of(
                new Configuration("(0 4 2)(1 35 3)(5 9 7)(6 10 8)(11 15 13)(12 16 14)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 4 2)(1 5 3)(6 16 14)(7 11 9)(8 12 10)(13 35 15)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(18 34 32)(19 35 33)(20 24 22)(21 25 23)(26 30 28)(27 31 29)"),
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(24 34 26)(25 35 27)(28 32 30)(29 33 31)(18 22 20)(19 23 21)"),
                new Configuration("(0 4 2)(1 5 3)(6 34 8)(7 35 33)(9 13 11)(10 14 12)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(27 31 29)(28 32 30)"),
                new Configuration("(0 4 2)(1 5 3)(6 16 8)(7 35 9)(10 14 12)(11 15 13)(17 21 19)(18 22 20)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 4 2)(1 5 3)(12 34 14)(13 35 33)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(27 31 29)(28 32 30)(6 10 8)(7 11 9)"),
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 22 20)(13 17 15)(14 18 16)(19 35 21)(23 27 25)(24 28 26)(29 33 31)(30 34 32)"),
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 28 14)(13 35 15)(16 20 18)(17 21 19)(29 33 31)(30 34 32)(22 26 24)(23 27 25)")
        ).forEach(conf -> sort(conf, "/home/luiskowada/proof1.333"));
    }

    @SneakyThrows
    public static void sort(final Configuration conf, String outputDir) {
        final var canonical = conf.getCanonical();

        final var sortingFile = new File(outputDir + "/comb/" + canonical.getSpi() + ".html");
        if (sortingFile.exists()) {
            System.out.println("Skipping " + conf.getSpi());
            return;
        }

        System.out.println("Sorting " + conf.getSpi());

        final var stream = generateAll0And2Moves(canonical.getSpi(), canonical.getPi())
                .filter(p -> p.third == 0)
                .map(p -> p.second)
                .map(move -> {
                    final var spi_ = computeProduct(canonical.getSpi(), move.getInverse());
                    final var pi_ = applyTransposition(canonical.getPi(), move);
                    final var b_ = getComponents(spi_, pi_).stream().mapToInt(component -> component.stream().mapToInt(Cycle::size).sum()).max();
                    return new Pair<>(b_.getAsInt(), move);
                }).sorted(Comparator.comparing(o -> ((Pair<Integer, Cycle>) o).getFirst()).reversed()).map(Pair::getSecond);

        final var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final var completionService = new ExecutorCompletionService<List<Cycle>>(executorService);

        final var submittedTasks = new ArrayList<Future<List<Cycle>>>();

        stream.forEach(m -> {
            final var move = m;
            final var _partialSorting = new Stack<Cycle>();
            _partialSorting.push(move);
            for (final var root : ProofGenerator._16_12_SEQS.getChildren()) {
                submittedTasks.add(completionService.submit(() -> {
                        Thread.currentThread().setName("pool-0");
                        return search(PermutationGroups.computeProduct(canonical.getSpi(), move.getInverse()),
                                CommonOperations.applyTransposition(canonical.getPi(), move),
                                _partialSorting,
                                root);
                }));
            }
        });

        executorService.shutdown();

        for (int i = 0; i < submittedTasks.size(); i++) {
            final var s = completionService.take();
            if (s.get().size() > 0) {
                try (final var out = new FileWriter(outputDir + "/comb/" + canonical.getSpi() + ".html")) {
                    renderSorting(canonical, s.get(), out);
                    System.out.println("Sorted " + conf.getSpi());
                }
                break;
            }
        }

        executorService.shutdownNow();

        System.out.println("Didn't find sorting for " + conf.getSpi());
    }

    public static List<Cycle> search(final MulticyclePermutation spi,
                                     final Cycle pi,
                                     final Stack<Cycle> moves,
                                     final ProofGenerator.Move root) {
        if (Thread.currentThread().isInterrupted()) {
            return Collections.emptyList();
        }

        Thread.currentThread().setName(Thread.currentThread().getName() + "-" + root.getMu());

        final Stream<Triplet<MulticyclePermutation, Cycle, Integer>> nextMoves;
        if (root.getMu() == 0) {
            nextMoves = generateAll0And2Moves(spi, pi).filter(p -> p.third == 0);
        } else {
            nextMoves = generateAll2Moves(spi, pi).map(p -> {
                final var spi_ =
                        computeProduct(true, pi.getMaxSymbol() + 1, spi, p.getFirst().getInverse());
                return new Triplet<>(spi_, p.getFirst(), 2);
            });
        }

        try {
            final var iterator = nextMoves.iterator();
            while (iterator.hasNext()) {
                final var triplet = iterator.next();
                moves.push(triplet.second);

                final var spi_ = triplet.first;
                final var pi_ = applyTransposition(pi, triplet.second);

                if (root.getChildren().isEmpty()) {
                    return moves;
                } else {
                    for (final var m : root.getChildren()) {
                        final var sorting = search(spi_, pi_, moves, m);
                        if (!sorting.isEmpty()) {
                            return moves;
                        }
                    }
                }

                moves.pop();
            }
        } catch (IllegalStateException e) {
            // means empty stream
            return Collections.emptyList();
        }

        Thread.currentThread().setName(StringUtils.chop(Thread.currentThread().getName(), 2));

        return Collections.emptyList();
    }

    private static Stream<Triplet<MulticyclePermutation, Cycle, Integer>> generateAll0And2Moves(final MulticyclePermutation spi, final Cycle pi) {
        final var ci = cycleIndex(spi, pi);
        final var numberOfEvenCycles = spi.getNumberOfEvenCycles();

        return IntStream.range(0, pi.size() - 2).boxed()
                .filter(i -> ci[pi.get(i)].size() > 1)
                .flatMap(i -> IntStream.range(i + 1, pi.size() - 1).boxed()
                        .filter(j -> ci[pi.get(j)].size() > 1)
                        .flatMap(j -> IntStream.range(j + 1, pi.size()).boxed()
                                .filter(k -> ci[pi.get(k)].size() > 1)
                                .filter(k -> {
                                    int a = pi.get(i), b = pi.get(j), c = pi.get(k);
                                    final var is_2Move = ci[a] != ci[b] && ci[b] != ci[c] && ci[a] != ci[c];
                                    // skip (-2)-moves
                                    return !is_2Move;
                                }).map(k -> {
                                    int a = pi.get(i), b = pi.get(j), c = pi.get(k);
                                    final var move = Cycle.create(a, b, c);
                                    final var spi_ = computeProduct(true, pi.getMaxSymbol() + 1, spi, move.getInverse());
                                    final var delta = (spi_.getNumberOfEvenCycles() - numberOfEvenCycles);
                                    if (delta >= 0)
                                        return new Triplet<>(spi_, move, delta);

                                    return null;
                                }))).filter(Objects::nonNull);
    }
}
