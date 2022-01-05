package br.unb.cic.tdp;

import br.unb.cic.tdp.base.Configuration;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.velocity.app.Velocity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static br.unb.cic.tdp.FinalPermutations.*;
import static br.unb.cic.tdp.proof.ProofGenerator.*;

public class Teste1 {

    public static void main(String[] args) {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        UNSUCCESSFUL_CONFIGS = CacheBuilder.newBuilder()
                .maximumSize(Integer.parseInt(args[0]))
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

        final var timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("Cache size: " + UNSUCCESSFUL_CONFIGS.size());
                System.out.println("Cache hits: " + hits);
                System.out.println("Cache misses: " + misses);
                System.out.println("Cache hit rate: " + String.format("%.2f", 1 - (misses.get() / (float) hits.get())));
                long heapSize = Runtime.getRuntime().totalMemory();
                System.out.println("Heap size GB: " + (((heapSize / 1024) / 1024) / 1024));
                long heapFreeSize = Runtime.getRuntime().freeMemory();
                System.out.println("Heap free size GB: " + (((heapFreeSize / 1024) / 1024) / 1024));
                System.out.println();
            }
        }, 0, Integer.parseInt(args[1]) * 1000);

        var s = new StopWatch();
        s.start();
        Stream.of(
                new Configuration("(0 46 2)(1 47 45)(3 7 5)(4 8 6)(9 13 11)(10 14 12)(15 25 17)(16 44 18)(19 23 21)(20 24 22)(26 42 28)(27 31 29)(30 34 32)(33 37 35)(36 40 38)(39 43 41)")
        ).forEach(conf -> {
            sort(conf, "C:/Users/Luiz Silva/Temp", _16_12_SEQS);
        });
        s.stop();
        System.out.println(s.getTime());

        timer.cancel();
    }
}
