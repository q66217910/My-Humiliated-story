package com.zd.algorithm.find;

public class Sort {

    /**
     * 冒泡排序 时间复杂度（O（n^2））
     */
    private int[] bubbleSort(int[] nums) {
        int index = 0;
        do {
            for (int i = 1; i < nums.length - index; i++) {
                if (nums[i - 1] > nums[i]) {
                    //交换位置
                    nums[i - 1] ^= nums[i];
                    nums[i] ^= nums[i - 1];
                    nums[i - 1] ^= nums[i];
                }
            }
            index++;
        } while (nums.length != index);
        return nums;
    }

    /**
     * 选择排序 时间复杂度（O（n^2））
     */
    private int[] selSort(int[] nums) {
        for (int i = 0; i < nums.length; i++) {
            int min = Integer.MAX_VALUE;
            int index = 0;
            for (int j = i; j < nums.length; j++) {
                if (nums[j] < min) {
                    min = nums[j];
                    index = j;
                }
            }
            if (i != index) {
                nums[i] ^= nums[index];
                nums[index] ^= nums[i];
                nums[i] ^= nums[index];
            }
        }
        return nums;
    }

    /**
     * 插入排序
     */
    private int[] insSort(int[] nums) {
        for (int i = 0; i < nums.length; i++) {

        }
        return nums;
    }

    public static void main(String[] args) {
        System.out.println(new Sort().selSort(new int[]{5, 4, 3, 2, 1}));
    }
}
