package br.unb.cic.tdp.proof.seq12_9;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.util.*;
import br.unb.cic.tdp.util.Pair;
import cern.colt.list.FloatArrayList;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.Floats;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static br.unb.cic.tdp.base.CommonOperations.generateAll0And2Moves;
import static br.unb.cic.tdp.base.CommonOperations.getComponents;
import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;
import static br.unb.cic.tdp.proof.ProofGenerator.*;
import static br.unb.cic.tdp.proof.seq12_9.Extensions.cleanUpBadExtensionAndInvalidFiles;
import static br.unb.cic.tdp.proof.util.SequenceSearcher.applyTransposition;
import static br.unb.cic.tdp.proof.util.SequenceSearcher.canonicalSignature;
import static java.util.stream.Collectors.toList;

public class Combinations {

    private static final Map<Integer, Set<Configuration>> incompleteConfigs = new HashMap<>();

    private static final Configuration ORIENTED_5_CYCLE = new Configuration(
            new MulticyclePermutation("(0,3,1,4,2)"));
    public static final Configuration INTERLEAVING_PAIR = new Configuration(
            new MulticyclePermutation("(0,4,2)(1,5,3)"));
    private static final Configuration NECKLACE_SIZE_4 = new Configuration(
            new MulticyclePermutation("(0,10,2)(1,5,3)(4,8,6)(7,11,9)"));
    private static final Configuration TWISTED_NECKLACE_SIZE_4 = new Configuration(
            new MulticyclePermutation("(0,7,5)(1,11,9)(2,6,4)(3,10,8)"));
    private static final Configuration NECKLACE_SIZE_5 = new Configuration(
            new MulticyclePermutation("(0,4,2)(1,14,12)(3,7,5)(6,10,8)(9,13,11)"));
    private static final Configuration NECKLACE_SIZE_6 = new Configuration(
            new MulticyclePermutation("(0,16,2)(1,5,3)(4,8,6)(7,11,9)(10,14,12)(13,17,15)"));
    private static final Configuration NECKLACE_SIZE_8 = new Configuration(
            new MulticyclePermutation("(0,22,20)(1,23,3)(2,6,4)(5,9,7)(8,12,10)(11,15,13)(14,18,16)(17,21,19)"));
    private static final Configuration NECKLACE_SIZE_10 = new Configuration(
            new MulticyclePermutation("(0,28,2)(1,5,3)(4,8,6)(7,11,9)(10,14,12)(13,17,15)(16,20,18)(19,23,21)(22,26,24)(25,29,27)"));

    public static final Configuration[] BAD_SMALL_COMPONENTS =
            new Configuration[]{ORIENTED_5_CYCLE, INTERLEAVING_PAIR, NECKLACE_SIZE_4,
                    TWISTED_NECKLACE_SIZE_4, NECKLACE_SIZE_5, NECKLACE_SIZE_6, NECKLACE_SIZE_8, NECKLACE_SIZE_10};

