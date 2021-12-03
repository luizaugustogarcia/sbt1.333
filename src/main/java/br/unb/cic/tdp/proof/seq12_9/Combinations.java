package br.unb.cic.tdp.proof.seq12_9;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.util.Pair;
import cern.colt.list.FloatArrayList;
import com.google.common.primitives.Floats;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static br.unb.cic.tdp.base.CommonOperations.combinations;
import static br.unb.cic.tdp.base.CommonOperations.getComponents;
import static br.unb.cic.tdp.proof.ProofGenerator.*;
import static br.unb.cic.tdp.proof.seq12_9.Extensions.cleanUpBadExtensionAndInvalidFiles;

public class Combinations {

    private static final Map<Integer, Set<Configuration>> incompleteConfigs = new HashMap<>();

    private static final SearchForSortingStrategy DEFAULT_STRATEGY = new DefaultSearchForSortingStrategy();

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
        Files.createDirectories(Paths.get(outputDir + "/comb/working/"));
        Files.createDirectories(Paths.get(outputDir + "/comb/bad-cases/"));

        //cleanUpIncompleteCases(outputDir + "/comb/");

        //cleanUpBadExtensionAndInvalidFiles(outputDir + "/comb/");

        //includeBadSmallCasesInBadCases(outputDir + "/comb/");

        // ATTENTION: The Sort Or Extend fork/join can never run with BAD EXTENSION files in the comb directory.
        // Otherwise, it will skip cases.

        final Queue<Configuration> notSortedOrExtended = new LinkedList<>(Arrays.asList(BAD_SMALL_COMPONENTS));

        while (!notSortedOrExtended.isEmpty()) {
            final var drainedConfigs = new ArrayList<Configuration>();
            while (!notSortedOrExtended.isEmpty()) {
                drainedConfigs.add(notSortedOrExtended.remove());
            }

            final var forkJoinPool = new ForkJoinPool();
            drainedConfigs.forEach(configuration -> forkJoinPool.execute(new SortOrExtend(configuration, outputDir, notSortedOrExtended)));
            forkJoinPool.shutdown();
            forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            System.out.println("Wait a bit to continue");
            Thread.sleep(1000 * 60 * 10);
        }

