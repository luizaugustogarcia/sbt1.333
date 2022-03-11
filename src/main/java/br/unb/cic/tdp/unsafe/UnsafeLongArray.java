package br.unb.cic.tdp.unsafe;

public class UnsafeLongArray {
    private final static byte LONG = 8;
    private final byte size;
    private final long address;

    public UnsafeLongArray(final byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size * LONG);
    }

    public static long getLong(final long address, final int i) {
        return TheUnsafe.get().getLong(address + (i * LONG));
    }

    public static void setLong(final long address, final byte i, final long value) {
        TheUnsafe.get().putLong(address + (i * LONG), value);
    }

    public static String toString(final long address, final byte len) {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < len; i++) {
            str.append(i + "=" + getLong(address, i));
            if (i != len - 1)
                str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    @Override
    public String toString() {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < size; i++) {
            str.append(i + "=" + getLong(i));
            if (i != size - 1)
                str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public static void fill(final long address, final long len, final byte value) {
        TheUnsafe.get().setMemory(address, len * LONG, value);
    }

    public void setLong(int i, long value) {
        TheUnsafe.get().putLong(address + ((long) i * LONG), value);
    }

    public long getLong(int i) {
        return TheUnsafe.get().getLong(address + ((long) i * LONG));
    }

    public byte size() {
        return size;
    }

    public long getAddress() {
        return this.address;
    }
}
