package com.zd.algorithm.stack;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * 窗口最大值问题
 */
public class MaxWindows {

    public static int[] getMaxWindow(int[] arr, int w) {
        //没传入数组，或者窗口过小
        if (arr == null || w < 1 || arr.length < w) {
            return null;
        }
        LinkedList<Integer> qmax = new LinkedList<>();
        //初始化结果数组
        int[] res = new int[arr.length - w + 1];
        int index = 0;
        for (int i = 0; i < arr.length; i++) {
            while (!qmax.isEmpty() && arr[qmax.peekLast()] <= arr[i]) {
                qmax.pollLast();
            }
            qmax.addLast(i);
            if (qmax.peekFirst() == i - w) {
                qmax.pollFirst();
            }
            if (i >= w - 1) {
                res[index++] = arr[qmax.peekFirst()];
            }
        }
        return res;
    }

    public static void main(String[] args) {
        int[] res = {4,3,5,4,3,3,6,7};
        System.out.println(Arrays.toString(getMaxWindow(res, 3)));
    }
}