        var forkJoinPool = new ForkJoinPool();
        Arrays.stream(BAD_SMALL_COMPONENTS).forEach(c -> forkJoinPool.execute(new MakeHtmlNavigation(c, outputDir)));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        System.out.println(incompleteConfigs.keySet());
        System.out.println(incompleteConfigs.values().size());
        incompleteConfigs.values().stream().flatMap(Collection::stream).map(Configuration::getSpi).forEach(System.out::println);

//        final var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        incompleteConfigs.forEach((key, value) -> value.forEach(configuration -> {
//            executorService.submit(() -> {
//                final var sorting = searchForSorting(configuration, new DefaultSearchForSortingStrategy());
//                final var canonical = configuration.getCanonical();
//                System.out.println("Sorted " + canonical.getSpi() + "-" + sorting);
//                try (final var out = new FileWriter(outputDir + "/comb/" + canonical.getSpi() + ".html")) {
//                    renderSorting(canonical, sorting.get(), out);
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }
//            });
//        }));
//
//        executorService.shutdown();
//        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    private static void includeBadSmallCasesInBadCases(final String outputDir) {
        Arrays.stream(BAD_SMALL_COMPONENTS).forEach(c -> {
            try (final var writer = new FileWriter(outputDir + "/bad-cases/" + c.getCanonical().getSpi())) {
                // create the base case
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
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
    static class SortOrExtend extends RecursiveAction {
        final Configuration config;
        final String outputDir;
        final Queue<Configuration> notSortedOrExtended;

        @Override
        protected void compute() {
            final var canonical = config.getCanonical();

            final var sortingFile = new File(outputDir + "/comb/" + canonical.getSpi() + ".html");
            if (sortingFile.exists()) {
                // if it's already sorted, return
                return;
            }

            final var badCaseFile = new File(outputDir + "/comb/bad-cases/" + canonical.getSpi());

            if (!badCaseFile.exists()) {
                final var workingFile = new File(outputDir + "/comb/working/" + canonical.getSpi());
                if (workingFile.exists()) {
                    // some thread already is working on this case, skipping
                    return;
                }

                try (final var writer = new FileWriter(workingFile)) {
                    final var sorting = searchForSortingSubCombinations(config, outputDir + "/comb");
                    if (!sorting.getFirst()) {
                        if (!sorting.getSecond().isEmpty()) {
                            System.out.println("Sorted " + config.getSpi() + "-" + sorting);
                            try (final var out = new FileWriter(outputDir + "/comb/" + config.getSpi() + ".html")) {
                                renderSorting(config, sorting.getSecond(), out);
                                return;
                            }
                        } else {
                            try (final var w = new FileWriter(outputDir + "/comb/bad-cases/" + canonical.getSpi())) {
                                // create the bad case
                            }
                        }
                    } else {
                        synchronized (notSortedOrExtended) {
                            // this configuration is not ready yet to be sorted. Other threads are working in the same case
                            notSortedOrExtended.offer(config);
                        }
                        return;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                } finally {
                    workingFile.delete();
                }
            }

            if (config.get3Norm() >= 12) {
                System.out.println("Combination does not allow (16/12): " + canonical.getSpi());
                System.exit(1);
            }

            extend(config).stream().map(extension -> new SortOrExtend(extension.getSecond(), outputDir, notSortedOrExtended)).forEach(ForkJoinTask::fork);
        }
    }

    public static Pair<Boolean, List<Cycle>> searchForSortingSubCombinations(final Configuration config, final String outputDir) throws InterruptedException, IOException {
        final var smallComponents = getComponents(config.getSpi(), config.getPi());
        boolean subCombinationsInWorkingState = false;
        for (int i = 2; i < smallComponents.size(); i++) {
            for (final var components : combinations(smallComponents, i)) {
                final var spi = new MulticyclePermutation(components.getVector().stream().flatMap(Collection::stream).collect(Collectors.toList()));
                final var subConfig = new Configuration(spi, removeExtraSymbols(spi.getSymbols(), config.getPi()));
                final var canonical = subConfig.getCanonical();

                final var badCaseFile = new File(outputDir + "/bad-cases/" + canonical.getSpi());
                if (badCaseFile.exists()) {
                    continue;
                }

                final var sortingFile = new File(outputDir + "/" + canonical.getSpi() + ".html");
                if (sortingFile.exists()) {
                    int wait = 0;
                    while (wait <= 2) {
                        try {
                            final var sorting = ListCases.getSorting(new File(outputDir + "/" + canonical.getSpi() + ".html").toPath());
                            if (!sorting.getSecond().isEmpty()) {
                                final var translatedSorting = subConfig.translatedSorting(canonical, sorting.getSecond());
                                return new Pair<>(Boolean.FALSE, translatedSorting);
                            }
                        } catch (IllegalStateException e) {
                            // wait until for the other thread (maybe in another machine) to finish the sorting rendering
                            Thread.sleep(2000);
                            wait++;
                        }
                    }
                }

                final var workingFile = new File(outputDir + "/working/" + canonical.getSpi());
                if (workingFile.exists()) {
                    subCombinationsInWorkingState = true;
                    continue;
                }

                try (final var writer = new FileWriter(workingFile)) {
                    final Optional<List<Cycle>> sorting = searchForSorting(subConfig, new MaximizeSizeOfComponentsStrategy());
                    if (sorting.isPresent()) {
                        try (final var out = new FileWriter(outputDir + "/" + canonical.getSpi() + ".html")) {
                            final var translatedSorting = canonical.translatedSorting(subConfig, sorting.get());
                            renderSorting(canonical, translatedSorting, out);
                            return new Pair<>(Boolean.FALSE, sorting.get());
                        }
                    } else {
                        try (final var w = new FileWriter(outputDir + "/bad-cases/" + canonical.getSpi())) {
                            // create the bad case
                        }
                    }
                } finally {
                    workingFile.delete();
                }
            }
        }

        if (subCombinationsInWorkingState) {
            return new Pair<>(Boolean.TRUE, Collections.emptyList());
        }

        var sorting = searchForSorting(config, new MaximizeSizeOfComponentsStrategy());
        if (sorting.isEmpty()) {
            sorting = searchForSorting(config, DEFAULT_STRATEGY);
        }

        return sorting.map(cycles -> new Pair<>(Boolean.FALSE, cycles)).orElseGet(() -> new Pair<>(Boolean.FALSE, Collections.emptyList()));
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
}