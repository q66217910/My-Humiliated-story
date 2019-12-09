package com.zd.algorithm.find;

/**
 * 二分查找法
 */
public class Dichotomy {

    public static void main(String[] args) {
        int[] arr = new int[]{1, 2, 2, 4, 5, 6, 7, 8};
        int[] a = new int[]{0, 0, 0, 0, 1, 1, 1, 1};
        System.out.println(basicSearch(arr, 2));
        System.out.println(a.length-basicSearch2(a, 1));
        System.out.println(basicSearch3(arr, 2));
    }

    /**
     * 基础二分法
     */
    private static int basicSearch(int[] arr, int key) {
        int left = 0, right = arr.length - 1;
        while (left < right) {
            int mid = left + (left + right) / 2;
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
            int mid = left + (left + right) / 2;
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
            int mid = left + (left + right) / 2;
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

}
