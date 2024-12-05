package br.unb.cic.tdp.base;

import br.unb.cic.tdp.permutation.Cycle;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static br.unb.cic.tdp.base.CommonOperations.applyTransposition;
import static br.unb.cic.tdp.base.CommonOperations.areSymbolsInCyclicOrder;
import static br.unb.cic.tdp.base.CommonOperations.isOriented;
import static br.unb.cic.tdp.base.CommonOperations.simplify;
import static org.junit.jupiter.api.Assertions.*;

class CommonOperationsTest {
    final private Cycle alpha = Cycle.of("0 2 7");
    final private Cycle pi = Cycle.of("0 5 4 3 8 7 6 2 1");

    @Test
    void testSimplify() {
        assertEquals(Cycle.of("0 4 8 3 7 2 6 1 5 9 14 13 12 11 10"), simplify(Cycle.of("0 3 6 2 5 1 4 10 9 8 7")));
    }

    @Test
    void testApplyTransposition() {
        assertEquals(Cycle.of("0 1 2 3 4 5 6"), applyTransposition(Cycle.of("0 4 5 6 1 2 3"), Cycle.of("0 4 1")));
    }

    @Test
    void testCycleIndex() {
        final var pi = Cycle.of("0 5 4 3 2 1");
        final var c0 = Cycle.of("0 2 4");
        final var c1 = Cycle.of("1 3 5");

        final var index = CommonOperations.cycleIndex(Arrays.asList(c0, c1), pi);
        assertArrayEquals(new Cycle[]{c0, c1, c0, c1, c0, c1}, index);
    }

    @Test
    void testAreSymbolsInCyclicOrder() {
        assertTrue(areSymbolsInCyclicOrder(pi.getInverse(), alpha.getSymbols()));
        assertFalse(areSymbolsInCyclicOrder(pi, alpha.getSymbols()));
    }

    @Test
    void testIsOriented() {
        assertTrue(isOriented(pi, alpha.getInverse()));
        assertFalse(isOriented(pi, alpha));
    }
}