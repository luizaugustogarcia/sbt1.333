package br.unb.cic.tdp.unsafe;

import br.unb.cic.tdp.util.Sorter;
import cern.colt.list.LongArrayList;

import java.util.ArrayList;
import java.util.List;

import static br.unb.cic.tdp.util.Sorter.*;

public class UnsafeListOfCycles {
    public static final UnsafeListOfCycles EMPTY_LIST = new UnsafeListOfCycles(1);
    private final int maxSize;
    private int size;
    public UnsafeLongArray elementData;

    public UnsafeListOfCycles(final int maxSize) {
        this.elementData = new UnsafeLongArray((byte) maxSize);
        this.maxSize = maxSize;
    }

    public static UnsafeListOfCycles singleton(final long cycleAddress) {
        final var singleton = new UnsafeListOfCycles(1);
        singleton.add(cycleAddress);
        return singleton;
    }

    public static UnsafeListOfCycles asList(final long cycleAddress1, final long cycleAddress2, final long cycleAddress3) {
        final var list = new UnsafeListOfCycles(3);
        list.add(cycleAddress1);
        list.add(cycleAddress2);
        list.add(cycleAddress3);
        return list;
    }

    public void add(final long data) {
        add(data, elementData, size);
    }

    private void add(final long e, UnsafeLongArray elementData, final int s) {
        if (s == elementData.size())
            elementData = grow();
        elementData.setLong(s, e);
        size = s + 1;
    }

    private UnsafeLongArray grow() {
        return grow(size + 1);
    }

    private UnsafeLongArray grow(final int minCapacity) {
        final var copy = new UnsafeLongArray((byte) minCapacity);
        arraycopy(elementData.getAddress(), 0, copy.getAddress(), 0, elementData.size());
        free(elementData.getAddress());
        return elementData = copy;
    }

    public void remove(final long data) {
        int i = size - 1;
        for (; i >= 0; i--)
            if (data == elementData.getLong(i))
                break;
        fastRemove(elementData, i);
    }

    private void fastRemove(final UnsafeLongArray es, final int i) {
        final int newSize;
        if ((newSize = size - 1) > i) {
            for (int j = i + 1; j < this.size; j++) {
                es.setLong(j - 1, es.getLong(j));
            }
        }
        es.setLong(size = newSize, 0);
    }

    public boolean contains(final long cycleAddress) {
        for (byte i = 0; i < size; i++) {
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

        for (int i = 0; i < size; i++) {
            str.append(Sorter.toString(elementData.getLong(i)));
            if (i != size - 1)
                str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public void removeAll(final UnsafeListOfCycles other) {
        for (int i = 0; i < other.size; i++) {
            this.remove(other.elementData.getLong(i));
        }
    }

    public void addAll(final UnsafeListOfCycles other) {
        for (int i = 0; i < other.size; i++) {
            this.add(other.elementData.getLong(i));
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public List<int[]> toList() {
        final var list = new ArrayList<int[]>();

        for (int i = 0; i < size; i++) {
            byte length = Sorter.cycleLen(elementData.getLong(i));
            int[] array = new int[length];
            for (int j = 0; j < length; j++) {
                array[j] = Sorter.cycleAt(elementData.getLong(i), j + 1);
            }

            list.add(array);
        }

        return list;
    }

    public int len() {
        return size;
    }

    public void clear() {
        size = 0;
    }

    public long at(int i) {
        return elementData.getLong(i);
    }

    public UnsafeListOfCycles clone() {
        final var clone = new UnsafeListOfCycles(maxSize);

        for (int i = 0; i < size; i++) {
            final var cycleAddress = elementData.getLong(i);
            final var len = cycleLen(cycleAddress);
            final var cycleClone = create(len);
            arraycopy(cycleAddress, 0, cycleClone, 0, len + 1);
            clone.add(cycleAddress);
        }

        return clone;
    }

    public void add(int[] cycleSymbols) {
        final var cycleAddress = create(cycleSymbols.length);
        for (int i = 0; i < cycleSymbols.length; i++) {
            cycleSet(cycleAddress, i, (byte) cycleSymbols[i]);
        }
        add(cycleAddress);
    }

    public long getElementDataAddress() {
        return elementData.getAddress();
    }

    public byte numberOfSymbols() {
        var n = (byte) 0;
        for (int i = 0; i < size; i++) {
            n += cycleLen(this.elementData.getLong(i));
        }
        return n;
    }
}