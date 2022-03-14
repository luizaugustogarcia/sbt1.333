package br.unb.cic.tdp.unsafe;

import sun.misc.Unsafe;

import static br.unb.cic.tdp.util.Sorter.arraycopy;

public class UnsafeFloatArray {
    private final static byte FLOAT = 4;
    private final byte size;
    private final long address;

    private static Unsafe unsafe;

    static {
        try {
            final var f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UnsafeFloatArray(byte size) {
        this.size = size;
        this.address = unsafe.allocateMemory(size * FLOAT);
    }

    public static float getFloat(long address, int i) {
        return unsafe.getFloat(address + (i * FLOAT));
    }

    public static void setFloat(final long address, final byte i, final float value) {
        unsafe.putFloat(address + (i * FLOAT), value);
    }

    public static long clone(final long address, final byte len) {
        final var dest = unsafe.allocateMemory(len * 4);
        arraycopy(address, 0, dest, 0, len * FLOAT);
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

    public static String toString(final long address, final byte size) {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < size; i++) {
            str.append(getFloat(address, i));
            if (i != size - 1)
                str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public static void fill(final long address, final long len, final byte value) {
        unsafe.setMemory(address, len * FLOAT, value);
    }

    public void setFloat(final int i, final float value) {
        unsafe.putFloat(address + ((long) i * FLOAT), value);
    }

    public float getFloat(final int i) {
        return unsafe.getFloat(address + ((long) i * FLOAT));
    }

    public byte size() {
        return size;
    }
}
