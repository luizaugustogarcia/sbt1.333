package br.unb.cic.tdp.proof.seq12_9;

import br.unb.cic.tdp.BaseAlgorithm;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.ProofGenerator;
import br.unb.cic.tdp.util.Pair;
import org.apache.velocity.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

import static br.unb.cic.tdp.BaseAlgorithm.get3Norm;
import static br.unb.cic.tdp.base.CommonOperations.*;
import static br.unb.cic.tdp.base.CommonOperations.applyTransposition;
import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;

public class MaximizeSizeOfComponentsStrategy implements SearchForSortingStrategy {

    @Override
    public List<Cycle> search(final MulticyclePermutation spi,
                              final Cycle pi,
                              final Stack<Cycle> moves,
                              final ProofGenerator.Move root) {
        if (Thread.currentThread().isInterrupted()) {
            return Collections.emptyList();
        }

        Thread.currentThread().setName(Thread.currentThread().getName() + "-" + root.mu);

        final Stream<Cycle> nextMoves;
        if (root.mu == 0) {
            nextMoves = generateAll0And2Moves(spi, pi)
                    .filter(p -> p.getSecond() == root.mu)
                    .map(Pair::getFirst)
                    .map(move -> {
                        final var spi_ = computeProduct(true, pi.getMaxSymbol() + 1, spi, move.getInverse());
                        final var pi_ = applyTransposition(pi, move);
                        final var b_ = getComponents(spi_, pi_).stream().mapToInt(component -> component.stream().mapToInt(Cycle::size).sum()).max();
                        return new Pair<>(b_.getAsInt(), move);
                    }).sorted(Comparator.comparing(o -> ((Pair<Integer, Cycle>) o).getFirst()).reversed()).map(Pair::getSecond);
        } else {
            nextMoves = generateAll2Moves(spi, pi).map(Pair::getFirst);
        }

        try {
            final var iterator = nextMoves.iterator();
            while (iterator.hasNext()) {
                final var move = iterator.next();
                moves.push(move);

                if (root.children.length == 0) {
                    return moves;
                } else {
                    final var spi_ = computeProduct(true, pi.getMaxSymbol() + 1, spi, move.getInverse());
                    final var pi_ = applyTransposition(pi, move);

                    for (final var m : root.children) {
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
}