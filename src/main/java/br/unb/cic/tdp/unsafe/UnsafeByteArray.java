package br.unb.cic.tdp.unsafe;

import static br.unb.cic.tdp.util.Sorter.arraycopy;

public class UnsafeByteArray {
    private final int size;
    private final long address;

    public UnsafeByteArray(final int size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size);
    }

    public UnsafeByteArray(final int[] symbols) {
        this((byte) symbols.length);
        for (byte i = 0; i < symbols.length; i++) {
            TheUnsafe.get().putByte(this.address + i, (byte) symbols[i]);
        }
    }

    public static void setByte(long address, int i, final byte value) {
        TheUnsafe.get().putByte(address + i, value);
    }

    public static byte getByte(final long arrayAddress, final int i) {
        return TheUnsafe.get().getByte(arrayAddress + i);
    }

    public static void fill(final long address, final long len, final byte value) {
        TheUnsafe.get().setMemory(address, len, value);
    }

    public byte getByte(final int i) {
        return getByte(address, i);
    }

    public int len() {
        return size;
    }

    public long getAddress() {
        return this.address;
    }

    public void set(int i, byte a) {
        TheUnsafe.get().putByte(this.address + i, a);
    }

    public UnsafeByteArray cloneArray() {
        final var dest = new UnsafeByteArray(size);
        arraycopy(address, 0, dest.address, 0, size);
        return dest;
    }

    public static String toString(final long address, final int size) {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < size; i++) {
            str.append(getByte(address, i));
            if (i != size - 1)
                str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public String toString() {
        return toString(this.address, this.size);
    }

    public UnsafeByteArray removeElement(byte element) {
        final var newArray = new UnsafeByteArray((byte) (size - 1));
        int i = 0;
        for (; i < size; i++) {
            if (getByte(i) == element) {
                break;
            }
        }

        arraycopy(this.address, 0, newArray.address, 0, i);
        if (i < size - 1)
            arraycopy(this.address, i + 1, newArray.address, i, size - i - 1);

        return newArray;
    }
}
