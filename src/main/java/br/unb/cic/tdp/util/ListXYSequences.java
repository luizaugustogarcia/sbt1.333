package br.unb.cic.tdp.util;

import br.unb.cic.tdp.base.CommonOperations;
import org.paukov.combinatorics.Factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;


public class ListXYSequences {

    public static void main(String[] args) {
        final var sequences = new ArrayList<String>();

        for (int i = 1; i <= 25; i++) {
            final var seq = new ArrayList<>();
            while (i / (i - ((float) seq.size() + 1)) <= 1.33334) {
                seq.add(0);
            }
            while (seq.size() < i) {
                seq.add(2);
            }
            for (final var sequence : Factory.createPermutationGenerator(Factory.createVector(seq))) {
                final var _seq = sequence.getVector().stream().map(Object::toString).collect(Collectors.joining(","));
                if (!_seq.startsWith("2") && doesNotStartWith(_seq, sequences)) {
                    System.out.println("{" + _seq + "},");
                    sequences.add(_seq);
                }
            }
        }
    }

    private static boolean doesNotStartWith(final String seq, final ArrayList<String> sequences) {
        for (final String s: sequences) {
            if (seq.startsWith(s))
                return false;
        }
        return true;
    }
}
