package com.zd.algorithm.find;

import java.util.Arrays;

/**
 * 二分查找法
 *
 * 1.首先判断出返回的是left还是right。(前面left，后面right)
 * 2.判断比较符号  key（>/>=left    </<= right）   mid
 */
public class Dichotomy {

    public static void main(String[] args) {
        int[] arr = new int[]{1, 2, 2, 4, 5, 6, 7, 8};
        int[] a = new int[]{0, 0, 0, 0, 1, 1, 1, 1};
        System.out.println(basicSearch(arr, 2));
        System.out.println(a.length - basicSearch2(a, 1));
        System.out.println(basicSearch3(arr, 2));
        System.out.println(basicSearch4(a, 1));
        System.out.println(basicSearch5(a, 0));
    }

    /**
     * 基础二分法
     */
    private static int basicSearch(int[] arr, int key) {
        int left = 0, right = arr.length - 1;
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] == key) {
                return mid;
            } else if (arr[mid] < key) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }


    /**
     * 查找第一个与key相等的元素
     */
    private static int basicSearch2(int[] arr, int key) {
        int left = 0, right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] >= key) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
            if (left < arr.length && arr[left] == key) {
                return left;
            }
        }
        return -1;
    }

    /**
     * 查找最后一个与key相等的元素
     */
    private static int basicSearch3(int[] arr, int key) {
        int left = 0, right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] <= key) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
            if (right > 0 && arr[right] == key) {
                return right;
            }
        }
        return -1;
    }

    /**
     * 查找第一个等于或者大于key的元素
     */
    private static int basicSearch4(int[] arr, int key) {
        int left = 0, right = arr.length - 1;
        //最大值小于key，不存在等于或者大于key的元素
        if (arr[right] < key) {
            return -1;
        }
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] >= key) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }


    /**
     * 查找最后一个等于或者小于key的元素
     */
    private static int basicSearch5(int[] arr, int key) {
        int left = 0, right = arr.length - 1;
        //最小值大于key
        if (arr[left] > key) {
            return 0;
        }
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] <= key) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return right;
    }
    
}
