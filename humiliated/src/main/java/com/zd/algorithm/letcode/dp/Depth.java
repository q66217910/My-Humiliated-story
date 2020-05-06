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


    public int openLock(String[] deadends, String target) {
        List<String> codes = Arrays.stream(deadends).collect(Collectors.toList());
        Queue<String> queue = new LinkedList<>();
        Set<String> set = new HashSet<>();
        int[] a = {1, -1};
        queue.add("0000");
        int i = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int k = 0; k < size; k++) {
                String value = queue.poll();
                if (!codes.contains(value)) {
                    if (value.equals(target)) {
                        return i;
                    }
                    //改变
                    for (int j = 0; j < target.length(); j++) {
                        for (int item : a) {
                            char[] chars = Arrays.copyOf(value.toCharArray(), value.length());
                            if (item == -1 && chars[j] == '0') {
                                chars[j] = '9';
                            } else if (item == 1 && chars[j] == '9') {
                                chars[j] = '0';
                            } else {
                                chars[j] += item;
                            }
                            String s = new String(chars);
                            if (!set.contains(s)) {
                                set.add(s);
                                queue.add(s);
                            }
                        }
                    }
                }
            }
            i++;
        }
        return -1;
    }

    public int numSquares(int n) {
        int[] dp = new int[n + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        int index = (int) Math.sqrt(n) + 1;
        int[] squares = new int[index];
        //所有可能平方的值
        for (int i = 1; i < index; i++) {
            squares[i] = i * i;
        }
        for (int i = 1; i <= n; ++i) {
            for (int j = 1; j < index; j++) {
                if (i < squares[j]) break;
                dp[i] = Math.min(dp[i], dp[i - squares[j]] + 1);
            }
        }
        return dp[n];
    }

    public int[] dailyTemperatures(int[] T) {
        int[] result = new int[T.length];
        Stack<Integer> stack = new Stack<>();
        for (int i = T.length - 1; i >= 0; i--) {
            //栈中压出比当前数小的
            while (!stack.isEmpty() && T[i] >= T[stack.peek()]) {
                stack.pop();
            }
            result[i] = stack.isEmpty() ? 0 : stack.peek() - i;
            stack.push(i);
        }
        return result;
    }

    class Node {
        public int val;
        public List<Node> neighbors;

        public Node() {
            val = 0;
            neighbors = new ArrayList<Node>();
        }

        public Node(int _val) {
            val = _val;
            neighbors = new ArrayList<Node>();
        }

        public Node(int _val, ArrayList<Node> _neighbors) {
            val = _val;
            neighbors = _neighbors;
        }
    }

    private HashMap<Node, Node> visited = new HashMap<>();

    public Node cloneGraph(Node node) {
        if (node == null) {
            return node;
        }
        if (visited.containsKey(node)) {
            return visited.get(node);
        }
        Node cloneNode = new Node(node.val, new ArrayList<>());
        visited.put(node, cloneNode);
        for (int i = 0; i < node.neighbors.size(); i++) {
            cloneNode.neighbors.add(cloneGraph(node.neighbors.get(i)));
        }
        return cloneNode;
    }

    int count = 0;

    public int findTargetSumWays(int[] nums, int S) {
        calculate(nums, 0, 0, S);
        return count;
    }

    public void calculate(int[] nums, int i, int sum, int S) {
        if (i == nums.length) {
            if (sum == S)
                count++;
        } else {
            calculate(nums, i + 1, sum + nums[i], S);
            calculate(nums, i + 1, sum - nums[i], S);
        }
    }

    public String decodeString(String s) {
        Stack<String> stack = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ']') {
                StringBuilder sb = new StringBuilder();
                int num = 0;
                //匹配到]从栈内压出
                while (!stack.empty()) {
                    String value = stack.pop();
                    if (value.charAt(0) == '[') {
                        //匹配到[到停止
                        int a = 1;
                        while (!stack.isEmpty() && Character.isDigit(stack.peek().charAt(0))) {
                            num = (stack.pop().charAt(0) - '0') * a + num;
                            a = a * 10;
                        }
                        num = Math.max(1, num);
                        StringBuilder r = new StringBuilder();
                        String key = sb.reverse().toString();
                        for (int j = 0; j < num; j++) {
                            r.insert(0, key);
                        }
                        stack.push(r.toString());
                        break;
                    } else {
                        if (value.length() > 1) {
                            value = new StringBuffer(value).reverse().toString();
                        }
                        sb.append(value);
                    }
                }
            } else {
                stack.push(new String(new char[]{c}));
            }
        }
        StringBuilder result = new StringBuilder();
        int size = stack.size();
        for (int i = 0; i < size; i++) {
            result.insert(0, stack.pop());
        }
        return result.toString();
    }

    public int[][] floodFill(int[][] image, int sr, int sc, int newColor) {
        int v = image[sr][sc];
        Set<Integer> set = new HashSet<>();
        Stack<Integer> stack = new Stack<>();
        stack.push(sr ^ (sc << 16));
        while (!stack.empty()) {
            Integer value = stack.pop();
            int i = value & 65535;
            int j = (value >> 16) & 65535;
            if (image[i][j] == v) {
                image[i][j] = newColor;
                if (i > 0 && image[i - 1][j] == v) {
                    if (!set.contains((i - 1) ^ (j << 16))) {
                        stack.push((i - 1) ^ (j << 16));
                        set.add((i - 1) ^ (j << 16));
                    }
                }
                if (j > 0 && image[i][j - 1] == v) {
                    if (!set.contains((i) ^ ((j - 1) << 16))) {
                        stack.push((i) ^ ((j - 1) << 16));
                        set.add((i) ^ ((j - 1) << 16));
                    }
                }
                if (i < image.length - 1 && image[i + 1][j] == v) {
                    if (!set.contains((i + 1) ^ ((j) << 16))) {
                        stack.push((i + 1) ^ ((j) << 16));
                        set.add((i + 1) ^ ((j) << 16));
                    }
                }
                if (j < image[0].length - 1 && image[i][j + 1] == v) {
                    if (!set.contains((i) ^ ((j + 1) << 16))) {
                        stack.push((i) ^ ((j + 1) << 16));
                        set.add((i) ^ ((j + 1) << 16));
                    }
                }
            }
        }
        return image;
    }

    public boolean canVisitAllRooms(List<List<Integer>> rooms) {
        Set<Integer> set = new HashSet<>();
        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        while (!stack.empty()) {
            Integer room = stack.pop();
            List<Integer> keys = rooms.get(room);
            set.add(room);
            if (set.size() == rooms.size()) {
                return true;
            }
            for (Integer key : keys) {
                if (!set.contains(key)) {
                    stack.push(key);
                }
            }
        }
        return false;
    }


    public static void main(String[] args) {
        System.out.println(new Depth().floodFill(new int[][]{{1, 1, 1}, {1, 1, 0}, {1, 0, 1}}, 1, 1, 2));
        System.out.println(new Depth().openLock(new String[]{"0201", "0101", "0102", "1212", "2002"}, "0202"));
    }
}
