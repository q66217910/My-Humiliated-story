package com.zd.algorithm.find;

public class Bit {

    /**
     * 交换两个值
     */
    private void swap(int a, int b) {
        a ^= b;
        b ^= a;
        a ^= b;
    }

    /**
     * 去掉最后一位
     */
    private static int shr1(int x) {
        return (x >> 1) << 1;
    }

    /**
     * 最后一位置位1
     */
    private static int last1(int x) {
        return x |= 1;
    }

    /**
     * 补数
     */
    public int findComplement(int num) {
        long i = 1;
        while (i <= num)
            i = i << 1;
        i -= 1;
        return (int) i ^ num;
    }



    public static void main(String[] args) {
        System.out.println(last1(2));
    }
}
