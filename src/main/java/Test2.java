import br.unb.cic.tdp.EliasAndHartman;
import br.unb.cic.tdp.Silvaetal;
import br.unb.cic.tdp.experiments.LongerPermutations;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.permutation.PermutationGroups;
import lombok.SneakyThrows;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static br.unb.cic.tdp.base.CommonOperations.CANONICAL_PI;
import static br.unb.cic.tdp.base.CommonOperations.simplify;

public class Test2 {

    @SneakyThrows
    public static void main(String[] args) {
        FileWriter myWriter = new FileWriter("C:\\Users\\Luiz Silva\\Temp\\eh.txt");
        PrintWriter printWriter = new PrintWriter(myWriter);

        for (int i = 2; i <= 50 ; i++) {
            final var resource = LongerPermutations.class.getResource("/datasets/large" + i * 10 + ".txt");
            final var path = Paths.get(resource.toURI());

            final var algoritm = new EliasAndHartman();

            Files.lines(path).forEach (line -> {
                var pi = Cycle.create(line);
                var sigmaPiInverse = PermutationGroups.computeProduct(CANONICAL_PI[pi.size()],
                        pi.getInverse());
                if (sigmaPiInverse.getNonTrivialCycles().size() == 1) {

                    var simplifiedPi = simplify(pi);
                    var simplifiedSpi = PermutationGroups.computeProduct(CANONICAL_PI[simplifiedPi.size()], simplifiedPi.getInverse());


                    final var _pi = pi.toString().replace("(0 ", "").replace(")", "");
                    var _simplifiedPi = simplifiedPi.toString().replace("(0 ", "").replace(")", "");

                    var low = simplifiedSpi.get3Norm();

                    final var _spi = spiToString(sigmaPiInverse, pi);
                    final var _simplifiedSpi = spiToString(simplifiedSpi, simplifiedPi);

                    final var sorting = algoritm.sort(pi);

                    final var moves = sorting.getSecond().stream().map(move -> {
                        final var _i = simplifiedPi.indexOf(move.get(0));
                        final var j = simplifiedPi.indexOf(move.get(1));
                        final var k = simplifiedPi.indexOf(move.get(2));

                        final var a = new int[]{(_i == 0 ? simplifiedPi.size() : _i),
                                (j == 0 ? simplifiedPi.size() : j),
                                (k == 0 ? simplifiedPi.size() : k)};
                        Arrays.sort(a);
                        return "(" + Arrays.stream(a).mapToObj(s -> Integer.toString(s)).collect(Collectors.joining(",")) + ")";
                    }).collect(Collectors.joining(","));

                    final var _moves = new ArrayList<Integer>();
                    for (Cycle move: sorting.getSecond()) {
                        int _3norm = simplifiedSpi.get3Norm();
                        simplifiedSpi = PermutationGroups.computeProduct(simplifiedSpi, move.getInverse());
                        _moves.add((_3norm - simplifiedSpi.get3Norm()) * 2);
                    }

                    printWriter.println("[" + _pi + "]-{" + _spi + "}-[" + _simplifiedPi + "]-{" + _simplifiedSpi + "}-" + moves + "-" + low + "-" + sorting.getSecond().size() + "-" + _moves.stream().map(s -> Integer.toString(s)).collect(Collectors.joining(",")));
                }
            });
        }
    }

    private static String spiToString(MulticyclePermutation sigmaPiInverse, Cycle pi) {
        return sigmaPiInverse.stream().map(c -> {
            final var cycle = startingByMaxSymbol(Arrays.stream(c.getSymbols())
                    .map(s -> pi.indexOf(s) == 0? pi.size(): pi.indexOf(s)).toArray());

            return "(" + Arrays.stream(cycle).mapToObj(s -> Integer.toString(s)).collect(Collectors.joining(",")) + ")";
        }).collect(Collectors.joining(","));
    }

    public static int[] startingByMaxSymbol(int[] cycle) {
        int index = -1;
        int max = -1;
        for (int i = 0; i < cycle.length; i++) {
            if (cycle[i] > max) {
                max = cycle[i];
                index = i;
            }
        }

        final var symbols = new int[cycle.length];
        System.arraycopy(cycle, index, symbols, 0, symbols.length - index);
        System.arraycopy(cycle, 0, symbols, symbols.length - index, index);

        return symbols;
    }

}
