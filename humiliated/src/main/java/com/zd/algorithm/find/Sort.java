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
     * 插入排序  时间复杂度（O（n^2））
     */
    private int[] insSort(int[] nums) {
        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[i] < nums[j]) {
                    int temp = nums[i];
                    System.arraycopy(nums, j, nums, j + 1, i - j);
                    nums[j] = temp;
                }
            }
        }
        return nums;
    }

    /**
     * 快速排序
     */
    private int[] quickSort(int[] nums) {
        return quickSort(nums, 0, nums.length - 1);
    }

    private int[] quickSort(int[] nums, int left, int right) {
        if (left >= right) {
            return nums;
        }
        // base中存放基准数
        int base = nums[left];
        int i = left, j = right;
        while (i != j) {
            // 顺序很重要，先从右边开始往左找，直到找到比base值小的数
            while (nums[j] >= base && i < j) {
                j--;
            }
            // 再从左往右边找，直到找到比base值大的数
            while (nums[i] <= base && i < j) {
                i++;
            }
            if (i < j) {
                nums[i] ^= nums[j];
                nums[j] ^= nums[i];
                nums[i] ^= nums[j];
            }
        }

        nums[left] = nums[i];
        nums[i] = base;

        quickSort(nums, left, i - 1);
        quickSort(nums, i + 1, right);
        return nums;
    }

    /**
     * 归并排序
     */
    private int[] mergeSort(int[] nums) {
        return mergeSort(nums, 0, nums.length - 1);
    }

    private int[] mergeSort(int[] nums, int left, int right) {
        if (left >= right) {
            return nums;
        }
        int mid = (left + right) / 2;
        mergeSort(nums, left, mid);
        mergeSort(nums, mid + 1, right);
        return mergeSort(nums, left, mid, right);
    }

    private int[] mergeSort(int[] nums, int left, int mid, int right) {
        int[] tmp = new int[nums.length];
        int r = mid + 1;
        int index = left;
        int cIndex = left;
        // 逐个归并
        while (left <= mid && r <= right) {
            if (nums[left] <= nums[r]) {
                tmp[index++] = nums[left++];
            } else {
                tmp[index++] = nums[r++];
            }
        }
        // 将左边剩余的归并
        while (left <= mid) {
            tmp[index++] = nums[left++];
        }
        // 将右边剩余的归并
        while (r <= right) {
            tmp[index++] = nums[r++];
        }
        //从临时数组拷贝到原数组
        System.arraycopy(tmp, cIndex, nums, cIndex, right - cIndex + 1);
        return nums;
    }


    public static void main(String[] args) {
        System.out.println(new Sort().quickSort(new int[]{5, 4, 3, 2, 1}));
    }
}
