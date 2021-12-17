package br.unb.cic.tdp;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.permutation.PermutationGroups;
import org.paukov.combinatorics.Factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static br.unb.cic.tdp.base.CommonOperations.*;

public class Teste {
    public static void main(String[] args) {
        System.out.println(PermutationGroups.
                computeProduct(new MulticyclePermutation("(0,16,2)(1,5,3)(4,8,6)(7,11,9)(10,14,12)(13,17,15)"),
                        Cycle.create("(9,10,12)")));
        methodA();
        System.out.println("-----");
        methodB();
    }

    private static void methodB() {
        final var _0Moves = new HashSet<Cycle>();
        final var spi = new MulticyclePermutation("(0,16,2)(1,5,3)(4,8,6)(7,11,9)(10,14,12)(13,17,15)");
        final var pi = CANONICAL_PI[spi.getNumberOfSymbols()];

        final var ci = new HashMap<Integer, Cycle>();
        spi.forEach(c -> {
            for (int i = 0; i < c.getSymbols().length; i++) {
                int s = c.get(i);
                ci.put(s, c);
            }
        });

        final var n = spi.getNumberOfEvenCycles();

        for (final var combination : combinations(IntStream.of(spi.getSymbols().toArray()).boxed().collect(Collectors.toList()), 3)) {
            for (final var permutation : Factory.createPermutationGenerator(combination)) {
                int a = permutation.getValue(0);
                int b = permutation.getValue(1);
                int c = permutation.getValue(2);

                final var is_0Move = ci.get(a) == ci.get(b) && ci.get(c) != ci.get(c) ||
                        ci.get(b) == ci.get(a) && ci.get(c) != ci.get(a) ||
                        ci.get(c) == ci.get(a) && ci.get(b) != ci.get(a);

                if (is_0Move && areSymbolsInCyclicOrder(pi, a,b,c)) {
                    final var s = PermutationGroups.computeProduct(spi, Cycle.create(a,b,c).getInverse());
                    if (s.getNumberOfEvenCycles() == n && s.stream().noneMatch(c_ -> !c_.isEven())) {
                        _0Moves.add(Cycle.create(a,b,c));
                    }
                }
            }
        }
        final var m = new TreeSet<>();
        _0Moves.forEach(c-> m.add(c.toString()));
        m.forEach(c-> System.out.println(c.toString()));
    }

    private static void methodA() {
        final var _0Moves = new HashSet<Cycle>();
        final var spi = new MulticyclePermutation("(0,16,2)(1,5,3)(4,8,6)(7,11,9)(10,14,12)(13,17,15)");
        final var pi = CANONICAL_PI[spi.getNumberOfSymbols()];
        for (final var combination : combinations(spi, 2)) {
            for (final var permutation: Factory.createPermutationGenerator(combination)) {
                final var c1 = permutation.getValue(0);
                final var c2 = permutation.getValue(1);

                for (int i = 0; i < c1.size(); i++) {
                    for (int j = i + 1; j <= c1.size() - 1; j++) {
                        for (int k = 0; k < c2.size(); k++) {
                            int segment1 = c1.getK(i, j) + c2.size();
                            int segment2 = c1.size() - c1.getK(i, j);
                            if (segment1 % 2 == 1 && segment2 % 2 == 1) {
                                if (areSymbolsInCyclicOrder(pi, c1.get(i),c1.get(j),c2.get(k)))
                                    _0Moves.add(Cycle.create(c1.get(i), c1.get(j), c2.get(k)));
                            }
                        }
                    }
                }
            }
        }
        final var m = new TreeSet<>();
        _0Moves.forEach(c-> m.add(c.toString()));
        m.forEach(c-> System.out.println(c.toString()));
    }
}