    @SneakyThrows
    public static void generate(final String outputDir) {
        Files.createDirectories(Paths.get(outputDir + "/comb/"));
        Files.createDirectories(Paths.get(outputDir + "/comb/bad-cases/"));

        cleanUpBadExtensionAndInvalidFiles(outputDir + "/comb/");

        // ATTENTION: The Sort Or Extend fork/join can never run with BAD EXTENSION files in the comb directory.
        // Otherwise, it will wrongly skip cases.

        var pool = new ForkJoinPool();

        for (final var configuration : BAD_SMALL_COMPONENTS) {
            pool.execute(new SortOrExtendCombinations(configuration, outputDir + "/comb/"));
        }

        pool.shutdown();
        // boundless
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        var forkJoinPool = new ForkJoinPool();
        Arrays.stream(BAD_SMALL_COMPONENTS).forEach(c -> forkJoinPool.execute(new MakeHtmlNavigation(c, outputDir)));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    private static List<Pair<String, Configuration>> extend(final Configuration config) {
        final var result = new ArrayList<Pair<String, Configuration>>();
        for (final var badSmallComponent : BAD_SMALL_COMPONENTS) {
            for (int i = 0; i < config.getPi().size(); i++) {
                final var badSmallComponentSignature = badSmallComponent.getSignature().getContent().clone();
                for (int j = 0; j < badSmallComponentSignature.length; j++) {
                    badSmallComponentSignature[j] += config.getSpi().size();
                }
                final var signature = new FloatArrayList(config.getSignature().getContent().clone());
                signature.beforeInsertAllOf(i, Floats.asList(badSmallComponentSignature));
                signature.trimToSize();

                final String info;
                if (badSmallComponent == ORIENTED_5_CYCLE)
                    info = "bad oriented 5-cycle";
                else if (badSmallComponent == INTERLEAVING_PAIR)
                    info = "unoriented interleaving pair";
                else if (badSmallComponent == NECKLACE_SIZE_4)
                    info = "unoriented necklace of size 4";
                else if (badSmallComponent == TWISTED_NECKLACE_SIZE_4)
                    info = "unoriented twisted necklace of size 4";
                else if (badSmallComponent == NECKLACE_SIZE_5)
                    info = "unoriented necklace of size 5";
                else if (badSmallComponent == NECKLACE_SIZE_6)
                    info = "unoriented necklace of size 6";
                else if (badSmallComponent == NECKLACE_SIZE_8)
                    info = "unoriented necklace of size 8";
                else
                    info = "unoriented necklace of size 10";

                result.add(new Pair<>("pos=" + i + " add " + info, Configuration.ofSignature(signature.elements())));
            }
        }
        return result;
    }

    @AllArgsConstructor
    static class MakeHtmlNavigation extends RecursiveAction {
        final Configuration configuration;
        final String outputDir;

        @SneakyThrows
        @Override
        protected void compute() {
            final var file = new File(outputDir + "/comb/" + configuration.getSpi() + ".html");
            if (file.exists())
                return;

            try (final var out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file), 1024 * 100))) {
                out.println("<html>\n" +
                        "\t<head>\n" +
                        "\t\t<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css\" integrity=\"sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh\" crossorigin=\"anonymous\">\n" +
                        "\t\t<script src=\"https://code.jquery.com/jquery-3.4.1.slim.min.js\" integrity=\"sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n\" crossorigin=\"anonymous\"></script>\n" +
                        "\t\t<script src=\"https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js\" integrity=\"sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo\" crossorigin=\"anonymous\"></script>\n" +
                        "\t\t<script src=\"https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js\" integrity=\"sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6\" crossorigin=\"anonymous\"></script>\n" +
                        "\t\t<script src=\"../draw-config.js\"></script>\n" +
                        "\t\t<style>* { font-size: small; }</style>\n" +
                        "\t</head>\n" +
                        "<body>\n" +
                        "<div class=\"modal fade\" id=\"modal\" role=\"dialog\">\n" +
                        "    <div class=\"modal-dialog\" style=\"left: 25px; max-width: unset;\">\n" +
                        "      <!-- Modal content-->\n" +
                        "      <div class=\"modal-content\" style=\"width: fit-content;\">\n" +
                        "        <div class=\"modal-header\">\n" +
                        "          <h6 class=\"modal-title\">--------</h6>\n" +
                        "          <button type=\"button\" class=\"close\" data-dismiss=\"modal\">&times;</button>\n" +
                        "        </div>\n" +
                        "        <div class=\"modal-body\">\n" +
                        "          <canvas id=\"modalCanvas\"></canvas>\n" +
                        "        </div>\n" +
                        "      </div>\n" +
                        "    </div>\n" +
                        "</div>\n" +
                        "<script>\n" +
                        "\tfunction updateCanvas(canvasId, spi) {\n" +
                        "\t   var pi = []; for (var i = 0; i < spi.flatMap(c => c).length; i++) { pi.push(i); }" +
                        "\t   var canvas = document.getElementById(canvasId);\n" +
                        "\t   canvas.height = calcHeight(canvas, spi, pi);\n" +
                        "\t   canvas.width = pi.length * padding;\n" +
                        "\t   draw(canvas, spi, pi);\n" +
                        "\t}\n" +
                        "</script>\n" +
                        "<div style=\"margin-top: 10px; margin-left: 10px\">");

                out.println("<canvas id=\"canvas\"></canvas>");
                out.printf("<script>updateCanvas('canvas', %s);</script>%n",
                        permutationToJsArray(configuration.getSpi()));

                out.println("<h6>" + configuration.getSpi() + "</h6>");

                out.println("Hash code: " + configuration.hashCode() + "<br>");
                out.println("Signature: " + configuration.getSignature() + "<br>");
                out.println("3-norm: " + configuration.getSpi().get3Norm());

                out.println("<p style=\"margin-top: 10px;\"></p>");
                out.println("THE EXTENSIONS ARE:");

                renderExtensions(configuration, out, outputDir);
            }
        }

        @SneakyThrows
        private void renderExtensions(final Configuration configuration,
                                      final PrintStream out, final String outputDir) {
            for (final var extension : extend(configuration)) {
                final var canonical = extension.getSecond().getCanonical();

                final var badCaseFile = new File(outputDir + "/comb/bad-cases/" + canonical.getSpi());
                final var sortingFile = new File(outputDir + "/comb/" + canonical.getSpi() + ".html");

                final var incomplete = !badCaseFile.exists() && !sortingFile.exists();
                if (incomplete) {
                    synchronized (incompleteConfigs) {
                        incompleteConfigs.computeIfAbsent(canonical.get3Norm(), key -> new HashSet<>());
                        incompleteConfigs.get(canonical.get3Norm()).add(canonical);
                    }
                }

                final var hasSorting = !badCaseFile.exists();

                out.println(hasSorting && !incomplete ? "<div style=\"margin-top: 10px; background-color: rgba(153, 255, 153, 0.15)\">" :
                        "<div style=\"margin-top: 10px; background-color: rgba(255, 0, 0, 0.05);\">");
                out.println(extension.getFirst() + "<br>");
                out.println((incomplete ? "INCOMPLETE" : ((hasSorting ? "GOOD" : "BAD") + " EXTENSION")) + "<br>");
                out.println("Hash code: " + extension.getSecond().hashCode() + "<br>");
                out.println("3-norm: " + extension.getSecond().getSpi().get3Norm() + "<br>");
                out.println("Signature: " + extension.getSecond().getSignature() + "<br>");

                final var jsSpi = permutationToJsArray(extension.getSecond().getSpi());
                out.printf("Extension: <a href=\"\" " +
                                "onclick=\"" +
                                "updateCanvas('modalCanvas', %s); " +
                                "$('h6.modal-title').text('%s');" +
                                "$('#modal').modal('show'); " +
                                "return false;\">%s</a><br>%n",
                        jsSpi, extension.getSecond().getSpi(), extension.getSecond().getSpi());

                if (!hasSorting) {
                    out.printf("View extension: <a href=\"%s.html\">%s</a>%n",
                            extension.getSecond().getSpi(), extension.getSecond().getSpi());
                } else {
                    out.printf("View sorting: <a href=\"%s.html\">%s</a>%n", canonical.getSpi(), canonical.getSpi());
                }

                out.println("</div>");
                out.println("</div></body></html>");

                if (incomplete)
                    continue;

                if (!hasSorting) {
                    new MakeHtmlNavigation(extension.getSecond(), outputDir).fork();
                }
            }
        }
    }

    public static class SortOrExtendCombinations extends SortOrExtend {

        public SortOrExtendCombinations(final Configuration configuration, final String outputDir) {
            super(configuration, outputDir);
        }

        public static <T> Generator<T> combinations(final Collection<T> collection, final int k) {
            return Factory.createSimpleCombinationGenerator(Factory.createVector(collection), k);
        }

        @SneakyThrows
        @Override
        protected List<Cycle> searchSorting(final Configuration configuration, final Move rootMove) {
            final var smallComponents = getComponents(configuration.getSpi(), configuration.getPi());

            for (int i = 2; i < smallComponents.size(); i++) {
                for (final var components : combinations(smallComponents, i)) {
                    final var spi = new MulticyclePermutation(components.getVector().stream().flatMap(Collection::stream).collect(Collectors.toList()));
                    final var subConfig = new Configuration(spi, removeExtraSymbols(spi.getSymbols(), configuration.getPi()));
                    final var canonical = subConfig.getCanonical();

                    final var badCaseFile = new File(outputDir + "/bad-cases/" + canonical.getSpi());
                    if (badCaseFile.exists()) {
                        continue;
                    }

                    final var sortingFile = new File(outputDir + "/" + canonical.getSpi() + ".html");
                    if (sortingFile.exists()) {
                        final var sorting = ListCases.getSorting(new File(outputDir + "/" + canonical.getSpi() + ".html").toPath());
                        if (!sorting.getSecond().isEmpty()) {
                            return subConfig.translatedSorting(canonical, sorting.getSecond());
                        }
                    }
                }
            }

            return internalSearchSorting(configuration, rootMove);
        }

        @SneakyThrows
        protected List<Cycle> internalSearchSorting(final Configuration configuration, final Move rootMove) {
            int numberOfMoves = rootMove.getHeight();

            final var canonical = configuration.getCanonical();

            final var sortingFile = new File(outputDir + "/comb/" + canonical.getSpi() + ".html");
            if (sortingFile.exists()) {
                return Collections.emptyList();
            }

//            var list = generateAll0And2Moves(configuration.getSpi(), configuration.getPi())
//                    .filter(p -> p.getSecond() == rootMove.mu)
//                    .map(Pair::getFirst)
//                    .collect(toList());
//
//            Collections.shuffle(list);

            var list = generateAll0And2Moves(configuration.getSpi(), configuration.getPi())
                    .filter(p -> p.getSecond() == rootMove.mu)
                    .map(Pair::getFirst)
                    .map(move -> {
                        final var spi_ = computeProduct(true, configuration.getPi().getMaxSymbol() + 1, configuration.getSpi(), move.getInverse());
                        final var pi_ = CommonOperations.applyTransposition(configuration.getPi(), move);
                        final var b_ = getComponents(spi_, pi_).stream().mapToInt(component -> component.stream().mapToInt(Cycle::size).sum()).max();
                        return new Pair<>(b_.getAsInt(), move);
                    }).sorted(Comparator.comparing(o -> ((Pair<Integer, Cycle>) o).getFirst()).reversed()).map(Pair::getSecond).collect(toList());

            var sorting = ListOfCycles.EMPTY_LIST;

            final var canonicalSignatures = new HashSet<String>();

            for (final var move : list) {
                final var spi = new ListOfCycles(configuration.getPi().size());
                computeProduct(configuration.getSpi(), move.getInverse())
                        .stream().map(Cycle::getSymbols).forEach(spi::add);

                final var parity = new boolean[configuration.getPi().size()];
                int[][] spiIndex = new int[configuration.getPi().size()][];

                for (int i = 0; i < spi.size; i++) {
                    final var cycle = spi.elementData[i];
                    for (int s : cycle) {
                        spiIndex[s] = cycle;
                        parity[s] = (cycle.length & 1) == 1;
                    }
                }

                final var removed = removeTrivialCycles(spi);

                final var pi = applyTransposition(configuration.getPi().getSymbols(),
                        move.getSymbols()[0], move.getSymbols()[1], move.getSymbols()[2],
                        configuration.getPi().size() - removed, spiIndex);

                final var canonicalSignature = canonicalSignature(spi, pi, spiIndex, configuration.getPi().getMaxSymbol());
                if (!canonicalSignatures.contains(canonicalSignature)) {
                    for (final var root : rootMove.children) {
                        final var stack = new MovesStack(numberOfMoves);
                        stack.push(move.getSymbols()[0], move.getSymbols()[1], move.getSymbols()[2]);

                        final Cache<String, Set<String>> unsuccessfulConfigs = CacheBuilder.newBuilder()
                                .maximumSize(1_000_000)
                                .build();

                        sorting = SequenceSearcher.search(unsuccessfulConfigs, spi, parity, spiIndex, spiIndex.length, pi, stack, root);
                        if (!sorting.isEmpty()) {
                            return sorting.toList().stream().map(Cycle::create).collect(toList());
                        }
                    }
                    canonicalSignatures.add(canonicalSignature);
                }
            }

            return Collections.emptyList();
        }

        @Override
        protected void extend(final Configuration canonical) {
            if (configuration.get3Norm() >= 12) {
                System.out.println("BAD: Combination does not allow (16/12): " + canonical.getSpi());
                return;
            }

            extendCombinations(configuration).stream().map(extension -> new SortOrExtendCombinations(extension, outputDir)).forEach(ForkJoinTask::fork);
        }

        private static int removeTrivialCycles(final ListOfCycles spi) {
            final var toRemove = new ArrayList<int[]>();
            var removed = 0;
            for (int i = 0; i < spi.size; i++) {
                final var cycle = spi.elementData[i];
                if (cycle.length == 1) {
                    toRemove.add(cycle);
                    removed++;
                }
            }
            spi.removeAll(toRemove);
            return removed;
        }

        private List<Configuration> extendCombinations(final Configuration config) {
            final var result = new ArrayList<Configuration>();
            for (final var badSmallComponent : BAD_SMALL_COMPONENTS) {
                for (int i = 0; i < config.getPi().size(); i++) {
                    final var badSmallComponentSignature = badSmallComponent.getSignature().getContent().clone();
                    for (int j = 0; j < badSmallComponentSignature.length; j++) {
                        badSmallComponentSignature[j] += config.getSpi().size();
                    }
                    final var signature = new FloatArrayList(config.getSignature().getContent().clone());
                    signature.beforeInsertAllOf(i, Floats.asList(badSmallComponentSignature));
                    signature.trimToSize();
                    result.add(Configuration.ofSignature(signature.elements()));
                }
            }
            return result;
        }
    }
}