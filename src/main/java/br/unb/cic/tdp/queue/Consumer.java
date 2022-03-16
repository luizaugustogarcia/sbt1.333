package br.unb.cic.tdp.queue;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.proof.ProofGenerator;
import br.unb.cic.tdp.proof.util.ListOfCycles;
import br.unb.cic.tdp.proof.util.Move;
import br.unb.cic.tdp.proof.util.Stack;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static br.unb.cic.tdp.util.ConfigurationSorter.search;
import static java.util.stream.Collectors.joining;

@Component
@ConditionalOnProperty(name = "consume")
public class Consumer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Map<String, Move> rootMap = new HashMap<>();

    public Consumer() {
        mapMoves(ProofGenerator._19_14_SEQS, rootMap);
    }

    private void mapMoves(final Move root, final Map<String, Move> rootMap) {
        rootMap.put(root.pathToRootUnsorted(), root);
        for (final var child: root.children) {
            mapMoves(child, rootMap);
        }
    }

    @SneakyThrows
    @Transactional
    @RabbitListener(queues = "sbt_19_14")
    public void receivedMessage(String message) {
        final var scanner = new Scanner(message);
        while (scanner.hasNextLine()) {
            final var line = scanner.nextLine();
            final var split = line.split(";");
            final var configuration = new Configuration(split[0]);
            final var pi = Arrays.stream(split[1].substring(1, split[1].length() - 1).split(",")).mapToInt(Integer::parseInt).toArray();

            var spiStr = split[2].substring(1, split[2].length() - 1);
            final var spi = new ListOfCycles(StringUtils.countMatches(split[2], "]"));
            spiStr = spiStr.substring(1, spiStr.length() - 1);
            for (final var cycle: spiStr.split("]\\[")) {
                spi.add(Arrays.stream(cycle.split(",")).mapToInt(Integer::parseInt).toArray());
            }

            var stackStr = split[3].substring(1, split[3].length() - 1);
            final var stack = new Stack(ProofGenerator._19_14_SEQS.getHeight());
            stackStr = stackStr.substring(1, stackStr.length() - 1);
            for (final var cycle: stackStr.split("]\\[")) {
                stack.push(Integer.parseInt(cycle.split(",")[0]),
                           Integer.parseInt(cycle.split(",")[1]),
                           Integer.parseInt(cycle.split(",")[2]));
            }

            final var parity = new boolean[configuration.getSpi().getMaxSymbol() + 1];
            final var spiIndex = new int[configuration.getSpi().getMaxSymbol() + 1][];

            for (int i = 0; i < spi.size; i++) {
                final var cycle = spi.elementData[i];
                for (int s : cycle) {
                    spiIndex[s] = cycle;
                    parity[s] = (cycle.length & 1) == 1;
                }
            }

            final var sorting = search(spi, parity, spiIndex, spiIndex.length, pi, stack, rootMap.get(split[4]));

            if (!sorting.isEmpty()) {
                System.out.println("Sorted: " + configuration.getSpi() + ", sorting: " + sorting.toList().stream().map(Arrays::toString).collect(joining(",")) + "\n");
                rabbitTemplate.convertAndSend("sbt_19_14_found", sorting.toList().stream().map(Arrays::toString).collect(joining(",")));
            }
        }
    }
}
