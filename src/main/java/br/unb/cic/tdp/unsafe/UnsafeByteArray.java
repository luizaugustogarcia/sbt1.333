package br.unb.cic.tdp.unsafe;

import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.NotInvertibleException;

public class UnsafeByteArray {
    private final byte size;

    private final long address;

    public UnsafeByteArray(final byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size);
    }

    public UnsafeByteArray(final int[] symbols) {
        throw new NotInvertibleException();
    }

    public static void setByte(long address, int i, final byte value) {
        throw new NotInvertibleException();
    }

    public void setByte(final byte i, final byte value) {
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
