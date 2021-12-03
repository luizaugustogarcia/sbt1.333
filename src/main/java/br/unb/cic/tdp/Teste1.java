package br.unb.cic.tdp;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import org.apache.commons.lang.time.StopWatch;

import static br.unb.cic.tdp.base.CommonOperations.cycleIndex;

public class Teste1 {
    public static void main(String[] args) {
        final var conf = new Configuration("(0 34 32)(1 29 27)(2 30 28)(3 7 5)(4 8 6)(9 13 11)(10 14 12)(15 19 17)(16 20 18)(21 25 23)(22 26 24)(31 35 33)");

        var s = new StopWatch();
        s.start();

        for (int i = 0; i < 10000; i++) {
//            final var ci = cycleIndex(conf.getSpi(), conf.getPi());
//            final var numberOfEvenCycles = conf.getSpi().getNumberOfEvenCycles();

            final var ci = new Cycle[conf.getSpi().getMaxSymbol() + 1];

            final int[] numberOfEvenCycles = {0};
            conf.getSpi().forEach(cycle -> {
                numberOfEvenCycles[0] += cycle.size() % 2;
                for (final int symbol : cycle.getSymbols()) {
                    ci[symbol] = cycle;
                }
            });
        }

        s.stop();

        System.out.println(s.getTime());
    }
}
