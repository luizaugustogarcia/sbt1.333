package br.unb.cic.tdp.unsafe;

public class UnsafeBooleanArray {
    private final byte size;

    private final long address;

    public UnsafeBooleanArray(final byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size);
    }

    public static void set(long address, int i, final boolean value) {
        TheUnsafe.get().putByte(address + i, (byte) (value ? 1 : 0));
    }

    public void set(final byte i, final boolean value) {
        TheUnsafe.get().putByte(address + i, (byte) (value ? 1 : 0));
    }

    public static void fill(final long address, final long len, final boolean value) {
        TheUnsafe.get().setMemory(address, len, (byte) (value ? 1 : 0));
    }

    public static boolean getBool(final long arrayAddress, final byte i) {
        return TheUnsafe.get().getByte(arrayAddress + i) == 1;
    }

    public boolean getBool(final int i) {
        return TheUnsafe.get().getByte(address + i) == 1;
    }

    public String toString() {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < size; i++) {
            str.append(getBool(i) ? 1 : 0);
            if (i != size - 1)
                str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public byte len() {
        return size;
    }

    public long getAddress() {
        return this.address;
    }
}
