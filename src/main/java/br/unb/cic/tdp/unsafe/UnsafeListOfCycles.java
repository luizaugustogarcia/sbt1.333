package br.unb.cic.tdp.unsafe;

import cern.colt.list.LongArrayList;
import lombok.SneakyThrows;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnsafeListOfCycles {
    public static final UnsafeListOfCycles EMPTY_LIST = new UnsafeListOfCycles(0);
    private int len;
    public UnsafeLongArray elementData;

    @SneakyThrows
    public UnsafeListOfCycles(final int maxSize) {
        this.elementData = new UnsafeLongArray(maxSize);
    }

    public static UnsafeListOfCycles singleton(long cycleAddress) {
        final var singleton = new UnsafeListOfCycles(1);
        singleton.add(cycleAddress);
        return singleton;
    }

    public static UnsafeListOfCycles asList(long cycleAddress1, long cycleAddress2, long cycleAddress3) {
        final var list = new UnsafeListOfCycles(3);
        list.add(cycleAddress1);
        list.add(cycleAddress2);
        list.add(cycleAddress3);
        return list;
    }

    public void add(long data) {
        add(data, elementData, len);
    }

    private void add(long e, long[] elementData, int s) {
        if (s == elementData.length)
            elementData = grow();
        elementData[s] = e;
        len = s + 1;
    }

    private long[] grow() {
        return grow(len + 1);
    }

    private long[] grow(int minCapacity) {
        try {
            return elementData = Arrays.copyOf(elementData, minCapacity);
        } finally {
            elementData.free();
        }
    }

    public void remove(long data) {
        int i = len - 1;
        for (; i >= 0; i--)
            if (data == elementData[i])
                break;
        fastRemove(elementData, i);
    }

    private void fastRemove(long[] es, int i) {
        final int newSize;
        if ((newSize = len - 1) > i)
            System.arraycopy(es, i + 1, es, i, newSize - i);
        es[len = newSize] = -1;
    }

    public boolean contains(final long cycleAddress) {
        for (byte i = 0; i < len; i++) {
            if (elementData.getLong(i) == cycleAddress)
                return true;
        }
        return false;
    }

    public void removeAll(final LongArrayList other) {
        other.trimToSize();;
        for (long cycle : other.elements()) {
            remove(cycle);
        }
    }

    @Override
    public String toString() {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < len; i++) {
            byte length = TheUnsafe.get().getByte(elementData[i]);
            for (int j = 0; j < length; j++) {
                str.append(TheUnsafe.get().getByte(elementData[i] + j + 1));
            }
            str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public void removeAll(final UnsafeListOfCycles other) {
        for (int i = 0; i < other.len; i++) {
            this.remove(other.elementData[i]);
        }
    }

    public void addAll(final UnsafeListOfCycles other) {
        for (int i = 0; i < other.len; i++) {
            this.add(other.elementData[i]);
        }
    }

    public boolean isEmpty() {
        return len == 0;
    }

    public List<int[]> toList() {
        final var list = new ArrayList<int[]>();

        for (int i = 0; i < len; i++) {
            byte length = TheUnsafe.get().getByte(elementData[i]);
            byte[] array = new byte[length];
            for (int j = 0; j < length; j++) {
                array[j] = TheUnsafe.get().getByte(elementData[i] + j + 1);
            }

            list.add(array);
        }

        return list;
    }

//    public UnsafeListOfCycles clone() {
//        final var clone = new UnsafeListOfCycles(len);
//
//        for (int i = 0; i < len; i++) {
//            clone.add(elementData[i]);
//        }
//
//        return clone;
//    }

    public int len() {
        return len;
    }

    public void clear() {
        len = 0;
    }

    public long at(int i) {
        // TODO
throw new NotImplementedException();
    }

    public long getAddress() {
        // TODO
throw new NotImplementedException();
    }

    public void free() {
        elementData.free();
    }

    public UnsafeListOfCycles clone() {
        // TODO
throw new NotImplementedException();
    }

    public void add(int[] cycleSymbols) {
        // TODO
throw new NotImplementedException();
    }
}