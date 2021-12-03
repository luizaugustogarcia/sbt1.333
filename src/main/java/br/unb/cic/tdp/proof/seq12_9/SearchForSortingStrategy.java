package br.unb.cic.tdp.proof.seq12_9;

import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.ProofGenerator;

import java.util.List;
import java.util.Stack;

public interface SearchForSortingStrategy {

    List<Cycle> search(final MulticyclePermutation spi,
                       final Cycle pi, final Stack<Cycle> moves,
                       final ProofGenerator.Move root);
}
