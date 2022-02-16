package br.unb.cic.tdp.queue;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class Loader implements CommandLineRunner {

    @Value("${loading:#{false}}")
    private boolean loading;

    @Value("${file:#{null}}")
    private String file;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void run(String... args) throws Exception {
      if (loading) {
        final var reader = new BufferedReader(new FileReader(file), 100 * 1024 * 1024);

        final var list = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null) {
          list.add(line);
          if (list.size() % 10 == 0) {
            String message = list.stream().collect(Collectors.joining("\n"));
            rabbitTemplate.convertAndSend("sbt_19_14", message);
            list.clear();
          }
        }
        if (!list.isEmpty()) {
          String message = list.stream().collect(Collectors.joining("\n"));
          rabbitTemplate.convertAndSend("sbt_19_14", message);
        }
      }
    }
}