package br.unb.cic.tdp.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class TheUnsafe {
    private static Unsafe unsafe;

    {
        final Field f;
        try {
            f = Unsafe.class.getDeclaredField("theUnsafe");
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
