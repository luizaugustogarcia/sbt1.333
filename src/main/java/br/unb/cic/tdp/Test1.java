package br.unb.cic.tdp;

import br.unb.cic.tdp.base.Configuration;
import com.google.common.cache.CacheBuilder;

import static br.unb.cic.tdp.FinalPermutations.UNSUCCESSFUL_CONFIGS;
import static br.unb.cic.tdp.FinalPermutations.sort;
import static br.unb.cic.tdp.proof.ProofGenerator.*;

public class Test1 {

    public static void main(String[] args) {
        UNSUCCESSFUL_CONFIGS = CacheBuilder.newBuilder()
                .maximumSize(20_000_000)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

        final var c = new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(18 22 20)(19 41 21)(23 39 25)(24 28 26)(27 31 29)(30 34 32)(33 37 35)(36 40 38)");
        sort(c, null, _16_12_SEQS);
    }
}
