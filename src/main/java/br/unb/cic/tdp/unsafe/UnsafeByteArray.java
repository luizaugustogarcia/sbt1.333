package br.unb.cic.tdp.unsafe;

public class UnsafeByteArray {
    private final byte size;

    private final long address;

    public UnsafeByteArray(final byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size);
    }

    public void set(final byte i, final byte value) {
        TheUnsafe.get().putByte(address + i, value);
    }

    public static byte at(final long arrayAddress, final byte i) {
        return TheUnsafe.get().getByte(arrayAddress + i);
    }

    public byte at(final byte i) {
        return TheUnsafe.get().getByte(address + i);
    }

    public byte size() {
        return size;
    }

    public long getAddress() {
        return this.address;
    }

    public void free() {
        TheUnsafe.get().freeMemory(this.address);
    }
}
