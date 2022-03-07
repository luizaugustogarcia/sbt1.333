package br.unb.cic.tdp.unsafe;

public class UnsafeByteArray {
    private final static int BYTE = 1;
    private final byte size;
    private final long address;

    public UnsafeByteArray(byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size * BYTE);
    }

    public void set(byte i, byte value) {
        TheUnsafe.get().putByte(address + i * BYTE, value);
    }

    public byte get(byte i) {
        return TheUnsafe.get().getByte(address + i * BYTE);
    }

    public byte size() {
        return size;
    }

    public void free() {
        TheUnsafe.get().freeMemory(this.address);
    }
}
