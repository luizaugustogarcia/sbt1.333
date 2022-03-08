package br.unb.cic.tdp.unsafe;

import org.apache.commons.lang.NotImplementedException;

public class UnsafeFloatArray {
    private final static byte FLOAT = 4;
    private final byte size;
    private final long address;

    public UnsafeFloatArray(byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size * FLOAT);
    }

    public static long at(long address, int l) {
        throw new NotImplementedException();
    }

    public static void set(long symbolIndexByOrientedCycle, byte at, float symbolIndex) {
        throw new NotImplementedException();
    }

    public static int len(long symbolIndexByOrientedCycle) {
        throw new NotImplementedException();
    }

    public void set(int i, long value) {
        TheUnsafe.get().putLong(address + ((long) i * FLOAT), value);
    }

    public long at(int i) {
        return TheUnsafe.get().getLong(address + ((long) i * FLOAT));
    }

    public byte size() {
        return size;
    }

    public void free() {
        TheUnsafe.get().freeMemory(this.address);
    }
}
