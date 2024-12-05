package br.unb.cic.tdp.proof.util;

import static br.unb.cic.tdp.base.CommonOperations.getComponents;
import static br.unb.cic.tdp.proof.ProofGenerator.*;
import static java.util.stream.Collectors.toList;

import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@AllArgsConstructor
public abstract class AbstractSortOrExtend extends RecursiveAction {

    private static final Map<String, Boolean> ONGOING_SORTINGS = new ConcurrentHashMap<>();

    protected final int maxExtension;

    protected final DataSource dataSource;

    protected final Configuration configuration;

    @SneakyThrows
    @Override
    protected void compute() {
        val canonical = configuration.getCanonical();

        val canonicalSpi = canonical.getSpi().toString();

        val isSortingOngoing = ONGOING_SORTINGS.putIfAbsent(canonicalSpi, Boolean.TRUE) != null;

        if (isSortingOngoing) {
            // some thread is already working on this case, thus, skipping
            return;
        }

        if (isAlreadySorted(canonicalSpi)) {
            return;
        }

        if (!isBadCase(canonicalSpi)) {
            try {
                val sorting = searchForSorting(canonical);
                if (sorting.isPresent()) {
                    try (val writer = new StringWriter()) {
                        //renderSorting(canonical, sorting.get(), writer);
                        saveSorting(canonicalSpi, sorting.toString());
                        return;
                    }
                } else {
                    saveBadCase(canonicalSpi);
                }
            } finally {
                ONGOING_SORTINGS.remove(canonicalSpi);
            }
        }

        extend(configuration);
    }

    private boolean isAlreadySorted(final String canonicalSpi) throws SQLException {
        try (val conn = dataSource.getConnection()) {
            val runner = new QueryRunner();
            return runner.query(conn, "select 1 from dfs where config = ? and not sorting is null",
                    canonicalSpi, new ScalarHandler<>()) != null || runner.query(conn,
                    "select 1 from full_configs_without_sorting where config = ?", canonicalSpi,
                    new ScalarHandler<>()) != null;
        }
    }

    @SneakyThrows
    private void saveSorting(final String canonicalSpi, final String sorting) {
        try (val conn = dataSource.getConnection()) {
            val runner = new QueryRunner();
            runner.execute(conn, "insert into dfs(config, sorting) values (?,?)", canonicalSpi, sorting);
        }
    }

    @SneakyThrows
    private void saveBadCase(final String canonicalSpi) {
        try (val conn = dataSource.getConnection()) {
            val runner = new QueryRunner();
            runner.execute(conn, "insert into dfs_bad_cases(config) values (?)", canonicalSpi);
        }
    }

    @SneakyThrows
    private boolean isBadCase(final String canonicalSpi) {
        try (val conn = dataSource.getConnection()) {
            val runner = new QueryRunner();
            return runner.query(conn, "select 1 from dfs_bad_cases where config = ?",
                    canonicalSpi, new ScalarHandler<>()) != null;
        }
    }

    protected Optional<List<Cycle>> searchForSorting(final Configuration configuration) {
        val _3norm = configuration.getSpi().get3Norm();

        var sorting = Collections.<Cycle>emptyList();

        val threadName = Thread.currentThread().getName();

        if (_3norm >= 4) {
            Thread.currentThread().setName(configuration.hashCode() + "-" + configuration.getSpi() + "-4,3");
            sorting = searchSorting(configuration, _5_4_SEQS);
        }

        if (_3norm >= 8 && sorting.isEmpty()) {
            Thread.currentThread().setName(configuration.hashCode() + "-" + configuration.getSpi() + "-8,6");
            sorting = searchSorting(configuration, _10_8_SEQS);
        }

        if (_3norm >= 12 && sorting.isEmpty()) {
            Thread.currentThread().setName(configuration.hashCode() + "-" + configuration.getSpi() + "-12,9");
            sorting = searchSorting(configuration, _15_12_SEQS);
        }

        Thread.currentThread().setName(threadName);

        if (!sorting.isEmpty()) {
            return Optional.of(sorting);
        }

        if (configuration.isFull() && getComponents(configuration.getSpi(), configuration.getPi()).size() == 1) {
            saveFullConfigWithoutSorting(configuration.getCanonical());
            System.out.println("Full configuration without (15/12): " + configuration.getCanonical().getSpi());
        }

        return Optional.empty();
    }

    @SneakyThrows
    private void saveFullConfigWithoutSorting(final Configuration canonical) {
        try (val conn = dataSource.getConnection()) {
            val runner = new QueryRunner();
            runner.execute(conn, "insert into full_configs_without_sorting(config) values (?)",
                    canonical.getSpi().toString());
        }
    }

    protected List<Cycle> searchSorting(final Configuration configuration, final Move rootMove) {
        val spi = new ListOfCycles(configuration.getPi().size());
        configuration.getSpi().stream().map(Cycle::getSymbols).forEach(spi::add);

        val parity = new boolean[configuration.getPi().size()];
        val spiIndex = new int[configuration.getPi().size()][];

        for (var i = 0; i < spi.size; i++) {
            val cycle = spi.elementData[i];
            for (val s : cycle) {
                spiIndex[s] = cycle;
                parity[s] = (cycle.length & 1) == 1;
            }
        }

        val pi = configuration.getPi().getSymbols();

        val stack = new Stack(rootMove.getHeight());

        return SortingSequenceSearcher.search(null, spi, parity, spiIndex, spiIndex.length, pi, stack, rootMove)
                .toList().stream().map(Cycle::of).collect(toList());
    }

    private void extend(final Configuration configuration) {
        if (configuration.get3Norm() > maxExtension) {
            System.err.println("Configuration too big");
            System.exit(1);
        }
        doExtend(configuration);
    }

    protected abstract void doExtend(Configuration configuration);
}