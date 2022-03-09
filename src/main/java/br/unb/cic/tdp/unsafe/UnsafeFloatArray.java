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

    public static long getFloat(long address, int l) {
        // TODO
throw new NotImplementedException();
    }

    public static void setFloat(long symbolIndexByOrientedCycle, byte at, float symbolIndex) {
        // TODO
throw new NotImplementedException();
    }

    public static int len(long symbolIndexByOrientedCycle) {
        // TODO
throw new NotImplementedException();
    }

    public static long clone(long signature) {
        // TODO
throw new NotImplementedException();
    }

    public static void reverse(long mirroredSignature) {
        // TODO
throw new NotImplementedException();
    }

    public static int hashCode(long mirroredSignature) {
        // TODO
throw new NotImplementedException();
    }

    public void setFloat(int i, long value) {
        TheUnsafe.get().putLong(address + ((long) i * FLOAT), value);
    }

    public long getFloat(int i) {
        return TheUnsafe.get().getLong(address + ((long) i * FLOAT));
    }

    public byte size() {
        return size;
    }

    public void free() {
        TheUnsafe.get().freeMemory(this.address);
    }
}
