package com.zd.algorithm.letcode.week;

import com.google.common.collect.Lists;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Week181 {

    public int maxPower(String s) {
        int j = 0, count = 1, result = 1;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(j)) {
                count++;
            } else {
                j = i;
                count = 1;
            }
            result = Math.max(result, count);
        }
        return result;
    }

    public List<String> simplifiedFractions(int n) {
        List<String> list = new ArrayList<>();
        if (n > 1) {
            for (int i = 2; i <= n; i++) {
                int a = i - 1;
                while (a >= 1) {
                    if (new BigInteger(a + "").gcd(new BigInteger(i + "")).intValue() == 1) {
                        list.add(a + "/" + i);
                    }
                    a--;
                }
            }
        }
        return list;
    }

    public String largestNumber(int[] cost, int target) {
        int[] f = new int[target + 1];
        Arrays.fill(f, -1000);
        f[0] = 0;
        for (int i = 1; i <= target; i++) {
            for (int j = 1; j <= 9; j++) {
                int c = cost[j - 1];
                if (i >= c) {
                    f[i] = Math.max(f[i], f[i - c] + 1);
                }
            }
        }
        if (f[target] < 0) {
            return "0";
        } else {
            StringBuilder ret = new StringBuilder();
            int now = target;
            while (now > 0) {
                for (int i = 9; i >= 1; i--) {
                    int c = cost[i - 1];
                    if (now >= c && f[now] == f[now - c] + 1) {
                        ret.append(i);
                        now -= c;
                        break;
                    }
                }
            }
            return ret.toString();
        }
    }

    public int busyStudent(int[] startTime, int[] endTime, int queryTime) {
        int res = 0;
        for (int i = 0; i < startTime.length; i++) {
            if (startTime[i] <= queryTime && endTime[i] >= queryTime) res++;
        }
        return res;
    }

    public String arrangeWords(String text) {
        //根据空格分隔
        String[] s = text.split(" ");
        String collect = Arrays.stream(s)
                .sorted(Comparator.comparing(String::length))
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
        char[] chars = collect.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    class Node {
        Set<String> set;
        Node next;
        int id;

        Node(Set<String> set) {
            this.set = set;
        }
    }

    public List<Integer> peopleIndexes(List<List<String>> favoriteCompanies) {
        Node head = new Node(null);
        Node tail = head;
        Map<String, Set<Node>> map = new HashMap<>();
        for (int i = 0; i < favoriteCompanies.size(); i++) {
            List<String> coll = favoriteCompanies.get(i);
            Set<String> set = new HashSet<>();
            Node node = new Node(set);
            node.id = i;
            tail.next = node;
            tail = tail.next;

            for (String cmp : coll) {
                set.add(cmp);
                map.compute(cmp, (k, v) -> {
                    Set<Node> cset = v;
                    if (cset == null) {
                        cset = new HashSet<>();
                    }
                    cset.add(node);
                    return cset;
                });
            }
        }
        List<Integer> list = new ArrayList<>();
        Node n = head;
        while ((n = n.next) != null) {
            if (n.set.size() == 0) {
                continue;
            }

            Iterator<String> iterator = n.set.iterator();
            String cmp = iterator.next();
            Set<Node> cset = map.get(cmp);
            if (cset.size() == 1) {
                list.add(n.id);
                continue;
            }
            boolean contain = false;
            for (Node node : cset) {
                if (node == n) {
                    continue;
                }
                boolean c = true;
                for (String icmp : n.set) {
                    if (!node.set.contains(icmp)) {
                        c = false;
                        break;
                    }
                }
                if (c) {
                    contain = true;
                    break;
                }
            }

            if (!contain) {
                list.add(n.id);
            }
        }
        return list;
    }

    private int r, num;

    public int numPoints(int[][] p, int r) {
        if (p == null) {
            return 0;
        } else if (p.length <= 1) {
            return p.length;
        }
        this.r = r;
        this.num = p.length;
        int res = 1;
        for (int i = 0; i < num; ++i) {
            for (int j = i + 1; j < num; ++j) {
                if (dist(p[i][0], p[i][1], p[j][0], p[j][1]) > 2 * r) {
                    continue;
                }
                double[] center = getCircleCenter(p[i][0], p[i][1], p[j][0], p[j][1]);
                int cnt = 0;
                for (int k = 0; k < num; ++k) {
                    if (dist(center[0], center[1], p[k][0], p[k][1]) < r + 0.000001) {
                        ++cnt;
                    }
                }
                res = Math.max(res, cnt);
            }
        }
        return res;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private double[] getCircleCenter(double x1, double y1, double x2, double y2) {
        double m1 = (x1 + x2) / 2, m2 = (y1 + y2) / 2;
        double angle = Math.atan2(x1 - x2, y2 - y1);
        double d = Math.sqrt(r * r - Math.pow(dist(x1, y1, m1, m2), 2));
        return new double[]{m1 + d * Math.cos(angle), m2 + d * Math.sin(angle)};
    }

    public int countNum(int x, int y, int r, int[][] points) {
        int count = 0;
        for (int[] point : points) {
            if (Math.pow(Math.pow(point[0] - x, 2) + Math.pow(point[1] - y, 2), 0.5) <= r) {
                count++;
            }
        }
        return count;
    }

    public int isPrefixOfWord(String sentence, String searchWord) {
        String[] sentences = sentence.split(" ");
        for (int i = 0; i < sentences.length; i++) {
            boolean flag = true;
            if (sentences[i].length() < searchWord.length()) {
                continue;
            }
            for (int j = 0; j < searchWord.length(); j++) {
                if (sentences[i].charAt(j) != searchWord.charAt(j)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return i + 1;
            }
        }
        return -1;
    }

    public int maxVowels(String s, int k) {
        int result = 0, temp = 0;
        for (int i = 0; i < k; i++) {
            if (isA(s.charAt(i))) {
                temp++;
            }
        }
        result = temp;
        for (int i = k; i < s.length(); i++) {
            if (isA(s.charAt(i - k))) temp--;
            if (isA(s.charAt(i))) temp++;
            result = Math.max(result, temp);
        }
        return result;
    }

    private boolean isA(char a) {
        switch (a) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return true;
        }
        return false;
    }

    public int maxDotProduct(int[] nums1, int[] nums2) {
        int m = nums1.length;
        int n = nums2.length;
        int[][] dp = new int[m][n];
        for (int i = 0; i < m; i++) {
            Arrays.fill(dp[i], -100000000);
        }
        int ret = -100000000;
        dp[0][0] = nums1[0] * nums2[0];
        //初始化
        for (int i = 1; i < n; i++) {
            //选择一个时的最大值
            dp[0][i] = Math.max(dp[0][i - 1], nums1[0] * nums2[i]);
            ret = Math.max(dp[0][i], ret);
        }
        for (int i = 1; i < m; i++) {
            //选择一个时的最大值
            dp[i][0] = Math.max(dp[i - 1][0], nums1[i] * nums2[0]);
            ret = Math.max(dp[i][0], ret);
        }
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                //去掉num1当前，或者去掉num2当前，或者只取nums1[i] * nums2[j]，或者前一个最大值加上当前乘积
                dp[i][j] = Math.max(dp[i][j - 1],
                        Math.max(dp[i - 1][j],
                                Math.max(dp[i - 1][j - 1] + nums1[i] * nums2[j], nums1[i] * nums2[j])));
                ret = Math.max(dp[i][j], ret);
            }
        }
        return ret;
    }

    public int[] shuffle(int[] nums, int n) {
        int[] result = new int[2 * n];
        for (int i = 0, j = 0; i < n; i++, j += 2) {
            result[j] = nums[i];
            result[j + 1] = nums[n + i];
        }
        return result;
    }

    public int[] getStrongest(int[] arr, int k) {
        List<Integer> list = new ArrayList<>();
        Arrays.sort(arr);
        //中位数
        int m = arr[(arr.length - 1) / 2];
        int i = 0, j = arr.length - 1;
        while (list.size() < k) {
            if (Math.abs(arr[i] - m) > Math.abs(arr[j] - m)) {
                list.add(arr[i]);
                i++;
            } else {
                list.add(arr[j]);
                j--;
            }
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public int minCost(int[] houses, int[][] cost, int m, int n, int target) {
        int ans = 100000;
        //i:房子数 ，j：街区数， k：装修成什么
        int[][][] dp = new int[m + 1][m + 1][n + 1];
        for (int i = 0; i < m + 1; i++) {
            for (int j = 0; j < m + 1; j++) {
                Arrays.fill(dp[i][j], 100000);
            }
        }
        dp[0][0][0] = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j <= i; j++) {
                for (int k = 0; k <= n; k++) {
                    //说明不需要装修
                    if (houses[i] > 0) {
                        //j + (k != houses[i] ? 1 : 0): 装修成K是不是会多一个街区
                        // 因为不需要装修，所以没有花费，取最小值就好
                        dp[i + 1][j + (k != houses[i] ? 1 : 0)][houses[i]]
                                = Math.min(dp[i + 1][j + (k != houses[i] ? 1 : 0)][houses[i]], dp[i][j][k]);
                    } else {
                        //装修
                        for (int l = 1; l <= n; l++) {
                            dp[i + 1][j + (k != l ? 1 : 0)][l] = Math.min(dp[i + 1][j + (k != l ? 1 : 0)][l], dp[i][j][k] + cost[i][l - 1]);
                        }
                    }
                }
            }
        }
        //取街全部装修完的最小费用
        for (int l = 1; l <= n; l++) {
            ans = Math.min(ans, dp[m][target][l]);
        }
        return ans == 100000 ? -1 : ans;
    }

    public int[] finalPrices(int[] prices) {
        int n = prices.length;
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            boolean ret = true;
            for (int j = i + 1; j < n; j++) {
                if (prices[j] <= prices[i]) {
                    res[i] = prices[i] - prices[j];
                    ret = false;
                    break;
                }
            }
            if (ret) {
                res[i] = prices[i];
            }
        }
        return res;
    }

    public int minSumOfLengths(int[] arr, int target) {
        //总数对应的索引
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 0);
        //数字的总和
        int sum = 0;
        int n = arr.length, ans = 0x3f3f3f3f;
        //dp:表示当前最小值
        int[] dp = new int[100005];
        Arrays.fill(dp, 0x3f);
        for (int i = 1; i <= n; i++) {
            sum += arr[i - 1];
            //剩余数
            int gp = sum - target;
            //若当前值没有默认前一个值
            dp[i] = dp[i - 1];
            //若存在数gp则表示从  pos -> i 是一个子数组
            if (map.containsKey(gp)) {
                int pos = map.get(gp);
                //设置dp的值
                dp[i] = Math.min(dp[i], i - pos);
                //ans为当前加上到pos的最小值
                ans = Math.min(ans, i - pos + dp[pos]);
            }
            map.put(sum, i);
        }
        return ans >= 0x3f ? -1 : ans;
    }

    public int minDistance(int[] houses, int k) {
        Arrays.sort(houses);
        int n = houses.length;
        //总距离
        int[] s = new int[n + 1];
        for (int i = 0; i < n; i++) s[i + 1] = s[i] + houses[i];
        //第i房子和j房子只有一个邮箱的差距
        int[][] w = new int[n + 1][n + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = i; j <= n; j++) {
                w[i][j] = s[j] - s[i + j - 1 >> 1] - s[i + j >> 1] + s[i - 1];
            }
        }
        //表示i栋房子在k个邮箱的最小值
        int[][] dp = new int[n + 1][k + 1];
        for (int[] ints : dp) {
            Arrays.fill(ints, 10000);
        }
        dp[0][0] = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= i && j <= k; j++) {
                for (int l = 0; l < i; l++) {
                    //表示 从l个j-1个的最小值+ 多一个邮箱(l+1到i只有1个邮箱)
                    dp[i][j] = Math.min(dp[i][j], dp[l][j - 1] + w[l + 1][i]);
                }
            }
        }
        return dp[n][k];
    }

    public int[] runningSum(int[] nums) {
        int[] res = new int[nums.length];
        int sum = 0;
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];
            res[i] = sum;
        }
        return res;
    }

    public int findLeastNumOfUniqueInts(int[] arr, int k) {
        Map<Integer, Long> map = Arrays.stream(arr).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        int result = map.size();
        List<Long> list = map.values().stream().sorted().collect(Collectors.toList());
        for (Long value : list) {
            if (k >= value) {
                result--;
                k -= value;
            }
        }
        return result;
    }

    public int minDays(int[] bloomDay, int m, int k) {
        if (m*k > bloomDay.length) {
            return -1;
        }
        // 最大等待的天数是数组里的最大值
        int max=0;
        for (int i : bloomDay) {
            max = Math.max(max, i);
        }
        // 最小等待0天
        int min=0;
        while (min < max) {
            // mid:等待天数
            int mid = min + (max-min)/2;
            // 等待mid天，有多少组连续的k朵花已经开花🌼了
            int count = getCount(bloomDay, mid, k);
            if (count >= m) {
                max = mid;
            } else {
                min = mid+1;
            }
        }
        return min;
    }
    // 返回等待day天，有多少组连续的k天<=day  这里用的贪心
    private int getCount(int[] arr, int day, int k) {
        int re=0;
        int count=0;
        for (int i=0; i<arr.length; i++) {
            if (arr[i] <= day) {
                count++;
            } else {
                count = 0;
            }
            //  连续的k朵花🌼开了
            if (count == k) {
                re++;
                count=0;
            }
        }
        return re;
    }
    

    public static void main(String[] args) {
        System.out.println(new Week181().minDays(new int[]{1, 10, 3, 10, 2}, 3, 1));
        System.out.println(new Week181().minDays(new int[]{1, 10, 2, 9, 3, 8, 4, 7, 5, 6}, 4, 2));
    }
}
