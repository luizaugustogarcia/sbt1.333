package br.unb.cic.tdp.proof;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.seq15_12.Extensions;
import br.unb.cic.tdp.proof.util.Move;
import cern.colt.list.IntArrayList;
import com.google.common.base.Throwables;
import com.google.common.primitives.Ints;
import lombok.val;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.collections.api.set.primitive.MutableIntSet;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ProofGenerator {

    static final int[][] _5_4 = new int[][]{{0, 2, 2, 2, 2}};

    static final int[][] _10_8 = new int[][]{
            {0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 2, 0, 2, 2, 2, 2, 2}
    };

    static final int[][] _15_12 = new int[][]{
            {0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2}
    };

    public static final Move _5_4_SEQS = new Move(0, new Move[0], null);
    public static final Move _10_8_SEQS = new Move(0, new Move[0], null);
    public static final Move _15_12_SEQS = new Move(0, new Move[0], null);

    static {
        try {
            toTrie(_5_4, _5_4_SEQS);
            toTrie(_10_8, _10_8_SEQS);
            toTrie(_15_12, _15_12_SEQS);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    public static void toTrie(final int[][] seqs, Move root) {
        val root_ = root;
        for (val seq : seqs) {
            root = root_;
            for (var j = 1; j < seq.length; j++) {
                val move = seq[j];
                if (Arrays.stream(root.children).noneMatch(m -> m.mu == move)) {
                    if (root.children.length == 0) {
                        root.children = new Move[1];
                        root.children[0] = new Move(move, new Move[0], root);
                    } else {
                        val children = new Move[2];
                        children[0] = root.children[0];
                        children[1] = new Move(move, new Move[0], root);
                        root.children = children;
                    }
                }
                root = Arrays.stream(root.children).filter(m -> m.mu == move).findFirst().get();
            }
        }
    }

    // mvn exec:java -Dexec.mainClass="br.unb.cic.tdp.proof.ProofGenerator" -Dexec.args=".\\proof\\"
    public static void main(String[] args) throws Throwable {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.setProperty("parser.pool.size", 100);
        Velocity.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        Velocity.init();

        Files.createDirectories(Paths.get(args[0]));

        Files.copy(ProofGenerator.class.getClassLoader().getResourceAsStream("index.html"),
                Paths.get(args[0] + "/index.html"), REPLACE_EXISTING);
        Files.copy(ProofGenerator.class.getClassLoader().getResourceAsStream("explain.html"),
                Paths.get(args[0] + "/explain.html"), REPLACE_EXISTING);
        Files.copy(ProofGenerator.class.getClassLoader().getResourceAsStream("draw-config.js"),
                Paths.get(args[0] + "/draw-config.js"), REPLACE_EXISTING);

        Extensions.generate(args[0]);
        //Combinations.generate(args[0]);
    }

    public static Cycle removeExtraSymbols(final MutableIntSet symbols, final Cycle pi) {
        val newPi = new IntArrayList(symbols.size());
        for (val symbol : pi.getSymbols()) {
            if (symbols.contains(symbol))
                newPi.add(symbol);
        }
        return Cycle.of(newPi);
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

        val spis = new ArrayList<MulticyclePermutation>();
        val jsSpis = new ArrayList<String>();
        val jsPis = new ArrayList<String>();
        var spi = canonicalConfig.getSpi();
        var pi = canonicalConfig.getPi();
        for (val move : sorting) {
            spis.add(spi = computeProduct(spi, move.getInverse()));
            jsSpis.add(permutationToJsArray(spi));
            jsPis.add(cycleToJsArray(pi = computeProduct(move, pi).asNCycle()));
        }
        context.put("spis", spis);
        context.put("jsSpis", jsSpis);
        context.put("jsPis", jsPis);

        val template = Velocity.getTemplate("templates/sorting.html");
        template.merge(context, writer);
    }
}