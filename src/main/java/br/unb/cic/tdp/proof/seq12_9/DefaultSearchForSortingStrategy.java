package br.unb.cic.tdp.proof.seq12_9;

import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.ProofGenerator;
import br.unb.cic.tdp.util.Pair;
import org.apache.velocity.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

import static br.unb.cic.tdp.base.CommonOperations.*;
import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;

public class DefaultSearchForSortingStrategy implements SearchForSortingStrategy {

    @Override
    public List<Cycle> search(final MulticyclePermutation spi,
                              final Cycle pi,
                              final Stack<Cycle> moves,
                              final ProofGenerator.Move root) {
        Thread.currentThread().setName(Thread.currentThread().getName() + "-" + root.mu);

        final Stream<Cycle> nextMoves;
        if (root.mu == 0) {
            nextMoves = generateAll0And2Moves(spi, pi).filter(p -> p.getSecond() == root.mu).map(Pair::getFirst);
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
                    for (final var m : root.children) {
                        final var sorting = search(
                                computeProduct(true, pi.getMaxSymbol() + 1, spi, move.getInverse()),
                                applyTransposition(pi, move), moves, m);
                        if (!sorting.isEmpty()) {
                            return moves;
                        }
                    }
                }
                moves.pop();
            }
        } catch(IllegalStateException e) {
            // means empty stream
            return Collections.emptyList();
        }

        Thread.currentThread().setName(StringUtils.chop(Thread.currentThread().getName(), 2));

        return Collections.emptyList();
    }
}
