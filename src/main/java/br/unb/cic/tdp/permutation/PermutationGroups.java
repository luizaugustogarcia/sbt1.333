package br.unb.cic.tdp.permutation;

import cc.redberry.core.utils.BitArray;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class PermutationGroups implements Serializable {

    public static MulticyclePermutation computeProduct(final Collection<Permutation> permutations) {
        return computeProduct(true, permutations.toArray(new Permutation[permutations.size()]));
    }

    public static MulticyclePermutation computeProduct(final Permutation... permutations) {
        return computeProduct(true, permutations);
    }

    public static MulticyclePermutation computeProduct(final boolean include1Cycle, final Permutation... p) {
        var n = 0;
        for (final var p1 : p) {
            if (p1 instanceof Cycle) {
                n = Math.max(((Cycle) p1).getMaxSymbol(), n);
            } else {
                for (final var c : ((MulticyclePermutation) p1)) {
                    n = Math.max(c.getMaxSymbol(), n);
                }
            }
        }
        return computeProduct(include1Cycle, n + 1, p);
    }

    public static MulticyclePermutation computeProduct(final boolean include1Cycle, final int n, final Permutation... permutations) {
        final var functions = new MutableIntIntMap[permutations.length];

        final var symbols = IntIntMaps.mutable.empty();

        for (var i = 0; i < permutations.length; i++) {
            functions[i] = IntIntMaps.mutable.empty();
            if (permutations[i] instanceof Cycle) {
                final var cycle = (Cycle) permutations[i];
                for (var j = 0; j < cycle.size(); j++) {
                    functions[i].put(cycle.get(j), cycle.image(cycle.get(j)));
                    symbols.put(cycle.get(j), 0);
                }
            } else {
                for (final var cycle : ((MulticyclePermutation) permutations[i])) {
                    for (var j = 0; j < cycle.size(); j++) {
                        functions[i].put(cycle.get(j), cycle.image(cycle.get(j)));
                        symbols.put(cycle.get(j), 0);
                    }
                }
            }
        }

        final var result = new MulticyclePermutation();

        final var cycle = IntLists.mutable.empty();
        final var bitArray = new BitArray(n);
        var counter = 0;
        while (counter < n) {
            var start = bitArray.nextZeroBit(0);

            var image = start;
            for (var i = functions.length - 1; i >= 0; i--) {
                image = !functions[i].containsKey(image) ? image : functions[i].get(image);
            }

            if (image == start) {
                ++counter;
                bitArray.set(start);
                if (include1Cycle && symbols.containsKey(start))
                    result.add(Cycle.create(start));
                continue;
            }
            while (!bitArray.get(start)) {
                bitArray.set(start);
                ++counter;
                cycle.add(start);

                image = start;
                for (var i = functions.length - 1; i >= 0; i--) {
                    image = !functions[i].containsKey(image) ? image : functions[i].get(image);
                }

                start = image;
            }

            result.add(Cycle.create(cycle.toArray()));
            cycle.clear();
        }

        return result;
    }
}
