package br.unb.cic.tdp.proof.seq15_12;

import static br.unb.cic.tdp.base.CommonOperations.cycleIndex;
import static br.unb.cic.tdp.base.Configuration.ofSignature;
import static br.unb.cic.tdp.base.Configuration.signature;
import static br.unb.cic.tdp.proof.ProofGenerator.permutationToJsArray;
import static java.lang.Math.floor;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.google.common.base.Preconditions;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.util.AbstractSortOrExtend;
import br.unb.cic.tdp.util.Pair;
import br.unb.cic.tdp.util.PoolingDataSource;
import cern.colt.list.FloatArrayList;
import lombok.SneakyThrows;
import lombok.val;

public class Extensions {

    private static DataSource dataSource;

    @SneakyThrows
    public static void generate(final String outputDir) {
        dataSource = PoolingDataSource.get(outputDir + "/extensions-db");

        try (val conn = dataSource.getConnection()) {
            val runner = new QueryRunner();
            runner.execute(conn, "create table IF NOT EXISTS full_configs_without_sorting(config varchar(255) primary key)");
            runner.execute(conn, "create table IF NOT EXISTS dfs(config varchar(255) primary key, sorting varchar(1000))");
            runner.execute(conn, "create table IF NOT EXISTS dfs_bad_cases(config varchar(255) primary key)");
        }

        val maxExtension = 12;

        try (var pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors())) {
            // oriented 5-cycle
            pool.submit(new SortOrExtend(maxExtension, dataSource, new Configuration(new MulticyclePermutation("(0,3,1,4,2)"))));
            // interleaving pair
            pool.submit(
                    new SortOrExtend(maxExtension, dataSource, new Configuration(new MulticyclePermutation("(0,4,2)(1,5,3)"))));
            // intersecting pair
            pool.submit(
                    new SortOrExtend(maxExtension, dataSource, new Configuration(new MulticyclePermutation("(0,3,1)(2,5,4)"))));
        }

