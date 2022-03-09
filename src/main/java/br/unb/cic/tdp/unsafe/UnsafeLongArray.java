package br.unb.cic.tdp.unsafe;

import org.apache.commons.lang.NotImplementedException;

public class UnsafeLongArray {
    private final static byte LONG = 8;
    private final byte size;
    private final long address;

    public UnsafeLongArray(byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size * LONG);
    }

    public static long getLong(long address, int l) {
        // TODO
throw new NotImplementedException();
    }

    public static void setLong(long symbolIndexByOrientedCycle, byte at, long symbolIndex) {
        // TODO
throw new NotImplementedException();
    }

    public static int len(long symbolIndexByOrientedCycle) {
        // TODO
throw new NotImplementedException();
    }

    public static void fill(long orientedIndexMapping, int len, long value) {
        // TODO
throw new NotImplementedException();
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
}
