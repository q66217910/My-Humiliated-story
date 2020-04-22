package com.zd.algorithm.letcode.dp;

import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 深度、广度优先算法
 */
public class Depth {

    public int numIslands(char[][] grid) {
        int n = grid.length;
        if (n == 0) {
            return 0;
        }
        int m = grid[0].length;
        Stack<String> stack = new Stack<>();
        int num = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (grid[i][j] == '1') {
                    stack.push(i + "_" + j);
                    grid[i][j] = 0;
                    num++;
                }
                while (!stack.isEmpty()) {
                    String[] value = stack.pop().split("_");
                    int a = Integer.parseInt(value[0]);
                    int b = Integer.parseInt(value[1]);
                    if (a > 0 && grid[a - 1][b] == '1') {
                        stack.push((a - 1) + "_" + b);
                        grid[a - 1][b] = 0;
                    }
                    if (b > 0 && grid[a][b - 1] == '1') {
                        stack.push(a + "_" + (b - 1));
                        grid[a][b - 1] = 0;
                    }
                    if (a < n - 1 && grid[a + 1][b] == '1') {
                        stack.push((a + 1) + "_" + b);
                        grid[a + 1][b] = 0;
                    }
                    if (b < m - 1 && grid[a][b + 1] == '1') {
                        stack.push(a + "_" + (b + 1));
                        grid[a][b + 1] = 0;
                    }
                }
            }
        }
        return num;
    }

    public List<List<Integer>> findSolution(BiFunction<Integer, Integer, Integer> customfunction, int z) {
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 1; i < z + 1; i++) {
            int left = 1, right = z;
            while (left < right) {
                int mid = (left + right) / 2;
                Integer value = customfunction.apply(i, mid);
                if (value < z) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
            if (customfunction.apply(i, left) == z) {
                List<Integer> list = new ArrayList<>();
                list.add(i);
                list.add(left);
                result.add(list);
            }

        }
        return result;
    }

    public int missingNumber(int[] nums) {
        Arrays.sort(nums);
        for (int i = 0; i < nums.length; i++) {
            if (i != nums[i]) {
                return i;
            }
        }
        return nums.length;
    }

    public int repeatedNTimes(int[] A) {
        for (int i = 0; i < A.length - 2; i++) {
            if (A[i] == A[i + 1] || A[i] == A[i + 2]) {
                return A[i];
            }
        }
        return A[A.length - 1];
    }

    public int islandPerimeter(int[][] grid) {
        int result = 0;
        int n = grid.length;
        int m = grid[0].length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (grid[i][j] == 1) {
                    result += 4;
                    if (i > 0 && grid[i - 1][j] == 1) {
                        result -= 1;
                    }
                    if (i < n - 1 && grid[i + 1][j] == 1) {
                        result -= 1;
                    }
                    if (j > 0 && grid[i][j - 1] == 1) {
                        result -= 1;
                    }
                    if (j < m - 1 && grid[i][j + 1] == 1) {
                        result -= 1;
                    }
                }
            }
        }
        return result;
    }

    public int majorityElement(int[] nums) {
        return Arrays.stream(nums)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > nums.length / 2)
                .findFirst()
                .map(Map.Entry::getKey)
                .get();
    }

    public int findRepeatNumber(int[] nums) {
        return Arrays.stream(nums)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .findFirst()
                .map(Map.Entry::getKey)
                .get();
    }

    public int majorityElement2(int[] nums) {
        return Arrays.stream(nums)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > nums.length / 2)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(-1);
    }

    public boolean uniqueOccurrences(int[] arr) {
        List<Long> values = new ArrayList<>(Arrays.stream(arr)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .values());
        return new HashSet<>(values).size() == values.size();
    }

    public int exchangeBits(int num) {
        return ((num & 0xaaaaaaaa) >> 1) | ((num & 0x55555555) << 1);
    }

    public static void main(String[] args) {
        new Depth().islandPerimeter(new int[][]{{0, 1, 0, 0},
                {1, 1, 1, 0},
                {0, 1, 0, 0},
                {1, 1, 0, 0}});
    }
}
