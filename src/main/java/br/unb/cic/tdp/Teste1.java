package br.unb.cic.tdp;

import br.unb.cic.tdp.base.Configuration;
import org.apache.commons.lang.time.StopWatch;
import org.apache.velocity.app.Velocity;

import java.util.stream.Stream;

import static br.unb.cic.tdp.FinalPermutations.sort;
import static br.unb.cic.tdp.proof.ProofGenerator.*;

public class Teste1 {
    public static void main(String[] args) {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        var s = new StopWatch();
        s.start();
        Stream.of(
                new Configuration("(0 34 20)(1 35 21)(2 6 4)(3 7 5)(8 12 10)(9 13 11)(14 18 16)(15 19 17)(22 32 30)(23 33 31)(24 28 26)(25 29 27)")
        ).forEach(conf -> sort(conf, "C:/Users/Luiz Silva/Temp", _16_12_SEQS));
        s.stop();
        System.out.println(s.getTime());

    }
}
