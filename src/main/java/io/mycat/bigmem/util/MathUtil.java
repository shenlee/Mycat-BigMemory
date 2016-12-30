package io.mycat.bigmem.util;

public class MathUtil {
    public static int log2p(int x) {
        int r = 0;
        while ((x >>= 1) != 0)
            r++;
        return r;
    }
}
