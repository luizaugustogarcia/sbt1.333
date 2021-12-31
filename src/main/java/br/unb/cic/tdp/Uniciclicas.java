package br.unb.cic.tdp;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.permutation.Permutation;
import cc.redberry.core.utils.BitArray;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.StopWatch;
import org.paukov.combinatorics.Factory;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Uniciclicas {

    @SneakyThrows
    public static void main(String[] args) {
        final var lock = new ReentrantLock(true);
        final var list = Arrays.stream(CommonOperations.CANONICAL_PI[17].getSymbols()).boxed().collect(Collectors.toList());
        final var generator = Factory.createPermutationGenerator(Factory.createVector(list));
        final var iterator = generator.iterator();
        final var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        final var s = System.currentTimeMillis();

        for (int j = 0; j < Runtime.getRuntime().availableProcessors(); j++) {
            executor.execute(() -> {
                try (final var out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\Luiz Silva\\Temp\\uni_16.txt"), 1024 * 1024))) {
                    while (true) {

                        final var permutations = new ArrayList<List<Integer>>();
                        try {
                            lock.lock();
                            for (int k = 0; k < 500; k++) {
                                final var symbols = iterator.next();
                                permutations.add(symbols.getVector());
                            }
                        } finally {
                            lock.unlock();
                        }

                        for (final var permutation : permutations) {
                            final var pi = Cycle.create(permutation.stream().mapToInt(i -> i).toArray());
                            if (isUnicyclic(17, CommonOperations.CANONICAL_PI[17], pi.getInverse())) {
                                out.append(pi.toString()
                                        .replace("(0 ", "")
                                        .replace(")", "")
                                        .replace(" ", ",")).append("\n");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    static boolean isUnicyclic(final int n, final Permutation... permutations) {
        final var functions = new int[permutations.length][n];

        for (var i = 0; i < permutations.length; i++) {
            if (permutations[i] instanceof Cycle) {
                final var cycle = (Cycle) permutations[i];
                for (var j = 0; j < cycle.size(); j++) {
                    functions[i][cycle.get(j)] = cycle.image(cycle.get(j));
                }
            } else {
                for (final var cycle : ((MulticyclePermutation) permutations[i])) {
                    for (var j = 0; j < cycle.size(); j++) {
                        functions[i][cycle.get(j)] = cycle.image(cycle.get(j));
                    }
                }
            }
        }

        final var seen = new BitArray(n);
        var counter = 0;

        var start = seen.nextZeroBit(0);

        var image = start;
        for (var i = functions.length - 1; i >= 0; i--) {
            image = functions[i][image] == -1 ? image : functions[i][image];
        }

        if (image == start) {
            ++counter;
            seen.set(start);
            return false;
        }

        while (!seen.get(start)) {
            seen.set(start);
            ++counter;

            image = start;
            for (var i = functions.length - 1; i >= 0; i--) {
                image = functions[i][image] == -1 ? image : functions[i][image];
            }

            start = image;
        }

        if (counter < n)
            return false;

        return true;
    }

    static class Lock {

        private AtomicBoolean locked = new AtomicBoolean(false);

        public void lock() {
            while (!locked.compareAndSet(false, true));
        }

        public void unlock() {
            locked.set(false);
        }
    }
}
