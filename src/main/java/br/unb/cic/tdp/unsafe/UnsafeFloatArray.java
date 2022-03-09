package br.unb.cic.tdp.unsafe;

import static br.unb.cic.tdp.util.Sorter.arraycopy;

public class UnsafeFloatArray {
    private final static byte FLOAT = 4;
    private final byte size;
    private final long address;

    public UnsafeFloatArray(byte size) {
        this.size = size;
        this.address = TheUnsafe.get().allocateMemory(size * FLOAT);
    }

    public static float getFloat(long address, int i) {
        return TheUnsafe.get().getFloat(address + (i * FLOAT));
    }

    public static void setFloat(final long address, final byte i, final float value) {
        TheUnsafe.get().putFloat(address + (i * FLOAT), value);
    }

    public static long clone(final long address, final byte len) {
        final var dest = TheUnsafe.get().allocateMemory(len * 4);
        arraycopy(address, 0, dest, 0, len * 4);
        return dest;
    }

    public static void reverse(final long address, final byte len) {
        for (byte i = 0; i < len / 2; i++) {
            float t = getFloat(address, i);
            setFloat(address, i, getFloat(address, len - i - 1));
            setFloat(address, (byte) (len - i - 1), t);
        }
    }

    public static int hashCode(final long address, final byte len) {
        int result = 1;
        for (int i = 0; i < len; i++) {
            final var element = getFloat(address, i);
            result = 31 * result + Float.floatToIntBits(element);
        }
        return result;
    }

    public void setFloat(final int i, final float value) {
        TheUnsafe.get().putFloat(address + ((long) i * FLOAT), value);
    }

    public float getFloat(final int i) {
        return TheUnsafe.get().getFloat(address + ((long) i * FLOAT));
    }

    public byte size() {
        return size;
    }

    public void free() {
        TheUnsafe.get().freeMemory(this.address);
    }
}
