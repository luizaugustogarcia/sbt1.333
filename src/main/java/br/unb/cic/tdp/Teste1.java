package br.unb.cic.tdp;

import br.unb.cic.tdp.base.Configuration;
import org.apache.commons.lang.time.StopWatch;
import org.apache.velocity.app.Velocity;

import java.util.concurrent.ExecutionException;
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
                new Configuration("(0 28 2)(1 35 27)(3 25 5)(4 26 24)(6 10 8)(7 11 9)(12 22 14)(13 23 21)(15 19 17)(16 20 18)(29 33 31)(30 34 32)")
        ).forEach(conf -> {
            sort(conf, "C:/Users/Luiz Silva/Temp", _16_12_SEQS);
        });
        s.stop();
        System.out.println(s.getTime());

    }
}
