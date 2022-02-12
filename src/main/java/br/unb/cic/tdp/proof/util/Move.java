package br.unb.cic.tdp.proof.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Move {
    public Move parent;
    public final int mu;
    public Move[] children;
    private String pathToRoot;
    private String pathToRootUnsorted;
    private int numberOfZeroMovesUntilTop = -1;

    public Move(int mu, Move[] children, Move parent) {
        this.mu = mu;
        this.children = children;
        this.parent = parent;
        pathToRoot();
    }

    public int getHeight() {
        return maxDepth(this);
    }

    private int maxDepth(final Move move) {
        if (move.children.length == 0)
            return 1;

            int lDepth = maxDepth(move.children[0]);
        int rDepth = move.children.length == 1 ? 1 : maxDepth(move.children[1]);

        if (lDepth > rDepth)
            return (lDepth + 1);
        else
            return (rDepth + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move m = (Move) o;
        return mu == m.mu;
    }

    public String pathToRoot() {
        if (pathToRoot == null) {
            final var list = new ArrayList<String>();
            var current = this;
            while (current != null) {
                list.add(Integer.toString(current.mu));
                current = current.parent;
            }

            pathToRoot = list.stream().sorted().collect(Collectors.joining());
        }
        return pathToRoot;
    }

    public String pathToRootUnsorted() {
        if (pathToRootUnsorted == null) {
            final var list = new ArrayList<String>();
            var current = this;
            while (current != null) {
                list.add(Integer.toString(current.mu));
                current = current.parent;
            }

            pathToRootUnsorted = list.stream().collect(Collectors.joining());
        }
        return pathToRootUnsorted;
    }

    @Override
    public String toString() {
        return Integer.toString(mu);
    }

    public int numberOfZeroMovesUntilTop() {
        if (numberOfZeroMovesUntilTop == -1) {
            numberOfZeroMovesUntilTop = StringUtils.countMatches(pathToRoot(), "0");
        }
        return numberOfZeroMovesUntilTop;
    }
}