package com.zd.algorithm.letcode.dp;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringTool {

    /**
     * 一年中的第几天
     */
    public int dayOfYear(String date) {
        int day = 0;
        int[] month = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        String[] split = date.split("-");
        if (isR(Integer.parseInt(split[0]))) {
            month[1] += 1;
        }
        for (int i = 0; i < Integer.parseInt(split[1]) - 1; i++) {
            day += month[i];
        }
        day += Integer.parseInt(split[2]);
        return day;
    }

    private boolean isR(int year) {
        if (year % 4 == 0 && year % 100 != 0) {
            return true;
        }
        if (year % 400 == 0) {
            return true;
        }
        return false;
    }

    public String reverseLeftWords(String s, int n) {
        StringBuilder temp = new StringBuilder();
        StringBuilder news = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i < n) {
                temp.append(s.charAt(i));
            } else {
                news.append(s.charAt(i));
            }
        }
        news.append(temp);
        return news.toString();
    }

    public int numberOfSteps(int num) {
        int step = 0;
        while (num != 0) {
            if (num % 2 == 0) {
                num /= 2;
            } else {
                num--;
            }
            step++;
        }
        return step;
    }

    public int subtractProductAndSum(int n) {
        int add = 0;
        int cen = 1;
        while (n > 0) {
            int num = n % 10;
            add += num;
            cen *= num;
            n /= 10;
        }
        return Math.abs(add - cen);
    }

    public int numJewelsInStones(String J, String S) {
        int num = 0;
        for (int i = 0; i < J.length(); i++) {
            for (int j = 0; j < S.length(); j++) {
                if (J.charAt(i) == S.charAt(j)) {
                    num++;
                }
            }
        }
        return num;
    }

    public int findNumbers(int[] nums) {
        int result = 0;
        for (int num : nums) {
            int n = num;
            int m = 0;
            while (n > 0) {
                n /= 10;
                m++;
            }
            if (m % 2 == 0) {
                result++;
            }
        }
        return result;
    }

    public int[] printNumbers(int n) {
        int result = 0;
        for (int i = 0; i < n; i++) {
            result = result * 10 + 9;
        }
        return IntStream.range(0, result).toArray();
    }


    public int balancedStringSplit(String s) {
        int rNum = 0, lNum = 0, result = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 'R') {
                rNum++;
            } else {
                lNum++;
            }
            if (rNum == lNum) {
                rNum = 0;
                lNum = 0;
                result++;
            }
        }
        return result;
    }

    public int[] sortedSquares(int[] A) {
        for (int i = 0; i < A.length; i++) {
            A[i] = A[i] * A[i];
        }
        Arrays.sort(A);
        return A;
    }

    public String replaceSpace(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') {
                sb.append("%20");
            } else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

    public int peakIndexInMountainArray(int[] A) {
        for (int i = 0; i < A.length - 1; i++) {
            if (A[i + 1] < A[i]) {
                return i;
            }
        }
        return A.length - 1;
    }

    public int arrayPairSum(int[] nums) {
        int result = 0;
        Arrays.sort(nums);
        for (int i = 0; i < nums.length; i += 2) {
            result += nums[i];
        }
        return result;
    }

    public int binaryGap(int N) {
        int last = -1, res = 0;
        //32次
        for (int i = 0; i < 32; i++) {
            if ((N >> i & 1) == 1) {
                if (last != -1) {
                    res = Math.max(res, i - last);
                }
                last = i;
            }
        }
        return res;
    }

    public int[] decompressRLElist(int[] nums) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < nums.length; i += 2) {
            for (int j = 0; j < nums[i]; j++) {
                list.add(nums[i + 1]);
            }
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public boolean judgeSquareSum(int c) {
        if (c == 0) {
            return true;
        }
        for (int i = 0; i * i <= c; i++) {
            int left = 0, right = c;
            while (left < right) {
                int mid = (left + right) / 2;
                int value = i * i + mid * mid;
                if (value < c) {
                    left = mid + 1;
                } else if (value > c) {
                    right = mid;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public int waysToChange(int n) {
        int ans = 0;
        for (int i = 0; i * 25 <= n; ++i) {
            int rest = n - i * 25;
            int a = rest / 10;
            int b = rest % 10 / 5;
            ans = (int) (ans + (long) (a + 1) * (a + b + 1) % 1000000007) % 1000000007;
        }
        return ans;
    }

    /**
     * 递归反转字符串
     */
    public void reverseString(char[] s) {
        reverseString(s, 0);
    }

    private void reverseString(char[] s, int index) {
        if (index == s.length) {
            return;
        }
        char c = s[index];
        reverseString(s, index + 1);
        s[s.length - index - 1] = c;
    }

    /**
     * 二进制求和
     */
    public String addBinary(String a, String b) {
        //保证a是长的
        if (b.length() > a.length()) {
            String temp = a;
            a = b;
            b = temp;
        }
        StringBuilder sb = new StringBuilder();
        int carry = 0;
        for (int i = a.length() - 1, j = b.length() - 1; i >= 0; i--, j--) {
            char d = '0';
            char c = a.charAt(i);
            if (j >= 0) {
                d = b.charAt(j);
            }
            int value = (c - '0') + (d - '0') + carry;
            if (value >= 2) {
                //进位
                carry = 1;
            } else {
                carry = 0;
            }
            sb.append(value % 2);
        }
        if (carry > 0) {
            sb.append(1);
        }
        return sb.reverse().toString();
    }

    /**
     * 连续1最大数
     */
    public int findMaxConsecutiveOnes(int[] nums) {
        int result = 0, temp = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] > 0) {
                temp++;
            } else {
                result = Math.max(result, temp);
                temp = 0;
            }
        }
        result = Math.max(result, temp);
        return result;
    }

    public int minSubArrayLen(int s, int[] nums) {
        int n = nums.length;
        int ans = Integer.MAX_VALUE;
        int left = 0;
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += nums[i];
            while (sum >= s) {
                ans = Math.min(ans, i + 1 - left);
                sum -= nums[left++];
            }
        }
        return (ans != Integer.MAX_VALUE) ? ans : 0;
    }

    public String reverseWords(String s) {
        char[] cs = s.toCharArray();
        int j = 0;
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == ' ' || i == cs.length - 1) {
                int k = i;
                if (cs[i] == ' ') {
                    k--;
                }
                //遇到空格,反转前面的数组
                while (j < k) {
                    cs[k] ^= cs[j];
                    cs[j] ^= cs[k];
                    cs[k] ^= cs[j];
                    j++;
                    k--;
                }
                //交换结束
                j = i + 1;
            }
        }
        return new String(cs);
    }

    /**
     * 数独
     */
    public boolean isValidSudoku(char[][] board) {
        char[][] cs = new char[10][10];
        char[][] ca = new char[10][10];
        char[][][] ka = new char[3][3][10];
        int k = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == '.') {
                    continue;
                }
                cs[i][board[i][j] - '0']++;
                if (cs[i][board[i][j] - '0'] > 1) {
                    return false;
                }
                ca[board[i][j] - '0'][j]++;
                if (ca[board[i][j] - '0'][j] > 1) {
                    return false;
                }
                ka[i / 3][j / 3][board[i][j] - '0']++;
                if (ka[i / 3][j / 3][board[i][j] - '0'] > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public int lengthOfLongestSubstring(String s) {
        List<Character> list = new ArrayList<>();
        int result = 0;
        for (char c : s.toCharArray()) {
            if (list.contains(c)) {
                result = Math.max(result, list.size());
                list = list.subList(list.indexOf(c) + 1, list.size());
            }
            list.add(c);
        }
        result = Math.max(result, list.size());
        return result;
    }

    public String[] findRestaurant(String[] list1, String[] list2) {
        HashMap<Integer, List<String>> map = new HashMap<>();
        for (int i = 0; i < list1.length; i++) {
            for (int j = 0; j < list2.length; j++) {
                if (list1[i].equals(list2[j])) {
                    if (!map.containsKey(i + j))
                        map.put(i + j, new ArrayList<String>());
                    map.get(i + j).add(list1[i]);
                }
            }
        }
        int min_index_sum = Integer.MAX_VALUE;
        for (int key : map.keySet())
            min_index_sum = Math.min(min_index_sum, key);
        String[] res = new String[map.get(min_index_sum).size()];
        return map.get(min_index_sum).toArray(res);
    }

    public List<Boolean> kidsWithCandies(int[] candies, int extraCandies) {
        int max = Arrays.stream(candies).max().getAsInt();
        return Arrays.stream(candies)
                .boxed()
                .map(a -> a + extraCandies > max)
                .collect(Collectors.toList());
    }

    public int maxDiff(int num) {
        String s = String.valueOf(num);
        int a = num, b = num;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '9') {
                a = Integer.parseInt(s.replace(s.charAt(i), '9'));
                break;
            }
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(0) != '1') {
                b = Integer.parseInt(s.replace(s.charAt(0), '1'));
                break;
            } else if (i > 0 && s.charAt(i) > '1') {
                b = Integer.parseInt(s.replace(s.charAt(i), '0'));
                break;
            }
        }
        return a - b;
    }

    public boolean checkIfCanBreak(String s1, String s2) {
        char[] c1 = s1.toCharArray();
        char[] c2 = s2.toCharArray();
        Arrays.sort(c1);
        Arrays.sort(c2);
        //a全部c1>=c2
        boolean a = true, b = true;
        for (int i = 0; i < c1.length; i++) {
            if (c1[i] < c2[i]) {
                a = false;
            }
            if (c2[i] < c1[i]) {
                b = false;
            }
        }
        return a || b;
    }

    public int numberWays(List<List<Integer>> hats) {
        //dp为帽子，有几个人能戴
        int[] peoples = new int[40];
        int p = 1;
        //一共10个人,最多10位，没一位1代表一个人
        for (List<Integer> hat : hats) {
            for (Integer h : hat) {
                peoples[h - 1] |= p;
            }
            p <<= 1;
        }

        //人数
        int[] dp = new int[p];
        dp[0] = 1;
        for (int people : peoples) {
            //0说明这个帽子没人愿意带
            if (people == 0) {
                continue;
            }
            //有人愿意带的帽子
            for (int status = p; status > 0; status--) {
                //每位取出
                for (int mask = people & status, pp = 0; mask > 0; mask ^= pp) {
                    //哪一位
                    pp = (-mask) & mask;
                    dp[status] = (dp[status ^ pp] + dp[status]) % 1000000007;
                }
            }
        }
        return dp[p - 1];
    }

    public String destCity(List<List<String>> paths) {
        Map<String, Long> a = paths.stream().collect(Collectors.groupingBy(list -> list.get(0), Collectors.counting()));
        for (List<String> path : paths) {
            if (!a.containsKey(path.get(1))) {
                return path.get(1);
            }
        }
        return "";
    }

    public boolean kLengthApart(int[] nums, int k) {
        //上一个
        int last = -1;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == 1) {
                if (last < 0) {
                    last = i;
                } else {
                    if (i - last - 1 < k) {
                        return false;
                    }
                    last = i;
                }
            }
        }
        return true;
    }

    public int longestSubarray(int[] nums, int limit) {
        TreeMap<Integer, Integer> tree = new TreeMap<>(Integer::compareTo);
        int l = 0, max = 0, r = 1;
        tree.put(nums[0], 1);
        for (; r < nums.length; r++) {
            int key = nums[r];
            tree.compute(key, (k, v) -> v == null ? 1 : v + 1);
            if (Math.abs(tree.lastKey() - tree.firstKey()) > limit) {
                max = Math.max(r - l, max);
                while (Math.abs(tree.lastKey() - tree.firstKey()) > limit) {
                    key = nums[l++];
                    if (tree.get(key) > 1) {
                        tree.put(key, tree.get(key) - 1);
                    } else {
                        tree.remove(key);
                    }
                }
            }
        }
        max = Math.max(r - l, max);
        return max;
    }

    public int kthSmallest(int[][] mat, int k) {
        //最小和
        int sum = 0;
        for (int i = 0; i < mat.length; i++) {
            sum += mat[i][0];
        }
        PriorityQueue<Item> pq = new PriorityQueue<>(Comparator.comparingInt(o -> o.total));
        Set<String> visit = new HashSet<>();
        Item first = new Item(new int[mat.length], sum);
        visit.add(Arrays.toString(first.pick));
        pq.offer(first);
        while (k > 1) {
            Item item = pq.poll();
            for (int i = 0; i < mat.length; i++) {
                item.pick[i]++;
                if (item.pick[i] < mat[i].length && !visit.contains(Arrays.toString(item.pick))) {
                    visit.add(Arrays.toString(item.pick));
                    int[] pick = Arrays.copyOf(item.pick, item.pick.length);

                    int total = item.total - mat[i][item.pick[i] - 1] + mat[i][item.pick[i]];
                    pq.add(new Item(pick, total));
                }

                item.pick[i]--;
            }
            k--;
        }
        return pq.peek().total;
    }

    private class Item {
        private int[] pick;
        private int total;

        public Item(int[] pick, int total) {
            this.pick = pick;
            this.total = total;
        }

    }

    public boolean containsNearbyDuplicate(int[] nums, int k) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (map.containsKey(nums[i])) {
                if (i - map.get(nums[i]) <= k) {
                    return true;
                }
            }
            map.put(nums[i], i);
        }
        return false;
    }

    public List<List<String>> groupAnagrams(String[] strs) {
        return new ArrayList<>(Arrays.stream(strs).collect(Collectors.groupingBy(s -> {
            char[] chars = s.toCharArray();
            Arrays.sort(chars);
            return new String(chars);
        })).values());
    }

    public int fourSumCount(int[] A, int[] B, int[] C, int[] D) {
        int result = 0;
        Map<Integer, Integer> map = new HashMap<>();
        for (int item : A) {
            for (int value : B) {
                map.compute(item + value, (k, v) -> v == null ? 1 : v + 1);
            }
        }
        for (int item : C) {
            for (int value : D) {
                if (map.containsKey(-(item + value))) {
                    result += map.get(-(item + value));
                }
            }
        }
        return result;
    }


    public int[] topKFrequent(int[] nums, int k) {
        return Arrays.stream(nums)
                .boxed()
                .collect(Collectors
                        .groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(k)
                .mapToInt(Map.Entry::getKey)
                .toArray();
    }

    public int search(int[] nums, int target) {
        int l = 0;
        int r = nums.length - 1;
        while (l <= r) {
            int mid = (l + r) / 2;
            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] > target) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return -1;
    }

    /**
     * 寻找峰值
     */
    public int findPeakElement(int[] nums) {
        int l = 0;
        int r = nums.length - 1;
        while (l <= r) {
            int mid = (l + r) / 2;
            if ((mid == 0 || nums[mid] > nums[mid - 1]) && (mid == nums.length - 1 || nums[mid] > nums[mid + 1])) {
                return mid;
            }
            if (mid == 0 || nums[mid] > nums[mid - 1]) {
                //前半段
                l = mid + 1;
            } else {
                //后半段
                r = mid - 1;
            }
        }
        return -1;
    }

    public int findMin(int[] nums) {
        if (nums.length == 1) {
            return nums[0];
        }
        int left = 0, right = nums.length - 1;
        if (nums[right] > nums[0]) {
            return nums[0];
        }
        while (right >= left) {
            int mid = left + (right - left) / 2;
            if (nums[mid] > nums[mid + 1]) {
                return nums[mid + 1];
            }
            if (nums[mid - 1] > nums[mid]) {
                return nums[mid];
            }
            if (nums[mid] > nums[0]) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        System.out.println(new StringTool().findMin(new int[]{1,2,3}));
    }
}
