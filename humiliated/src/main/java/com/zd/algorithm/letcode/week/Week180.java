package com.zd.algorithm.letcode.week;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Week180 {

    public List<Integer> luckyNumbers(int[][] matrix) {
        Map<Integer, Integer> mimMap = new HashMap<>();
        Map<Integer, Integer> maxMap = new HashMap<>();
        for (int i = 0; i < matrix.length; i++) {
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < matrix[i].length; j++) {
                //寻找行中最小元素
                min = Math.min(min, matrix[i][j]);
                Integer value = maxMap.get(j);
                if (value == null) {
                    value = -1;
                }
                value = Math.max(value, matrix[i][j]);
                maxMap.put(j, value);
            }
            mimMap.put(i, min);
        }
        return mimMap.values().stream().filter(a -> maxMap.values().contains(a)).collect(Collectors.toList());
    }

    public static List<List<Integer>> combinations(List<Integer> list, int k) {
        if (k == 0 || list.isEmpty()) {//去除K大于list.size的情况。即取出长度不足K时清除此list
            return Collections.emptyList();
        }
        if (k == 1) {//递归调用最后分成的都是1个1个的，从这里面取出元素
            return list.stream().map(e -> Stream.of(e).collect(Collectors.toList())).collect(Collectors.toList());
        }
        Map<Boolean, List<java.lang.Integer>> headAndTail = split(list, 1);
        List<java.lang.Integer> head = headAndTail.get(true);
        List<java.lang.Integer> tail = headAndTail.get(false);
        List<List<java.lang.Integer>> c1 = combinations(tail, (k - 1)).stream().map(e -> {
            List<java.lang.Integer> l = new ArrayList<>();
            l.addAll(head);
            l.addAll(e);
            return l;
        }).collect(Collectors.toList());
        List<List<java.lang.Integer>> c2 = combinations(tail, k);
        c1.addAll(c2);
        return c1;
    }

    public static Map<Boolean, List<Integer>> split(List<Integer> list, int n) {
        return IntStream
                .range(0, list.size())
                .mapToObj(i -> new AbstractMap.SimpleEntry<>(i, list.get(i)))
                .collect(Collectors.partitioningBy(entry -> entry.getKey() < n, Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toList())));
    }

    public int maxPerformance(int n, int[] speed, int[] efficiency, int k) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        int result = 0;
        for (int i = 1; i <= k; i++) {
            List<List<Integer>> combinations = combinations(list, i);
            for (List<Integer> combination : combinations) {
                int s = 0;
                int e = Integer.MAX_VALUE;
                for (Integer value : combination) {
                    s += speed[value];
                    e = Math.min(efficiency[value], e);
                }
                result = Math.max(s * e, result);
            }
        }
        return result;
    }

    /**
     *
     */
    public int countLargestGroup(int n) {
        Map<Integer, Long> collect = IntStream.range(1, n + 1)
                .boxed()
                .map(value -> {
                    int num = 0;
                    while (value > 0) {
                        num += value % 10;
                        value = value / 10;
                    }
                    return num;
                })
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Long max = collect.values().stream().max(Comparator.comparing(Function.identity())).get();
        return (int) collect.values().stream().filter(a -> a.equals(max)).count();
    }

    public boolean canConstruct(String s, int k) {
        int m = s.length();
        if (k > m) {
            return false;
        }
        //查看是否这么多数量重复数字
        int num = m - k;
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < m; i++) {
            map.put(s.charAt(i), map.getOrDefault(s.charAt(i), 0) + 1);
        }
        for (Integer value : map.values()) {
            if (value > 1) {
                num -= (value / 2) * 2;
            }
        }
        return num <= 0;
    }

    /**
     * 圆和正方形是否相交
     */
    public boolean checkOverlap(int radius, int x_center, int y_center, int x1, int y1, int x2, int y2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                if ((y - y_center) * (y - y_center) + (x - x_center) * (x - x_center) <= radius * radius) {
                    return true;
                }
            }
        }
        return false;
    }

    public int maxSatisfaction(int[] satisfaction) {
        Arrays.sort(satisfaction);
        int result = 0;
        int sum = 0;
        int max = 0;
        for (int i = satisfaction.length - 1; i >= 0; i--) {
            result += sum;
            sum += satisfaction[i];
            result += satisfaction[i];
            if (result > max) {
                max = result;
            } else {
                break;
            }
        }
        return result;
    }

    public List<Integer> minSubsequence(int[] nums) {
        Arrays.sort(nums);
        int sum = Arrays.stream(nums).sum();
        int max = 0;
        List<Integer> list = new ArrayList<>();
        for (int i = nums.length - 1; i >= 0; i--) {
            max += nums[i];
            list.add(nums[i]);
            if (max > sum - max) {
                break;
            }
        }
        return list;
    }

    public int numSteps(String s) {
        int num = 0;
        while (!s.equals("1")) {
            num++;
            char last = s.charAt(s.length() - 1);
            if (last == '1') {
                //奇数
                StringBuffer sb = new StringBuffer();
                sb.append('0');
                boolean carry = true;
                for (int i = s.length() - 2; i >= 0; i--) {
                    if (carry) {
                        if (s.charAt(i) == '1') {
                            sb.append('0');
                        } else {
                            sb.append('1');
                            carry = false;
                        }
                    } else {
                        sb.append(s.charAt(i));
                    }
                }
                if (carry) {
                    sb.append('1');
                }
                s = sb.reverse().toString();
            } else {
                //偶数
                s = s.substring(0, s.length() - 1);
            }

        }
        return num;
    }

    public String longestDiverseString(int a, int b, int c) {
        if (a < 0 || b < 0 || c < 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        int[] arr = new int[]{a, b, c};
        int last = -1;
        int all = a + b + c;
        while (arr[0] > 0 || arr[1] > 0 || arr[2] > 0) {
            int index = 0;
            int max = 0;
            for (int i = 0; i < 3; i++) {
                if (last != i) {
                    if (arr[i] > max) {
                        max = arr[i];
                        index = i;
                    }
                }
            }
            if (arr[index] == 0 || index == last) {
                break;
            }
            if (arr[index] - 2 >= 2 * (all - arr[index])) {
                sb.append((char) (index + 'a'));
                sb.append((char) (index + 'a'));
                arr[index] -= 2;
                all -= 2;
            } else {
                sb.append((char) (index + 'a'));
                arr[index] -= 1;
                all -= 1;
            }
            last = index;
        }
        return sb.toString();
    }

    public String stoneGameIII(int[] stoneValue) {
        int[] dp = new int[stoneValue.length + 1];
        Arrays.fill(dp, Integer.MIN_VALUE);
        dp[stoneValue.length] = 0;
        for (int i = stoneValue.length - 1; i >= 0; i--) {
            int result = 0;
            //dp计算：分别为拿1,2,3个石子.
            for (int j = i; j < stoneValue.length && j < i + 3; j++) {
                result += stoneValue[j];
                //result - dp[j + 1]当前取值减去对象接下来的最佳得分
                dp[i] = Math.max(dp[i], result - dp[j + 1]);
            }
        }
        if (dp[0] > 0) {
            return "Alice";
        } else if (dp[0] == 0) {
            return "Tie";
        }
        return "Bob";
    }

    public List<String> stringMatching(String[] words) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            for (int j = 0; j < words.length; j++) {
                if (i != j && words[j].contains(words[i])) {
                    result.add(words[i]);
                    break;
                }
            }
        }
        return result;
    }

    public int[] processQueries(int[] queries, int m) {
        List<Integer> result = new ArrayList<>();
        List<Integer> collect = IntStream.range(1, m + 1).boxed().collect(Collectors.toList());
        for (int query : queries) {
            result.add(collect.indexOf(query));
            collect.remove(new Integer(query));
            collect.add(0, query);
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    public String entityParser(String text) {
        text = text.replaceAll("&quot;", "\"");
        text = text.replaceAll("&apos;", "'");
        text = text.replaceAll("&amp;", "&");
        text = text.replaceAll("&gt;", ">");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&frasl;", "/");
        return text;
    }

    public int[][] merge(int[][] intervals) {
        int len = intervals.length;
        if (len < 1) return intervals;

        //先按最小值排序
        Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));

        List<int[]> list = new ArrayList<>(len);
        for (int i = 0; i < len - 1; i++) {
            if (intervals[i][1] >= intervals[i + 1][0]) {
                intervals[i + 1][0] = intervals[i][0];
                intervals[i + 1][1] = Math.max(intervals[i + 1][1], intervals[i][1]);
            } else list.add(intervals[i]);
        }
        list.add(intervals[len - 1]);

        return list.toArray(new int[list.size()][2]);
    }

    public int game(int[] guess, int[] answer) {
        int result = 0;
        for (int i = 0; i < guess.length; i++) {
            if (guess[i] == answer[i]) result++;
        }
        return result;
    }

    public int minCount(int[] coins) {
        int num = 0;
        for (int coin : coins) {
            if (coin % 2 == 0) {
                num += coin / 2;
            } else {
                num += coin / 2 + 1;
            }
        }
        return num;
    }

    public int numWays(int n, int[][] relation, int k) {
        int num = 0;
        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        for (int i = 0; i < k; i++) {
            Stack<Integer> temp = new Stack<>();
            while (stack.size() > 0) {
                Integer value = stack.pop();
                for (int[] ints : relation) {
                    if (ints[0] == value) {
                        if (i == k - 1 && ints[1] == n - 1) {
                            num++;
                        }
                        temp.push(ints[1]);
                    }
                }
            }
            stack = temp;
        }
        return num;
    }


    public int[] getTriggerTime(int[][] increase, int[][] requirements) {
        int n = requirements.length;
        int m = increase.length;
        int[] result = new int[n];
        int[][] increases = new int[increase.length + 1][3];
        Arrays.fill(result, -1);
        int[] c = new int[increase.length + 1];
        int[] r = new int[increase.length + 1];
        int[] h = new int[increase.length + 1];
        c[0] = 0;
        r[0] = 0;
        h[0] = 0;
        for (int i = 0; i < m; i++) {
            increases[i + 1][0] = increases[i][0] + increase[i][0];
            increases[i + 1][1] += increases[i][1] + increase[i][1];
            increases[i + 1][2] += increases[i][2] + increase[i][2];
            c[i + 1] = increases[i + 1][0];
            r[i + 1] = increases[i + 1][1];
            h[i + 1] = increases[i + 1][2];
        }
        for (int i = 0; i < n; i++) {
            int[] re = requirements[i];
            int value1 = binarySearch(c, re[0]);
            int value2 = binarySearch(r, re[1]);
            int value3 = binarySearch(h, re[2]);
            if (value1 == -1 || value2 == -1 || value3 == -1) {
                result[i] = -1;
            } else {
                result[i] = Math.max(Math.max(value1, value2), value3);
            }

        }
        return result;
    }

    private int binarySearch(int[] arr, int key) {
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

    public int minJump(int[] jump) {
        int n = jump.length;
        int[] dp = new int[n];
        int cur = 0;
        dp[0] = jump[0] >= jump.length ? 1 : 0;
        for (int i = 1; i < n; i++) {
            if (jump[i] + i >= jump.length) {
                dp[i] = 1;
                if (cur == 0) {
                    cur = i;
                }
            } else if (dp[i - 1] > 0) {
                dp[i] = 2;
            }
        }
        while (dp[0] == 0) {
            int value = 0;
            int temp = 0;
            for (int i = 0; i < cur; i++) {
                if (dp[i + jump[i]] > 0) {
                    dp[i] = dp[i + jump[i]] + 1;
                    value = dp[i];
                    if (temp == 0) {
                        temp = i;
                    }
                }
            }
            for (int i = 0; i < cur; i++) {
                if (dp[i] > 0) {
                    value = dp[i];
                }
                if (i > 0 && dp[i - 1] > 0 && dp[i] == 0) {
                    dp[i] = value + 1;
                }
            }
            cur = temp;
        }
        return dp[0];
    }

    public int minStartValue(int[] nums) {
        int num = 0;
        int temp = 0;
        for (int i = 0; i < nums.length; i++) {
            temp += nums[i];
            num = Math.min(temp, num);
        }
        return -num + 1;
    }

    public int findMinFibonacciNumbers(int k) {
        List<Integer> list = new ArrayList<>();
        int a = 1, b = 1, num = 0;
        while (a <= k) {
            list.add(b);
            int temp = a + b;
            a = b;
            b = temp;
        }
        int max = list.size();
        while (k > 0) {
            while (list.get(max - 1) > k) {
                max--;
            }
            k -= list.get(max - 1);
            num++;
        }
        return num;
    }

    public String getHappyString(int n, int k) {
        if (n > 0) {
            char[] chars = new char[n];
            if (3 * Math.pow(2, n - 1) >= k) {
                for (int i = 0; i < n; i++) {
                    if (i % 2 == 1) {
                        chars[i] = 'b';
                    } else {
                        chars[i] = 'a';
                    }
                }
                int[] result = new int[n];
                int num = 0;
                k = k - 1;
                while (k > 0) {
                    if (num != n - 1) {
                        result[num++] = k % 2;
                        k /= 2;
                    } else {
                        result[num++] = k % 3;
                        k /= 3;
                    }
                }

                char[][] c = new char[3][];
                c[0] = new char[2];
                c[0][0] = 'b';
                c[0][1] = 'c';
                c[1] = new char[2];
                c[1][0] = 'a';
                c[1][1] = 'c';
                c[2] = new char[2];
                c[2][0] = 'a';
                c[2][1] = 'b';

                while (num > 0) {
                    if (num == n) {
                        chars[chars.length - num] = (char) ('a' + result[--num]);
                    } else {
                        chars[chars.length - num] = c[chars[chars.length - num - 1] - 'a'][result[--num]];
                    }
                }
                return new String(chars);
            }
        }
        return "";
    }

    public int numberOfArrays(String s, int k) {
        int n = s.length();
        int[] dp = new int[n];
        dp[0] = 1;
        StringBuffer sb = new StringBuffer();
        sb.append(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == '0') {
                dp[i] = dp[i - 1];
            } else {
                dp[i] = dp[i - 1] + i;
            }
        }
        return dp[n - 1];
    }

    public String reformat(String s) {
        List<Character> dists = new ArrayList<>();
        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                dists.add(s.charAt(i));
            } else {
                chars.add(s.charAt(i));
            }
        }
        StringBuilder sb = new StringBuilder();
        if (Math.abs(dists.size() - chars.size()) <= 1) {
            if (chars.size() > dists.size()) {
                sb.append(chars.get(chars.size() - 1));
            }
            for (int i = 0; i < Math.min(dists.size(), chars.size()); i++) {
                sb.append(dists.get(i));
                sb.append(chars.get(i));
            }
            if (dists.size() > chars.size()) {
                sb.append(dists.get(dists.size() - 1));
            }
        }
        return sb.toString();
    }

    public List<List<String>> displayTable(List<List<String>> orders) {
        Map<String, Map<String, Long>> map = orders.stream()
                .collect(Collectors.groupingBy(list -> list.get(1),
                        Collectors.groupingBy(a -> a.get(2),
                                Collectors.counting())));
        List<List<String>> result = new ArrayList<>();
        //所有菜品
        List<String> set = orders.stream().map(a -> a.get(2)).sorted().distinct().collect(Collectors.toList());
        set.add(0, "Table");
        result.add(set);
        List<String> collect = map.keySet().stream().mapToInt(Integer::parseInt).sorted().mapToObj(String::valueOf).collect(Collectors.toList());
        collect.forEach(en -> {
            Map<String, Long> value = map.get(en);
            List<String> list = new ArrayList<>();
            //桌号
            list.add(en);
            for (int i = 1; i < set.size(); i++) {
                Long num = value.get(set.get(i));
                list.add(num == null ? "0" : num.toString());
            }
            result.add(list);
        });
        return result;
    }

    public int minNumberOfFrogs(String croakOfFrogs) {
        Stack<Character> c = new Stack<>();
        Stack<Character> r = new Stack<>();
        Stack<Character> o = new Stack<>();
        Stack<Character> a = new Stack<>();
        int num = 0;
        for (int i = 0; i < croakOfFrogs.length(); i++) {
            if (croakOfFrogs.charAt(i) == 'c') {
                c.push(croakOfFrogs.charAt(i));
            } else if (croakOfFrogs.charAt(i) == 'r') {
                if (c.isEmpty()) {
                    return -1;
                }
                r.push(croakOfFrogs.charAt(i));
            } else if (croakOfFrogs.charAt(i) == 'o') {
                if (c.isEmpty() || r.isEmpty()) {
                    return -1;
                }
                o.push(croakOfFrogs.charAt(i));
            } else if (croakOfFrogs.charAt(i) == 'a') {
                if (c.isEmpty() || r.isEmpty() || o.isEmpty()) {
                    return -1;
                }
                a.push(croakOfFrogs.charAt(i));
            } else {
                if (c.isEmpty() || r.isEmpty() || o.isEmpty() || a.isEmpty()) {
                    return -1;
                }
                c.pop();
                r.pop();
                o.pop();
                a.pop();
                num = Math.max(num, c.size());
            }
        }
        if (!c.isEmpty() || !r.isEmpty() || !o.isEmpty() || !a.isEmpty()) {
            return -1;
        }
        return num + 1;
    }

    public int getMaxRepetitions(String s1, int n1, String s2, int n2) {
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 || len2 == 0 || n1 == 0 || n2 == 0) {
            return 0;
        }
        char[] chars1 = s1.toCharArray();
        char[] chars2 = s2.toCharArray();
        // 记录下一个要匹配的s2中字符的索引
        int index = 0;
        // 记录匹配完的s2个数
        int count = 0;
        // 记录在遍历每个s1时匹配出的s2的个数，可能是包含了前面一个s1循环节的部分
        int[] countRecorder = new int[len2 + 1];
        // 记录在每个s1中想要匹配的s2中字符的索引
        int[] indexRecorder = new int[len2 + 1];
        for (int i = 0; i < n1; ++i) {
            for (int j = 0; j < len1; ++j) {
                // 匹配s2字符，匹配成功，s2索引+1
                if (chars1[j] == chars2[index]) {
                    ++index;
                }
                // 匹配完一个s2，计数器+1，重置s2索引
                if (index == chars2.length) {
                    index = 0;
                    ++count;
                }
            }
            // 记录遍历完i个s1后s2出现的次数
            countRecorder[i] = count;
            // 记录遍历完第i个s1后s2下一个要被匹配到的字符下标
            indexRecorder[i] = index;
            // 剪枝
            // 查看该索引在之前是否已出现，出现即表示已经出现循环节，可以直接进行计算
            // 上一次出现该索引是在第j个s1中（同时可以说明第一个循环节的出现是从第j+1个s1开始的）
            for (int j = 0; j < i && indexRecorder[j] == index; ++j) {
                // preCount: 记录循环节出现之前的s2出现的个数
                int preCount = countRecorder[j];
                // patternCount: 记录所有循环节构成的字符串中出现s2的个数
                //      (n1 - 1 - j) / (i - j): 循环节个数
                //      countRecorder[i] - countRecorder[j]: 一个循环节中包含的s2个数
                int patternCount = ((n1 - 1 - j) / (i - j)) * (countRecorder[i] - countRecorder[j]);
                // remainCount: 记录剩余未构成完整循环节的部分出现的s2的个数
                //      通过取模从已有循环节记录中查找，并减去循环节之前出现的次数
                int remainCount = countRecorder[j + (n1 - 1 - j) % (i - j)] - countRecorder[j];
                // 三者相加，即为出现的s2的总次数
                return (preCount + patternCount + remainCount) / n2;
            }
        }
        // 没有循环节的出现，相当于直接使用暴力法
        return countRecorder[n1 - 1] / n2;
    }

    private boolean max(int[] arr, int k) {
        int maxValue = -1;
        int maxIndex = -1;
        int cost = 0;
        for (int i = 0; i < arr.length; i++) {
            if (maxValue < arr[i]) {
                maxValue = arr[i];
                maxIndex = i;
                cost += 1;
            }
        }
        return cost == k;
    }

    public int countNegatives(int[][] grid) {
        int n = grid.length;
        if (n == 0) {
            return 0;
        }
        int num = 0;
        int m = grid[0].length;
        for (int[] ints : grid) {
            for (int j = 0; j < m; j++) {
                if (ints[j] < 0) {
                    num++;
                }
            }
        }
        return num;
    }

    public int numberOfSubarrays(int[] nums, int k) {
        int n = nums.length;
        int[] dp = new int[n + 1];
        dp[0] = 1;
        int c = 0, ret = 0;
        for (int num : nums) {
            c += num & 1;
            dp[c]++;
            if (c >= k) {
                ret += dp[c - k];
            }
        }
        return ret;
    }

    public int[] replaceElements(int[] arr) {
        int max = arr[arr.length - 1];
        arr[arr.length - 1] = -1;
        for (int i = arr.length - 2; i >= 0; i--) {
            int temp = max;
            max = Math.max(max, arr[i]);
            arr[i] = temp;

        }
        return arr;
    }

    public int oddCells(int n, int m, int[][] indices) {
        //行
        boolean[] r = new boolean[n];
        //列
        boolean[] c = new boolean[m];
        //行与列是否为奇数
        for (int[] index : indices) {
            r[index[0]] = !r[index[0]];
            c[index[1]] = !c[index[1]];
        }
        int rr = 0, cc = 0;
        //行的奇数总数
        for (boolean b : r) {
            if (b) rr++;
        }
        //列的奇数总数
        for (boolean b : c) {
            if (b) cc++;
        }
        //每行rr*m列+ 每列cc*n行- rr * cc * 2（重复计算的数量，*2是因为重复后变偶数）
        return rr * m + cc * n - rr * cc * 2;
    }

    public int maxScore(String s) {
        char[] chars = s.toCharArray();
        int numO = 0;
        int num1 = 0;
        for (char aChar : chars) {
            if (aChar == '0') {
                numO++;
            } else {
                num1++;
            }
        }
        int result = 0, a = 0, b = num1;
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '0') {
                a++;
            } else {
                b--;
            }
            result = Math.max(result, a + b);
        }
        return result;
    }

    public int maxScore(int[] cardPoints, int k) {
        if (k >= cardPoints.length) {
            return (int) Arrays.stream(cardPoints).sum();
        }
        int temp = 0;
        //先算k-0的和
        for (int i = k - 1; i >= 0; i--) {
            temp += cardPoints[i];
        }
        int result = temp;
        //每次滑动1
        for (int i = 1; i <= k; i++) {
            temp = temp - cardPoints[k - i] + cardPoints[cardPoints.length - i];
            result = Math.max(result, temp);
        }
        return result;
    }

    public int[] findDiagonalOrder(List<List<Integer>> nums) {
        List<Point> list = new LinkedList<>();
        for (int i = 0; i < nums.size(); i++) {
            List<Integer> numList = nums.get(i);
            for (int j = 0; j < numList.size(); j++) {
                list.add(new Point(i, j, numList.get(j)));
            }
        }
        Function<Point, Integer> function = p -> p.i + p.j;
        return list.stream()
                .sorted(Comparator.comparing(function).thenComparing(a -> a.j))
                .mapToInt(Point::getValue)
                .toArray();
    }

    public class Point {

        private Integer i;
        private Integer j;
        private Integer value;

        public Point(Integer i, Integer j, Integer value) {
            this.i = i;
            this.j = j;
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static void main(String[] args) {
        ArrayList<Integer> a = Lists.newArrayList(1, 2, 3);
        ArrayList<Integer> b = Lists.newArrayList(4, 5, 6);
        ArrayList<Integer> c = Lists.newArrayList(7, 8, 9);
        List<List<Integer>> nums = Lists.newArrayList(a, b, c);
        System.out.println(new Week180().findDiagonalOrder(nums));
    }
}
