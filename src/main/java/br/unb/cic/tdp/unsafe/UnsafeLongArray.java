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

    public static void fill(final long address, final int len, final long value) {
        for (byte i = 0; i < len; i++) {
            setLong(address, i, value);
        }
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

    public void free() {
        TheUnsafe.get().freeMemory(this.address);
    }

    public long getAddress() {
        return this.address;
    }
}
