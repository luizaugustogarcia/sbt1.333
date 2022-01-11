package br.unb.cic.tdp.proof.util;

import java.util.Arrays;

public class Node {
    public int[] data;
    public Node next;
    public Node previous;
    public Node(int[] d) { data = d; }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}