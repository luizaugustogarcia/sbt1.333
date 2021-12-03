package br.unb.cic.tdp.permutation;

import cern.colt.list.IntArrayList;
import com.google.common.primitives.Ints;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MulticyclePermutation extends ArrayList<Cycle> implements Permutation {

    public MulticyclePermutation() {
    }

    public MulticyclePermutation(final String permutation, final boolean include1Cycles) {
        of(permutation);
        if (include1Cycles) {
            for (int i = 0; i <= getMaxSymbol(); i++) {
                if (!getSymbols().contains(i)) {
                    this.add(Cycle.create(i));
                }
            }
        }
    }

    public MulticyclePermutation(final String permutation) {
        of(permutation);
    }

    private void of(String permutation) {
        var cycle = new IntArrayList();
        int symbol = 0;
        for (var i = 0; i < permutation.length(); i++) {
            final var current = permutation.charAt(i);
            if (current != '(') {
                if (current == ')') {
                    cycle.add(symbol);
                    symbol = 0;
                    this.add(Cycle.create(cycle));
                    cycle = new IntArrayList();
                } else if (current == ',' || current == ' ') {
                    cycle.add(symbol);
                    symbol = 0;
                } else {
                    symbol = symbol * 10 + Character.getNumericValue(current);
                }
            }
        }
    }

    public MulticyclePermutation(final Cycle cycle) {
        this.add(cycle);
    }

    public MulticyclePermutation(final Collection<Cycle> cycles) {
        addAll(cycles);
    }

    @Override
    public String toString() {
        if (this.isEmpty()) {
            return "()";
        }
        return StringUtils.join(this, "");
    }

    @Override
    public MulticyclePermutation getInverse() {
        final var permutation = new MulticyclePermutation();

        this.forEach((cycle) -> permutation.add(cycle.getInverse()));

        return permutation;
    }

    public Cycle asNCycle() {
        if (this.size() > 1) {
            throw new RuntimeException("NONCYCLICPERMUTATION");
        }
        return this.get(0);
    }

    @Override
    public int image(int a) {
        // this is inefficient
        return this.stream().filter(c -> c.contains(a)).findFirst().get().image(a);
    }

    public boolean isIdentity() {
        return this.isEmpty() || (stream().filter((cycle) -> cycle.size() == 1).count() == this.size());
    }

    @Override
    public int getNumberOfEvenCycles() {
        return (int) this.stream().filter((cycle) -> cycle.size() % 2 == 1).count();
    }

    public int getNumberOfSymbols() {
        return this.stream().mapToInt(Cycle::size).sum();
    }

    public Set<Integer> getSymbols() {
        return this.stream().flatMap(cycle -> Ints.asList(cycle.getSymbols()).stream()).collect(Collectors.toSet());
    }

    public int get3Norm() {
        return (this.getNumberOfSymbols() - getNumberOfEvenCycles()) / 2;
    }

    public int getMaxSymbol() {
        return Collections.max(getSymbols());
    }

    public List<Cycle> getNonTrivialCycles() {
        return this.stream().filter(c -> c.size() > 1).collect(Collectors.toList());
    }
}
