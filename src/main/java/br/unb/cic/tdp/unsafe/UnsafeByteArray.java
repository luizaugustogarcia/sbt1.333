package br.unb.cic.tdp.unsafe;

public class UnsafeByteArray {
    private final byte size;

    private final long address;

    public UnsafeByteArray(final byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size);
    }

    public static void set(long address, int i, final byte value) {
    }

    public void set(final byte i, final byte value) {
        TheUnsafe.get().putByte(address + i, value);
    }

    public static byte at(final long arrayAddress, final int i) {
        return TheUnsafe.get().getByte(arrayAddress + i);
    }

    public byte at(final int i) {
        return TheUnsafe.get().getByte(address + i);
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
