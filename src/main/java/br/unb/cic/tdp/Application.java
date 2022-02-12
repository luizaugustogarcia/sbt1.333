package br.unb.cic.tdp;

import com.google.common.cache.CacheBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static br.unb.cic.tdp.FinalPermutations.*;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        UNSUCCESSFUL_VISITED_CONFIGS = CacheBuilder.newBuilder()
                .maximumSize(Integer.parseInt(args[0]))
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Queue sbt1914() {
        return new Queue("sbt_19_14", true);
    }

    @Bean
    public TaskExecutor exec() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setMaxPoolSize(1);
        return exec;
    }
}
