package com.zd.algorithm.letcode.week;

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

    public static void main(String[] args) {
        System.out.println(new Week180().entityParser("&amp; is an HTML entity but &ambassador; is not."));
    }
}
