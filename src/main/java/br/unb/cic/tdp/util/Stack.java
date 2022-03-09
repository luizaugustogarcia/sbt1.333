package br.unb.cic.tdp.util;

import br.unb.cic.tdp.unsafe.UnsafeByteArray;
import br.unb.cic.tdp.unsafe.UnsafeListOfCycles;
import org.apache.commons.lang.NotImplementedException;

import static br.unb.cic.tdp.util.Sorter.create;
import static br.unb.cic.tdp.util.Sorter.set;

public class Stack {
    private int maxSize;
    private UnsafeByteArray content;
    private int size = 0;

    public Stack(int size) {
        this.maxSize = size;
        this.content = new UnsafeByteArray((byte) (size * 3));
    }

   public void push(final byte a, final byte b, final byte c) {
        this.content.set(size * 3, a);
        this.content.set((size * 3) + 1, b);
        this.content.set((size * 3) + 2, c);
        size++;
   }

    public void pop() {
        size--;
    }

    public UnsafeListOfCycles toListOfCycles() {
        final UnsafeListOfCycles list = new UnsafeListOfCycles(size);
        for (int i = 0; i < size; i++) {
            final var cycleAddress = create(3);
            set(cycleAddress, 0, content.getByte(i * 3));
            set(cycleAddress, 1, content.getByte((i * 3) + 1));
            set(cycleAddress, 2, content.getByte((i * 3) + 2));

            list.add(cycleAddress);
        }
        return list;
    }

    public int size() {
        return size;
    }

    public Stack clone() {
        final var clone = new Stack(this.size);
        clone.maxSize = maxSize;
        clone.content = content.cloneArray();
        clone.size = size;
        return clone;
    }

    public void free() {
        content.free();
    }
}
