package br.unb.cic.tdp;

import br.unb.cic.tdp.base.Configuration;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.StopWatch;
import org.apache.velocity.app.Velocity;

import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static br.unb.cic.tdp.FinalPermutations.*;
import static br.unb.cic.tdp.proof.ProofGenerator.*;

public class Teste1 {
    @SneakyThrows
    public static void main(String[] args) {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        UNSUCCESSFUL_CONFIGS = CacheBuilder.newBuilder()
                .maximumSize(Integer.parseInt(args[0]))
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

        numberOfMovesToCache = Integer.parseInt(args[1]);

        final var timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("Cache size: " + UNSUCCESSFUL_CONFIGS.size());
                System.out.println("Cache hits: " + hits);
                System.out.println("Cache misses: " + misses);
                System.out.println("Cache miss rate: " + String.format("%.2f", misses.get() / (double)hits.get()));
                long heapSize = Runtime.getRuntime().totalMemory();
                System.out.println("Heap size GB: " + (((heapSize / 1024) / 1024) / 1024));
                long heapFreeSize = Runtime.getRuntime().freeMemory();
                System.out.println("Heap free size GB: " + (((heapFreeSize / 1024) / 1024) / 1024));
                System.out.println();
            }
        }, 0, Integer.parseInt(args[2]) * 60 * 1000);


        var s = new StopWatch();
        s.start();
        Stream.of(
                new Configuration("(0 4 2)(1 5 3)(6 10 8)(7 11 9)(12 16 14)(13 17 15)(24 34 26)(25 35 27)(28 32 30)(29 33 31)(18 22 20)(19 23 21)")
        ).forEach(conf -> sort(conf, "C:/Users/Luiz Silva/Temp", _16_12_SEQS));
        s.stop();
        System.out.println(s.getTime());

        timer.cancel();
    }
}
