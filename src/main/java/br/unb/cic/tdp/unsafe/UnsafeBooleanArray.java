package br.unb.cic.tdp.unsafe;

import org.apache.commons.lang3.NotImplementedException;

public class UnsafeBooleanArray {
    private final byte size;

    private final long address;

    public UnsafeBooleanArray(final byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size);
    }

    public static void set(long address, int i, final boolean value) {
        throw new NotImplementedException();
    }

    public void set(final byte i, final boolean value) {
        TheUnsafe.get().putByte(address + i, (byte) (value ? 1: 0));
    }

    public static boolean at(final long arrayAddress, final byte i) {
        return TheUnsafe.get().getByte(arrayAddress + i) == 1;
    }

    public boolean at(final int i) {
        return TheUnsafe.get().getByte(address + i) == 1;
    }

    public byte len() {
        return size;
    }

    public long getAddress() {
        return this.address;
    }

    public void free() {
        TheUnsafe.get().freeMemory(this.address);
    }
}
