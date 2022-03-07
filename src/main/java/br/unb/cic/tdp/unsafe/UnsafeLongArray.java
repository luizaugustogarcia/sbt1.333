package br.unb.cic.tdp.unsafe;

public class UnsafeLongArray {
    private final static byte LONG = 8;
    private final byte size;
    private final long address;

    public UnsafeLongArray(byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size * LONG);
    }

    public void set(int i, long value) {
        TheUnsafe.get().putLong(address + ((long) i * LONG), value);
    }

    public long at(int i) {
        return TheUnsafe.get().getLong(address + ((long) i * LONG));
    }

    public byte size() {
        return size;
    }

    public void free() {
        TheUnsafe.get().freeMemory(this.address);
    }
}