        try (val executor = Executors.newCachedThreadPool()) {
//            Files.list(dir)
//                    .map(Path::toFile)
//                    .forEach(file -> executor.submit(() -> makeHtmlNavigation(new Configuration(new MulticyclePermutation(file.getName())), outputDir)));
        }
    }

    @SneakyThrows
    private static void renderExtensions(final List<Pair<String, Configuration>> extensions, final PrintStream out,
            final String outputDir) {
        for (val extension : extensions) {
            val configuration = extension.getSecond();
            val canonical = extension.getSecond().getCanonical();

            var hasSorting = hasSorting(canonical);

            out.println(hasSorting ? "<div style=\"margin-top: 10px; background-color: rgba(153, 255, 153, 0.15)\">" :
                    "<div style=\"margin-top: 10px; background-color: rgba(255, 0, 0, 0.05);\">");
            out.println(extension.getFirst() + "<br>");
            out.println(((hasSorting ? "GOOD" : "BAD") + " EXTENSION") + "<br>");
            out.println("Hash code: " + configuration.hashCode() + "<br>");
            out.println("3-norm: " + configuration.getSpi().get3Norm() + "<br>");
            out.println("Signature: " + configuration.getSignature() + "<br>");
            val jsSpi = permutationToJsArray(configuration.getSpi());
            out.printf("Extension: <a href=\"\" " +
                            "onclick=\"" +
                            "updateCanvas('modalCanvas', %s); " +
                            "$('h6.modal-title').text('%s');" +
                            "$('#modal').modal('show'); " +
                            "return false;\">%s</a><br>%n",
                    jsSpi, configuration.getSpi(), configuration.getSpi());
            out.printf("View canonical extension: <a href=\"%s.html\">%s</a>%n", canonical.getSpi(), canonical.getSpi());
            out.println("</div>");
        }
    }

    private static boolean hasSorting(Configuration canonical) throws SQLException {
        try (val conn = dataSource.getConnection()) {
            val runner = new QueryRunner();
            return runner.query(conn, "select 1 from dfs_bad_cases where config = ?",
                    canonical.getSpi(), new ScalarHandler<>()) != null;
        }
    }

    /*
     * Type 1 extension.
     */
    private static List<Pair<String, Configuration>> type1Extensions(final Configuration config) {
        val result = new ArrayList<Pair<String, Configuration>>();

        val newCycleLabel = config.getSpi().size() + 1;

        val signature = signature(config.getSpi(), config.getPi());

        for (var i = 0; i < signature.length; i++) {
            if (config.getOpenGates().contains(i)) {
                for (var b = 0; b < signature.length; b++) {
                    for (var c = b; c < signature.length; c++) {
                        if (!(i == b && b == c)) {
                            result.add(new Pair<>(String.format("a=%d b=%d c=%d", i, b, c),
                                    ofSignature(unorientedExtension(signature, newCycleLabel, i, b, c).elements())));
                        }
                    }
                }
            }
        }

        return result;
    }

    /*
     * Type 2 extension.
     */
    private static List<Pair<String, Configuration>> type2Extensions(final Configuration config) {
        if (!config.isFull()) {
            return Collections.emptyList();
        }

        val result = new ArrayList<Pair<String, Configuration>>();

        val newCycleLabel = config.getSpi().size() + 1;

        val signature = signature(config.getSpi(), config.getPi());

        for (var a = 0; a < signature.length; a++) {
            for (var b = a; b < signature.length; b++) {
                for (var c = b; c < signature.length; c++) {
                    if (!(a == b && b == c)) {
                        result.add(new Pair<>(String.format("a=%d b=%d c=%d", a, b, c),
                                ofSignature(unorientedExtension(signature, newCycleLabel, a, b, c).elements())));
                    }
                }
            }
        }

        return result;
    }

    /*
     * Type 3 extension.
     */
    private static List<Pair<String, Configuration>> type3Extensions(final Configuration config) {
        val result = new ArrayList<Pair<String, Configuration>>();

        val signature = signature(config.getSpi(), config.getPi());
        val _cyclesSizes = new HashMap<Integer, Integer>();
        val indexesByLabel = new HashMap<Integer, List<Integer>>();
        for (var i = 0; i < signature.length; i++) {
            _cyclesSizes.putIfAbsent((int) floor(signature[i]), 0);
            _cyclesSizes.computeIfPresent((int) floor(signature[i]), (k, v) -> v + 1);
            indexesByLabel.computeIfAbsent((int) floor(signature[i]), _s -> new ArrayList<>());
            int finalI = i;
            indexesByLabel.computeIfPresent((int) floor(signature[i]), (k, v) -> {
                v.add(finalI);
                return v;
            });
        }

        val cycleIndex = cycleIndex(config.getSpi(), config.getPi());
        val cyclesByLabel = new HashMap<Integer, Cycle>();
        for (var i = 0; i < signature.length; i++) {
            val _i = i;
            cyclesByLabel.computeIfAbsent((int) floor(signature[i]), k -> cycleIndex[config.getPi().get(_i)]);
        }

        for (var label = 1; label <= config.getSpi().size(); label++) {
            if (!isOriented(signature, label)) {
                for (var a = 0; a < signature.length; a++) {
                    for (var b = a; b < signature.length; b++) {
                        val extendedSignature = unorientedExtension(signature, label, a, b).elements();

                        val extension = ofSignature(extendedSignature);

                        if (remainsUnoriented(indexesByLabel.get(label), a, b)) {
                            if (extension.getOpenGates().size() <= 2) {
                                result.add(new Pair<>(String.format("a=%d b=%d, extended cycle: %s", a, b, cyclesByLabel.get(label)),
                                        extension));
                            }
                        } else if (_cyclesSizes.get(label) == 3) {
                            val extension_ = extend(cyclesByLabel, label, signature, a, b);
                            val fractions = new float[] { 0.1F, 0.3F, 0.5F, 0.2F, 0.4F };
                            for (var i = 0; i < fractions.length; i++) {
                                fractions[i] += label;
                            }

                            if (areSymbolsInCyclicOrder(extension_, fractions)) { // otherwise, it accepts a 2-move
                                result.add(new Pair<>(String.format("a=%d b=%d, extended cycle: %s, turn oriented", a, b,
                                        cyclesByLabel.get(label)), Configuration.ofSignature(extension_)));
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public static boolean areSymbolsInCyclicOrder(final float[] elements, final float[] other) {
        int next = 0;

        outer:
        for (var i = 0; i < elements.length; i++) {
            if (elements[i] == other[next]) {
                for (var j = 0; j <= elements.length; j++) {
                    int index = (i + j) % elements.length;
                    if (floor(elements[index]) == floor(other[next % other.length])) {
                        if (elements[index] == other[next % other.length]) {
                            next++;
                            if (next > other.length) {
                                break outer;
                            }
                        } else {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private static float[] extend(
            final Map<Integer, Cycle> cyclesByLabel,
            final int label,
            float[] signature,
            final int a,
            final int b
    ) {
        final Cycle cycle = cyclesByLabel.get(label).startingBy(cyclesByLabel.get(label).getMaxSymbol());

        float[] copiedSignature = new float[signature.length];
        System.arraycopy(signature, 0, copiedSignature, 0, signature.length);

        for (var i = cycle.getSymbols().length - 1; i >= 0; i--) {
            copiedSignature[cycle.getSymbols()[i]] += 0.1 * (i + 1);
        }

        float next = 0.5f;
        val positions = new int[] { a, b };
        val extension = new FloatArrayList(copiedSignature);
        int inserted = 0;
        for (var position : positions) {
            extension.beforeInsert(position + inserted, label + next);
            next -= 0.1f;
            inserted++;
        }
        extension.trimToSize();

        return extension.elements();
    }

    private static boolean remainsUnoriented(final List<Integer> indexes, final int... newIndices) {
        val intervals = new HashSet<Pair<Integer, Integer>>();

        for (val index : newIndices) {
            for (var i = 0; i < indexes.size(); i++) {
                int left = indexes.get(i), right = indexes.get((i + 1) % indexes.size());
                if ((left < index && index <= right) ||
                        (right < left && (left < index || index <= right))) {
                    intervals.add(new Pair<>(left, right));
                }
            }
        }

        return intervals.size() == 1;
    }

    private static boolean isOriented(float[] signature, int label) {
        for (float s : signature) {
            if (s % 1 > 0 && floor(s) == label) {
                return true;
            }
        }
        return false;
    }

    private static FloatArrayList unorientedExtension(final float[] signature, final int label, final int... positions) {
        Preconditions.checkArgument(1 < positions.length && positions.length <= 3);
        Arrays.sort(positions);
        val extension = new FloatArrayList(signature);
        for (var i = 0; i < positions.length; i++) {
            extension.beforeInsert(positions[i] + i, label);
        }
        extension.trimToSize();
        return extension;
    }

    @SneakyThrows
    private static void makeHtmlNavigation(final Configuration configuration, final String outputDir) {
        try (val out = new PrintStream(outputDir + "/dfs/" + configuration.getSpi() + ".html")) {
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
            out.println("Open gates: " + configuration.getOpenGates() + "<br>");
            out.println("Signature: " + configuration.getSignature() + "<br>");
            out.println("3-norm: " + configuration.getSpi().get3Norm());

            out.println("<p style=\"margin-top: 10px;\"></p>");
            out.println("THE EXTENSIONS ARE:");

            out.println("<table style=\"width:100%; border: 1px solid lightgray; border-collapse: collapse;\">");
            out.println("  <tr>");
            out.println("    <th style=\"text-align: start; border: 1px solid lightgray;\">Type 1</th>");
            out.println("    <th style=\"text-align: start; border: 1px solid lightgray;\">Type 2</th>");
            out.println("    <th style=\"text-align: start; border: 1px solid lightgray;\">Type 3</th>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <td style=\"vertical-align: baseline; border: 1px solid lightgray;\">");
            renderExtensions(type1Extensions(configuration), out, outputDir);
            out.println("    </td>");
            out.println("    <td style=\"vertical-align: baseline; border: 1px solid lightgray;\">");
            renderExtensions(type2Extensions(configuration), out, outputDir);
            out.println("    </td>");
            out.println("    <td style=\"vertical-align: baseline; border: 1px solid lightgray;\">");
            renderExtensions(type3Extensions(configuration), out, outputDir);
            out.println("    </td>");
            out.println("  </tr>");
            out.println("</table>");

            out.println("</body>");
            out.println("</html>");
        }
    }

    static class SortOrExtend extends AbstractSortOrExtend {

        public SortOrExtend(final int maxExtension, final DataSource dataSource, final Configuration configuration) {
            super(maxExtension, dataSource, configuration);
        }

        @Override
        protected void doExtend(final Configuration canonical) {
            Stream.of(type1Extensions(canonical), type2Extensions(canonical), type3Extensions(canonical))
                    .flatMap(Collection::stream)
                    .map(extension -> new SortOrExtend(maxExtension, dataSource, extension.getSecond()))
                    .forEach(ForkJoinTask::fork);
        }
    }
}