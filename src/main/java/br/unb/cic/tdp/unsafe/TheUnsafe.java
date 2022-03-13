package br.unb.cic.tdp.unsafe;

import sun.misc.Unsafe;

public class TheUnsafe {
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

    public static Unsafe get() {
        return unsafe;
    }
}
