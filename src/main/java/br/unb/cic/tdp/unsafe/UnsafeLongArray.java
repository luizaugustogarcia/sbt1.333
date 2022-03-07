package br.unb.cic.tdp.unsafe;

import org.apache.commons.lang.NotImplementedException;

public class UnsafeLongArray {
    private final static int LONG = 8;
    private final byte size;
    private final long address;

    public UnsafeLongArray(byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size * LONG);
    }

    public void set(byte i, long value) {
        TheUnsafe.get().putLong(address + i * LONG, value);
    }

    public byte get(byte i) {
        return TheUnsafe.get().getByte(address + i * LONG);
    }

    public byte size() {
        return size;
    }

    public void free() {
        TheUnsafe.get().freeMemory(this.address);
    }
}
