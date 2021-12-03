package br.unb.cic.tdp;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.permutation.PermutationGroups;
import br.unb.cic.tdp.util.Pair;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Optional;
import java.util.Stack;

import static br.unb.cic.tdp.BaseAlgorithm.loadSortings;
import static br.unb.cic.tdp.base.CommonOperations.searchForSortingSeq;

public class Test3 {
    public static void main(String[] args) {
         Multimap<Integer, Pair<Configuration, List<Cycle>>> sortings = HashMultimap.create();
        final var _12_9seqs = new int[][]{
                    {0,2,2,2},
                    {0,0,2,2,2,2,2,2},
                    {0,2,0,2,2,2,2,2},
                    {0,2,2,0,2,2,2,2}};

        final var config = new Configuration(new MulticyclePermutation("(0,12,8)(1,11,4)(2,14,13)(3,7,5)(6,10,9)"));
//        loadSortings("cases/cases-dfs.txt", sortings);
//        var pair = sortings.get(config.hashCode())
//                .stream().filter(p -> p.getFirst().equals(config)).findFirst();
//        if (pair.isPresent()) {
//            pair.toString();
//        }

//        for (final var seq : _12_9seqs) {
//            final var sorting = new Stack<Cycle>();
//            searchForSortingSeq(config.getSpi(), config.getPi(), seq, sorting);
//            var spi = config.getSpi();
//            var pi = config.getPi();
//            if (!sorting.isEmpty()) {
//                for (final var move: sorting) {
//                    spi = PermutationGroups.computeProduct(spi, move.getInverse());
//                    pi = PermutationGroups.computeProduct(move, pi).asNCycle();
//                }
//                System.out.println(spi);
//                System.out.println(pi);
//                break;
//            }
//        }
    }
}
