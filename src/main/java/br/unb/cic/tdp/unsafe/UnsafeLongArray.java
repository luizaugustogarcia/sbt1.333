package br.unb.cic.tdp.unsafe;

import br.unb.cic.tdp.util.Sorter;
import sun.misc.Unsafe;

public class UnsafeLongArray {
    private final static byte LONG = 8;
    private final int size;
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
    
    public UnsafeLongArray(final int size) {
        this.size = size;
        this.address = unsafe.allocateMemory(size * LONG);
    }

    public static long getLong(final long address, final int i) {
        return unsafe.getLong(address + (i * LONG));
    }

    public static void setLong(final long address, final byte i, final long value) {
        unsafe.putLong(address + (i * LONG), value);
    }

    public static String toString(final long address, final byte len) {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < len; i++) {
            str.append(i + "=" + getLong(address, i));
            if (i != len - 1)
                str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public static void copy(long src, int srcPos, long dest, int destPos, long size) {
        Sorter.arraycopy(src, srcPos, dest, destPos, (int) (size * LONG));
    }

    @Override
    public String toString() {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < size; i++) {
            str.append(i + "=" + getLong(i));
            if (i != size - 1)
                str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public static void fill(final long address, final long len, final byte value) {
        unsafe.setMemory(address, len * LONG, value);
    }

    public void setLong(int i, long value) {
        unsafe.putLong(address + ((long) i * LONG), value);
    }

    public long getLong(int i) {
        return unsafe.getLong(address + ((long) i * LONG));
    }

    public int size() {
        return size;
    }

    public long getAddress() {
        return this.address;
    }
}
