package br.unb.cic.tdp;

import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static br.unb.cic.tdp.util.Sorter.*;

@SpringBootApplication
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        final var cacheSize = (int) ((Runtime.getRuntime().maxMemory() * 0.85) / 429);
        logger.info("Cache size:" + cacheSize);
        UNSUCCESSFUL_VISITED_CONFIGS = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Queue sbt1914() {
        return new Queue("sbt_19_14", true);
    }

    @Bean
    public Queue sbt1914Found() {
        return new Queue("sbt_19_14_found", true);
    }
}
