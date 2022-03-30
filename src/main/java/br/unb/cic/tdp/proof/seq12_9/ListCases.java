package br.unb.cic.tdp.proof.seq12_9;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.util.Pair;
import com.google.common.base.Throwables;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static br.unb.cic.tdp.base.CommonOperations.is16_12;

public class ListCases {

    public static void main(String[] args) throws IOException {
        list("C:\\Users\\Luiz Silva\\Temp\\proof1.333\\dfs\\", "C:\\Users\\Luiz Silva\\Projects\\tdp1375-1.333\\src\\main\\resources\\cases\\cases-dfs.txt");
        //list("C:\\Users\\Luiz Silva\\Temp\\proof1.333\\comb\\", "C:\\Users\\Luiz Silva\\Projects\\tdp1375\\src\\main\\resources\\cases\\cases-comb2.txt");
    }

    public static void list(final String inputDir, final String outputFile) throws IOException {
        try (final var writer = new BufferedWriter(new PrintWriter(outputFile), 1024 * 10)) {
            final var dir = new File(inputDir);
            File[] files = dir.listFiles(file -> file.getName().endsWith(".html"));

            if (files != null && files.length > 0)
                Arrays.stream(files)
                        .forEach(file -> {
                            try {
                                final var reader = new BufferedReader(new FileReader(file), 1024 * 10);
                                var line = reader.readLine();
                                MulticyclePermutation spi = null;
                                while ((line = reader.readLine()) != null) {
                                    line = line.trim();

                                    if (line.startsWith("<h6>")) {
                                        spi = new MulticyclePermutation(line.trim().replace("<h6>", "")
                                                .replace("</h6>", "").replace(" ", ","));
                                    }
                                    if (line.equals("THE EXTENSIONS ARE: </div>")) {
                                        return;
                                    }
                                    final var sorting = new ArrayList<Cycle>();
                                    if (line.trim().equals("ALLOWS (12/9)-SEQUENCE")) {
                                        while ((line = reader.readLine()) != null) {
                                            line = line.trim();

                                            if (!line.equals("<div style=\"margin-top: 10px; \">")) {
                                                continue;
                                            }

                                            line = reader.readLine();

                                            final var move = line.split(": ")[1].replace(" ", ",")
                                                    .replace("<br>", "");
                                            sorting.add(Cycle.create(move));
                                        }
                                        is16_12(spi, CommonOperations.CANONICAL_PI[spi.getNumberOfSymbols()], sorting);
                                        writer.append(spi + "->" + sorting + "\n");
                                    }
                                }
                            } catch (IOException e) {
                                Throwables.propagate(e);
                            }
                        });
        }
    }

    public static boolean isBadExtension(final File file) {
        final BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file), 1024 * 10);
            var line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.equals("THE EXTENSIONS ARE: </div>")) {
                    return true;
                }

                if (line.trim().equals("ALLOWS (12/9)-SEQUENCE")) {
                    return false;
                }
            }
        } catch (Throwable e) {
            Throwables.propagate(e);
        }

        throw new IllegalStateException("Unknown file");
    }

    @SneakyThrows
    public static Pair<MulticyclePermutation, List<Cycle>> getSorting(final Path path) {
        final var reader = new BufferedReader(new FileReader(path.toFile()), 1024 * 10);
        var line = reader.readLine();
        MulticyclePermutation spi = null;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.startsWith("<h6>")) {
                spi = new MulticyclePermutation(line.trim().replace("<h6>", "")
                        .replace("</h6>", "").replace(" ", ","));
            }

            if (line.equals("THE EXTENSIONS ARE: </div>")) {
                return new Pair<>(spi, null);
            }

            final var sorting = new ArrayList<Cycle>();
            if (line.trim().equals("ALLOWS (12/9)-SEQUENCE")) {
                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (!line.equals("<div style=\"margin-top: 10px; \">")) {
                        continue;
                    }

                    line = reader.readLine();

                    final var move = line.split(": ")[1].replace(" ", ",")
                            .replace("<br>", "");
                    sorting.add(Cycle.create(move));
                }
                return new Pair<>(spi, sorting);
            }
        }

        throw new IllegalStateException("Unknown file " + path.toFile());
    }
}
