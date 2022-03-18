package br.unb.cic.tdp.proof.util;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.util.ConfigurationSorter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;

import static br.unb.cic.tdp.base.CommonOperations.getComponents;
import static br.unb.cic.tdp.proof.ProofGenerator.*;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
public abstract class SortOrExtend extends RecursiveAction {
    protected final Configuration configuration;
    protected final String outputDir;
    private static final Map<String, Boolean> workingConfigurations = new ConcurrentHashMap<>();

    @SneakyThrows(IOException.class)
    @Override
    protected void compute() {
        final var canonical = configuration.getCanonical();

        final var sortingFile = new File(outputDir + "/" + canonical.getSpi() + ".html");
        if (sortingFile.exists()) {
            // if it's already sorted, return
            return;
        }

        final var badCaseFile = new File(outputDir + "/bad-cases/" + canonical.getSpi());

        if (!badCaseFile.exists()) {
            try {
                final var previousValue = workingConfigurations.putIfAbsent(canonical.getSpi().toString(), Boolean.TRUE);
                if (previousValue != null && previousValue) {
                    // some thread already is working on this case, skipping
                    return;
                }

                final var sorting = searchForSorting(canonical);
                if (sorting.isPresent()) {
                    try (final var writer = new FileWriter(outputDir + "/" + canonical.getSpi() + ".html")) {
                        renderSorting(canonical, sorting.get(), writer);
                        return;
                    }
                } else {
                    try (final var writer = new FileWriter(outputDir + "/bad-cases/" + canonical.getSpi())) {
                        // create the base case
                    }
                }
            } finally {
                workingConfigurations.remove(canonical.getSpi().toString());
            }
        }

        extend(configuration);
    }

    protected Optional<List<Cycle>> searchForSorting(final Configuration configuration) {
        final var _3norm = configuration.getSpi().get3Norm();

        List<Cycle> sorting = Collections.emptyList();

        String threadName = Thread.currentThread().getName();

        if (_3norm >= 3) {
            Thread.currentThread().setName(configuration.hashCode() + "-" + configuration.getSpi() + "-4,3");
            sorting = searchSorting(configuration, _4_3_SEQS);
        }

        if (_3norm >= 6 && sorting.isEmpty()) {
            Thread.currentThread().setName(configuration.hashCode() + "-" + configuration.getSpi() + "-8,6");
            sorting = searchSorting(configuration, _8_6_SEQS);
        }

        if (_3norm >= 9 && sorting.isEmpty()) {
            Thread.currentThread().setName(configuration.hashCode() + "-" + configuration.getSpi() + "-12,9");
            sorting = searchSorting(configuration, _12_9_SEQS);
        }

        if (_3norm >= 12 && sorting.isEmpty()) {
            Thread.currentThread().setName(configuration.hashCode() + "-" + configuration.getSpi() + "-16,12");
            sorting = searchSorting(configuration, _16_12_SEQS);
        }

        Thread.currentThread().setName(threadName);

        if (!sorting.isEmpty()) {
            return Optional.of(sorting);
        }

        if (configuration.isFull() && getComponents(configuration.getSpi(), configuration.getPi()).size() == 1) {
            System.out.println("Full configuration without (12/9): " + configuration.getCanonical().getSpi());
        }

        return Optional.empty();
    }

    protected List<Cycle> searchSorting(final Configuration configuration, final Move rootMove) {
        final var spi = new ListOfCycles(configuration.getPi().size());
        configuration.getSpi().stream().map(Cycle::getSymbols).forEach(spi::add);

        final var parity = new boolean[configuration.getPi().size()];
        int[][] spiIndex = new int[configuration.getPi().size()][];

        for (int i = 0; i < spi.size; i++) {
            final var cycle = spi.elementData[i];
            for (int s : cycle) {
                spiIndex[s] = cycle;
                parity[s] = (cycle.length & 1) == 1;
            }
        }

        final var pi = configuration.getPi().getSymbols();

        final var stack = new Stack(rootMove.getHeight());

        return SortingSequenceSearcher.search(null, spi, parity, spiIndex, spiIndex.length, pi, stack, rootMove)
                .toList().stream().map(Cycle::create).collect(toList());
    }

    protected abstract void extend(final Configuration configuration);
}