package br.unb.cic.tdp.unsafe;

public class UnsafeByteArray {
    private final byte size;
    private final long address;

    public UnsafeByteArray(final byte size) {
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

    public byte getByte(final int i) {
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
