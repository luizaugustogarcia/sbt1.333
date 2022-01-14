package br.unb.cic.tdp.proof.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListOfCycles {
    public static final ListOfCycles EMPTY_LIST = new ListOfCycles(0);
    public int size;
    public int[][] elementData;

    public ListOfCycles(final int initialSize) {
        this.elementData = new int[initialSize][];
    }

    public static ListOfCycles singleton(int[] data) {
        final var singleton = new ListOfCycles(1);
        singleton.add(data);
        return singleton;
    }

    public static ListOfCycles asList(int[] c1, int[] c2, int[] c3) {
        final var list = new ListOfCycles(3);
        list.add(c1);
        list.add(c2);
        list.add(c3);
        return list;
    }

    public void add(int[] data) {
        add(data, elementData, size);
    }

    private void add(int[] e, int[][] elementData, int s) {
        if (s == elementData.length)
            elementData = grow();
        elementData[s] = e;
        size = s + 1;
    }

    private int[][] grow() {
        return grow(size + 1);
    }

    private int[][] grow(int minCapacity) {
        return elementData = Arrays.copyOf(elementData, minCapacity);
    }

    public void remove(int[] data) {
        int i = size - 1;
        for (; i >= 0; i--)
            if (data == elementData[i])
                break;
        try {
            fastRemove(elementData, i);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void fastRemove(int[][] es, int i) {
        final int newSize;
        if ((newSize = size - 1) > i)
            System.arraycopy(es, i + 1, es, i, newSize - i);
        es[size = newSize] = null;
    }

    public boolean contains(final int[] data) {
        for (int i = 0; i < size; i++) {
            if (elementData[i] == data)
                return true;
        }
        return false;
    }

    public void removeAll(final List<int[]> other) {
        for (int[] cycle : other) {
            remove(cycle);
        }
    }

    @Override
    public String toString() {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < size; i++) {
            str.append(Arrays.toString(elementData[i]));
            str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public void removeAll(final ListOfCycles other) {
        for (int i = 0; i < other.size; i++) {
            this.remove(other.elementData[i]);
        }
    }

    public void addAll(final ListOfCycles other) {
        for (int i = 0; i < other.size; i++) {
            this.add(other.elementData[i]);
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public List<int[]> toList() {
        final var list = new ArrayList<int[]>();

        for (int i = 0; i < size; i++) {
            list.add(elementData[i]);
        }

        return list;
    }

    public ListOfCycles clone() {
        final var clone = new ListOfCycles(size);

        for (int i = 0; i < size; i++) {
            clone.add(elementData[i].clone());
        }

        return clone;
    }

    public void clear() {
        size = 0;
    }
}