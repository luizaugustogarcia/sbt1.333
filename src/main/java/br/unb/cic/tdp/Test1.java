package br.unb.cic.tdp;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.proof.seq12_9.Combinations;
import com.google.common.cache.CacheBuilder;

import static br.unb.cic.tdp.FinalPermutations.UNSUCCESSFUL_CONFIGS;
import static br.unb.cic.tdp.FinalPermutations.sort;
import static br.unb.cic.tdp.proof.ProofGenerator._16_12_SEQS;
import static br.unb.cic.tdp.proof.ProofGenerator._8_6_SEQS;

public class Test1 {

    public static void main(String[] args) {
        UNSUCCESSFUL_CONFIGS = CacheBuilder.newBuilder()
                .maximumSize(50_000_000)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

        final var c = new Configuration("(0 21 19 11 3)(1 22 2)(4 7 5)(6 10 8)(9 14 12)(13 17 15)(16 20 18)");
        sort(c, null, _8_6_SEQS);
    }
}
