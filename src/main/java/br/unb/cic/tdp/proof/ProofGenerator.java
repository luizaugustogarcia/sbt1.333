package br.unb.cic.tdp.proof;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.seq12_9.Combinations;
import br.unb.cic.tdp.proof.seq12_9.SearchForSortingStrategy;
import br.unb.cic.tdp.util.Pair;
import cern.colt.list.IntArrayList;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import lombok.Getter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.Serializable;
import java.io.Writer;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static br.unb.cic.tdp.BaseAlgorithm.*;
import static br.unb.cic.tdp.base.CommonOperations.*;
import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ProofGenerator {

    public static final Multimap<Integer, Pair<Configuration, List<Cycle>>> sortings = HashMultimap.create();

    static final int[][] _4_3 = new int[][]{{0,2,2,2}};

    static final int[][] _8_6 = new int[][]{
                {0,2,2,0,2,2,2,2},
                {0,2,0,2,2,2,2,2},
                {0,0,2,2,2,2,2,2}};

    static final int[][] _12_9 = new int[][]{
                {0,2,2,0,2,2,2,0,2,2,2,2},
                {0,2,2,0,2,2,0,2,2,2,2,2},
                {0,2,2,0,2,0,2,2,2,2,2,2},
                {0,2,2,0,0,2,2,2,2,2,2,2},
                {0,2,0,2,2,2,2,0,2,2,2,2},
                {0,2,0,2,2,2,0,2,2,2,2,2},
                {0,2,0,2,2,0,2,2,2,2,2,2},
                {0,2,0,2,0,2,2,2,2,2,2,2},
                {0,2,0,0,2,2,2,2,2,2,2,2},
                {0,0,2,2,2,2,2,0,2,2,2,2},
                {0,0,2,2,2,2,0,2,2,2,2,2},
                {0,0,2,2,2,0,2,2,2,2,2,2},
                {0,0,2,2,0,2,2,2,2,2,2,2},
                {0,0,2,0,2,2,2,2,2,2,2,2},
                {0,0,0,2,2,2,2,2,2,2,2,2}
        };

    static final int[][] _16_12 = new int[][]{
            {0,2,2,0,2,2,2,0,2,2,2,0,2,2,2,2},
            {0,2,2,0,2,2,2,0,2,2,0,2,2,2,2,2},
            {0,2,2,0,2,2,2,0,2,0,2,2,2,2,2,2},
            {0,2,2,0,2,2,2,0,0,2,2,2,2,2,2,2},
            {0,2,2,0,2,2,0,2,2,2,2,0,2,2,2,2},
            {0,2,2,0,2,2,0,2,2,2,0,2,2,2,2,2},
            {0,2,2,0,2,2,0,2,2,0,2,2,2,2,2,2},
            {0,2,2,0,2,2,0,2,0,2,2,2,2,2,2,2},
            {0,2,2,0,2,2,0,0,2,2,2,2,2,2,2,2},
            {0,2,2,0,2,0,2,2,2,2,2,0,2,2,2,2},
            {0,2,2,0,2,0,2,2,2,2,0,2,2,2,2,2},
            {0,2,2,0,2,0,2,2,2,0,2,2,2,2,2,2},
            {0,2,2,0,2,0,2,2,0,2,2,2,2,2,2,2},
            {0,2,2,0,2,0,2,0,2,2,2,2,2,2,2,2},
            {0,2,2,0,2,0,0,2,2,2,2,2,2,2,2,2},
            {0,2,2,0,0,2,2,2,2,2,2,0,2,2,2,2},
            {0,2,2,0,0,2,2,2,2,2,0,2,2,2,2,2},
            {0,2,2,0,0,2,2,2,2,0,2,2,2,2,2,2},
            {0,2,2,0,0,2,2,2,0,2,2,2,2,2,2,2},
            {0,2,2,0,0,2,2,0,2,2,2,2,2,2,2,2},
            {0,2,2,0,0,2,0,2,2,2,2,2,2,2,2,2},
            {0,2,2,0,0,0,2,2,2,2,2,2,2,2,2,2},
            {0,2,0,2,2,2,2,0,2,2,2,0,2,2,2,2},
            {0,2,0,2,2,2,2,0,2,2,0,2,2,2,2,2},
            {0,2,0,2,2,2,2,0,2,0,2,2,2,2,2,2},
            {0,2,0,2,2,2,2,0,0,2,2,2,2,2,2,2},
            {0,2,0,2,2,2,0,2,2,2,2,0,2,2,2,2},
            {0,2,0,2,2,2,0,2,2,2,0,2,2,2,2,2},
            {0,2,0,2,2,2,0,2,2,0,2,2,2,2,2,2},
            {0,2,0,2,2,2,0,2,0,2,2,2,2,2,2,2},
            {0,2,0,2,2,2,0,0,2,2,2,2,2,2,2,2},
            {0,2,0,2,2,0,2,2,2,2,2,0,2,2,2,2},
            {0,2,0,2,2,0,2,2,2,2,0,2,2,2,2,2},
            {0,2,0,2,2,0,2,2,2,0,2,2,2,2,2,2},
            {0,2,0,2,2,0,2,2,0,2,2,2,2,2,2,2},
            {0,2,0,2,2,0,2,0,2,2,2,2,2,2,2,2},
            {0,2,0,2,2,0,0,2,2,2,2,2,2,2,2,2},
            {0,2,0,2,0,2,2,2,2,2,2,0,2,2,2,2},
            {0,2,0,2,0,2,2,2,2,2,0,2,2,2,2,2},
            {0,2,0,2,0,2,2,2,2,0,2,2,2,2,2,2},
            {0,2,0,2,0,2,2,2,0,2,2,2,2,2,2,2},
            {0,2,0,2,0,2,2,0,2,2,2,2,2,2,2,2},
            {0,2,0,2,0,2,0,2,2,2,2,2,2,2,2,2},
            {0,2,0,2,0,0,2,2,2,2,2,2,2,2,2,2},
            {0,2,0,0,2,2,2,2,2,2,2,0,2,2,2,2},
            {0,2,0,0,2,2,2,2,2,2,0,2,2,2,2,2},
            {0,2,0,0,2,2,2,2,2,0,2,2,2,2,2,2},
            {0,2,0,0,2,2,2,2,0,2,2,2,2,2,2,2},
            {0,2,0,0,2,2,2,0,2,2,2,2,2,2,2,2},
            {0,2,0,0,2,2,0,2,2,2,2,2,2,2,2,2},
            {0,2,0,0,2,0,2,2,2,2,2,2,2,2,2,2},
            {0,2,0,0,0,2,2,2,2,2,2,2,2,2,2,2},
            {0,0,2,2,2,2,2,0,2,2,2,0,2,2,2,2},
            {0,0,2,2,2,2,2,0,2,2,0,2,2,2,2,2},
            {0,0,2,2,2,2,2,0,2,0,2,2,2,2,2,2},
            {0,0,2,2,2,2,2,0,0,2,2,2,2,2,2,2},
            {0,0,2,2,2,2,0,2,2,2,2,0,2,2,2,2},
            {0,0,2,2,2,2,0,2,2,2,0,2,2,2,2,2},
            {0,0,2,2,2,2,0,2,2,0,2,2,2,2,2,2},
            {0,0,2,2,2,2,0,2,0,2,2,2,2,2,2,2},
            {0,0,2,2,2,2,0,0,2,2,2,2,2,2,2,2},
            {0,0,2,2,2,0,2,2,2,2,2,0,2,2,2,2},
            {0,0,2,2,2,0,2,2,2,2,0,2,2,2,2,2},
            {0,0,2,2,2,0,2,2,2,0,2,2,2,2,2,2},
            {0,0,2,2,2,0,2,2,0,2,2,2,2,2,2,2},
            {0,0,2,2,2,0,2,0,2,2,2,2,2,2,2,2},
            {0,0,2,2,2,0,0,2,2,2,2,2,2,2,2,2},
            {0,0,2,2,0,2,2,2,2,2,2,0,2,2,2,2},
            {0,0,2,2,0,2,2,2,2,2,0,2,2,2,2,2},
            {0,0,2,2,0,2,2,2,2,0,2,2,2,2,2,2},
            {0,0,2,2,0,2,2,2,0,2,2,2,2,2,2,2},
            {0,0,2,2,0,2,2,0,2,2,2,2,2,2,2,2},
            {0,0,2,2,0,2,0,2,2,2,2,2,2,2,2,2},
            {0,0,2,2,0,0,2,2,2,2,2,2,2,2,2,2},
            {0,0,2,0,2,2,2,2,2,2,2,0,2,2,2,2},
            {0,0,2,0,2,2,2,2,2,2,0,2,2,2,2,2},
            {0,0,2,0,2,2,2,2,2,0,2,2,2,2,2,2},
            {0,0,2,0,2,2,2,2,0,2,2,2,2,2,2,2},
            {0,0,2,0,2,2,2,0,2,2,2,2,2,2,2,2},
            {0,0,2,0,2,2,0,2,2,2,2,2,2,2,2,2},
            {0,0,2,0,2,0,2,2,2,2,2,2,2,2,2,2},
            {0,0,2,0,0,2,2,2,2,2,2,2,2,2,2,2},
            {0,0,0,2,2,2,2,2,2,2,2,0,2,2,2,2},
            {0,0,0,2,2,2,2,2,2,2,0,2,2,2,2,2},
            {0,0,0,2,2,2,2,2,2,0,2,2,2,2,2,2},
            {0,0,0,2,2,2,2,2,0,2,2,2,2,2,2,2},
            {0,0,0,2,2,2,2,0,2,2,2,2,2,2,2,2},
            {0,0,0,2,2,2,0,2,2,2,2,2,2,2,2,2},
            {0,0,0,2,2,0,2,2,2,2,2,2,2,2,2,2},
            {0,0,0,2,0,2,2,2,2,2,2,2,2,2,2,2},
            {0,0,0,0,2,2,2,2,2,2,2,2,2,2,2,2}};

    public static final Move _4_3_SEQS = new Move(0, new LinkedList<>());
    public static final Move _8_6_SEQS = new Move(0, new LinkedList<>());
    public static final Move _12_9_SEQS = new Move(0, new LinkedList<>());
    public static final Move _16_12_SEQS = new Move(0, new LinkedList<>());

    // TODO Remove
    static final int[][] _16_12_SPECIAL = new int[][]{{0,0,0,0,2,2,2,2,2,2,2,2,2,2,2,2}};
    public static final Move _16_12_SPECIAL_SEQ = new Move(0, new LinkedList<>());

    static {
        toTrie(_4_3, _4_3_SEQS);
        toTrie(_8_6, _8_6_SEQS);
        toTrie(_12_9, _12_9_SEQS);
        toTrie(_16_12, _16_12_SEQS);

        // TODO Remove
        toTrie(_16_12_SPECIAL, _16_12_SPECIAL_SEQ);
    }

    private static void toTrie(final int[][] seqs, Move root) {
        final var root_ = root;
        for (int[] seq : seqs) {
            root = root_;
            for (int j = 1; j < seq.length; j++) {
                final var move = seq[j];
                if (root.getChildren().stream().noneMatch(m -> m.mu == move)) {
                    root.getChildren().add(new Move(move, new LinkedList<>()));
                }
                root = root.getChildren().stream().filter(m -> m.getMu() == move).findFirst().get();
            }
        }
    }

    // mvn exec:java -Dexec.mainClass="br.unb.cic.tdp.proof.ProofGenerator" -Dexec.args=".\\proof\\"
    public static void main(String[] args) throws Throwable {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        Files.createDirectories(Paths.get(args[0]));

        Files.copy(ProofGenerator.class.getClassLoader().getResourceAsStream("index.html"),
                Paths.get(args[0] + "/index.html"), REPLACE_EXISTING);
        Files.copy(ProofGenerator.class.getClassLoader().getResourceAsStream("explain.html"),
                Paths.get(args[0] + "/explain.html"), REPLACE_EXISTING);
        Files.copy(ProofGenerator.class.getClassLoader().getResourceAsStream("draw-config.js"),
                Paths.get(args[0] + "/draw-config.js"), REPLACE_EXISTING);

        // TODO load sortings from 11/8 algorithm
        // this one here is for the algorithm
        //loadSortings("cases/cases-dfs.txt", sortings);
        loadSortings("cases/cases-comb.txt", sortings);

        //Extensions.generate(args[0]);
        Combinations.generate(args[0]);
    }

    public static Optional<List<Cycle>> searchForSorting(final Configuration config,
                                                         final SearchForSortingStrategy strategy) {
        var candidates = sortings.get(config.hashCode());

        Optional<Pair<Configuration, List<Cycle>>> pair;
        if (candidates.size() == 1) {
            pair = candidates.stream().findFirst();
        } else {
            pair = candidates.stream().filter(p -> p.getFirst().equals(config)).findFirst();
        }

        if (pair.isPresent() && (pair.get().getSecond().size() == 4 || pair.get().getSecond().size() == 8)) {
            // in this case, we found a 4/3-sequence earlier in the previous work
            return Optional.of(config.translatedSorting(pair.get().getFirst(), pair.get().getSecond()));
        }

        final var _3norm = config.getSpi().get3Norm();

        final var sorting = new Stack<Cycle>();

        String threadName = Thread.currentThread().getName();

        if (_3norm >= 3) {
            Thread.currentThread().setName(config.hashCode() + "-" + config.getSpi() + "-4,3");
            strategy.search(config.getSpi(), config.getPi(), sorting, _4_3_SEQS);
        }

        if (_3norm >= 6 && sorting.isEmpty()) {
            Thread.currentThread().setName(config.hashCode() + "-" + config.getSpi() + "-8,6");
            strategy.search(config.getSpi(), config.getPi(), sorting, _8_6_SEQS);
            Thread.currentThread().setName(threadName);
        }

        if (_3norm >= 9 && sorting.isEmpty()) {
            Thread.currentThread().setName(config.hashCode() + "-" + config.getSpi() + "-12,9");
            strategy.search(config.getSpi(), config.getPi(), sorting, _12_9_SEQS);
            Thread.currentThread().setName(threadName);
        }

        if (_3norm >= 12 && sorting.isEmpty()) {
            System.out.println("Searching for 16/12: " + config.getSpi());
            Thread.currentThread().setName(config.hashCode() + "-" + config.getSpi() + "-12,9 or 16,12");
            strategy.search(config.getSpi(), config.getPi(), sorting, _16_12_SEQS);
        }

        Thread.currentThread().setName(threadName);

        if (!sorting.isEmpty()) {
            return Optional.of(sorting);
        }

        if (config.isFull() && getComponents(config.getSpi(), config.getPi()).size() == 1) {
            System.out.println("Full configuration without (12/9): " + config.getCanonical().getSpi());
        }

        return Optional.empty();
    }

    public static Cycle removeExtraSymbols(final Set<Integer> symbols, final Cycle pi) {
        final var newPi = new IntArrayList(symbols.size());
        for (final var symbol: pi.getSymbols()) {
            if (symbols.contains(symbol))
                newPi.add(symbol);
        }
        return Cycle.create(newPi);
    }

    public static String permutationToJsArray(final MulticyclePermutation permutation) {
        return "[" + permutation
                .stream().map(c -> "[" + Ints.asList(c.getSymbols()).stream()
                        .map(s -> Integer.toString(s))
                        .collect(Collectors.joining(",")) + "]")
                .collect(Collectors.joining(",")) + "]";
    }

    private static String cycleToJsArray(final Cycle cycle) {
        return "[" + Ints.asList(cycle.getSymbols()).stream()
                .map(s -> Integer.toString(s))
                .collect(Collectors.joining(",")) + "]";
    }

    public static void renderSorting(final Configuration canonicalConfig, final List<Cycle> sorting, final Writer writer) {
        VelocityContext context = new VelocityContext();

        context.put("spi", canonicalConfig.getSpi());
        context.put("piSize", canonicalConfig.getPi().size());
        context.put("jsSpi", permutationToJsArray(canonicalConfig.getSpi()));
        context.put("jsPi", cycleToJsArray(canonicalConfig.getPi()));
        context.put("sorting", sorting);

        final var spis = new ArrayList<MulticyclePermutation>();
        final var jsSpis = new ArrayList<String>();
        final var jsPis = new ArrayList<String>();
        var spi = canonicalConfig.getSpi();
        var pi = canonicalConfig.getPi();
        for (final Cycle move : sorting) {
            spis.add(spi = computeProduct(spi, move.getInverse()));
            jsSpis.add(permutationToJsArray(spi));
            jsPis.add(cycleToJsArray(pi = computeProduct(move, pi).asNCycle()));
        }
        context.put("spis", spis);
        context.put("jsSpis", jsSpis);
        context.put("jsPis", jsPis);

        final var template = Velocity.getTemplate("templates/sorting.html");
        template.merge(context, writer);
    }

    @Getter
    public static class Move implements Serializable {
        private final int mu;
        private final List<Move> children;

        public Move(int mu, List<Move> children) {
            this.mu = mu;
            this.children = children;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move m = (Move) o;
            return mu == m.mu;
        }

        @Override
        public String toString() {
            return Integer.toString(mu);
        }
    }
}
