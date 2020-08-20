package com.zd.algorithm.find;

import java.util.Arrays;

/**
 * 二分查找法
 * <p>
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


    /**
     * 找出第 k 小的距离对
     */
    public int smallestDistancePair(int[] nums, int k) {
        Arrays.sort(nums);
        int l = 0, r = nums[nums.length - 1] - nums[0];
        while (l < r) {
            //中间差值
            int m = (l + r) >> 1;
            int count = 0, left = 0;
            //计算右节点右节点比左节点大的m数量
            for (int right = 0; right < nums.length; right++) {
                while (nums[right] - nums[left] > m) left++;
                count += right - left;
            }
            if (count >= k) r = m;
            else l = m + 1;
        }
        return l;
    }

    /**
     *  构建大顶堆
     */
    private static void buildMaxHeap(int[] array, int length) {
        //从最后一个非叶子节点开始
        for (int i = length / 2 - 1; i >= 0; i--) {
            adjustHeap(array, i, length);
        }
    }

    private static void adjustHeap(int[] array, int i, int length) {
        //获取当前非叶子节点的值
        int temp = array[i];
        //依次遍历非叶子节点的左子节点
        for (int j = 2 * i + 1; j < length; j = 2 * j + 1) {
            //让j指向左右子节点较大的哪个
            if (j + 1 < length && array[j] < array[j + 1]) {
                j++;
            }
            //如果子节点>父节点
            if (array[j] > temp) {
                //让当前非叶子节点的值等于子节点的值
                array[i] = array[j];
                //让当前非叶子节点的下标指向当前字节点的下标
                i = j;
            } else {
                //因为大顶堆是从下到上构建的，所以如果父节点是最大的那个的话就可以直接退出循环
                break;
            }
            //让大的子节点等于之前非叶子节点的值
            array[j] = temp;
        }
    }

    /**
     *  堆排序
     */
    public static int[] heapSort(int[] array, int n) {
        int size = n;
        //第一次构建大顶堆
        int length = array.length;
        buildMaxHeap(array, length);
        //此时顶端是数组中最大的节点，将顶端与数组末尾交换，然后在剩下的数组中再次构建大顶堆
        while (n > 0 && n <= length) {
            // 交换首尾元素
            int temp = array[0];
            array[0] = array[length - 1];
            array[length - 1] = temp;
            n--;
            length--;
            buildMaxHeap(array, length);
        }
        int[] result = new int[size];
        System.arraycopy(array, array.length - size, result, 0, size);
        return result;
    }
}
