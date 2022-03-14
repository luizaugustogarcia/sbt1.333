package br.unb.cic.tdp.unsafe;

import sun.misc.Unsafe;

public class UnsafeBooleanArray {
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

    public UnsafeBooleanArray(final int size) {
        this.size = size;
        this.address = unsafe.allocateMemory(size);
    }

    public static void set(long address, int i, final boolean value) {
        unsafe.putByte(address + i, (byte) (value ? 1 : 0));
    }

    public void set(final byte i, final boolean value) {
        unsafe.putByte(address + i, (byte) (value ? 1 : 0));
    }

    public static void fill(final long address, final long len, final boolean value) {
        unsafe.setMemory(address, len, (byte) (value ? 1 : 0));
    }

    public static boolean getBool(final long arrayAddress, final byte i) {
        return unsafe.getByte(arrayAddress + i) == 1;
    }

    public boolean getBool(final int i) {
        return unsafe.getByte(address + i) == 1;
    }

    public String toString() {
        final var str = new StringBuilder();
        str.append("[");

        for (int i = 0; i < size; i++) {
            str.append(i + "=");
            str.append(getBool(i) ? 1 : 0);
            if (i != size - 1)
                str.append(" ");
        }

        str.append("]");

        return str.toString();
    }

    public int len() {
        return size;
    }

    public long getAddress() {
        return this.address;
    }
}
