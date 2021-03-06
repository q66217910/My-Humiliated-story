package com.zd.algorithm.letcode.dp;

import com.google.common.collect.Comparators;
import com.google.common.collect.Lists;

import java.time.LocalDate;
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

    /**
     * 在排序数组中查找元素的第一个和最后一个位置
     */
    private int extremeInsertionIndex(int[] nums, int target, boolean left) {
        int lo = 0;
        int hi = nums.length;

        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (nums[mid] > target || (left && target == nums[mid])) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }

        return lo;
    }

    public int[] searchRange(int[] nums, int target) {
        int[] targetRange = {-1, -1};

        int leftIdx = extremeInsertionIndex(nums, target, true);

        // assert that `leftIdx` is within the array bounds and that `target`
        // is actually in `nums`.
        if (leftIdx == nums.length || nums[leftIdx] != target) {
            return targetRange;
        }

        targetRange[0] = leftIdx;
        targetRange[1] = extremeInsertionIndex(nums, target, false) - 1;

        return targetRange;
    }

    public List<Integer> findClosestElements(int[] arr, int k, int x) {
        return Arrays.stream(arr)
                .boxed()
                .sorted(Comparator.comparing(a -> Math.abs(a - x)))
                .limit(k)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 是否是完全平方数
     */
    public boolean isPerfectSquare(int num) {
        if (num < 2) {
            return true;
        }
        long l = 2, r = num / 2;
        while (l <= r) {
            long mid = l + ((r - l) >>> 1);
            long value = mid * mid;
            if (value == num) {
                return true;
            } else if (value > num) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return false;
    }

    public char nextGreatestLetter(char[] letters, char target) {
        if (letters[letters.length - 1] <= target) {
            return letters[0];
        }
        int l = 0, r = letters.length - 1;
        while (l <= r) {
            int mid = l + ((r - l) >>> 1);
            if (letters[mid] > target) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return letters[l];
    }

    public int findMin2(int[] nums) {
        int l = 0, r = nums.length - 1;
        while (l < r) {
            int mid = l + ((r - l) >>> 1);
            if (nums[mid] < nums[r]) {
                //后半断
                r = mid;
            } else if (nums[mid] > nums[r]) {
                //前半断
                l = mid + 1;
            } else {
                r--;
            }
        }
        return nums[l];
    }

    public int firstUniqChar(String s) {
        char[] chars = s.toCharArray();
        Map<Character, Long> collect = IntStream.range(0, s.length())
                .boxed()
                .map(i -> chars[i])
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (int i = 0; i < chars.length; i++) {
            if (collect.get(chars[i]) == 1) {
                return i;
            }
        }
        return -1;
    }

    public String frequencySort(String s) {
        Map<Character, Integer> map = new HashMap<>();
        for (char c : s.toCharArray()) {
            map.put(c, map.getOrDefault(c, 0) + 1);
        }
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(entry -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < entry.getValue(); i++) {
                        sb.append(entry.getKey());
                    }
                    return sb.toString();
                }).collect(Collectors.joining());
    }

    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> list = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) {
            int target = -nums[i];
            int l = i + 1, r = nums.length - 1;
            //当前值大于0说明和必大于0
            if (nums[i] > 0) break;
            if (i > 0 && nums[i] == nums[i - 1]) continue;
            while (l < r) {
                int s = nums[i] + nums[l] + nums[r];
                if (s == 0) {
                    List<Integer> value = new ArrayList<>();
                    value.add(nums[i]);
                    value.add(nums[l]);
                    value.add(nums[r]);
                    list.add(value);
                    while (l + 1 < r && nums[l] == nums[l + 1]) l++;
                    while (l < r - 1 && nums[r] == nums[r - 1]) r--;
                    l++;
                    r--;
                } else if (s < 0) {
                    l++;
                } else {
                    r--;
                }
            }
        }
        return list;
    }

    public List<List<Integer>> fourSum(int[] nums, int target) {
        int n = nums.length;
        Arrays.sort(nums);
        List<List<Integer>> ans = new ArrayList<>();
        for (int i = 0; i < n - 3; i++) {
            //去重
            if (i > 0 && nums[i] == nums[i - 1]) continue;
            //四数相加大于target,后面肯定都大于了
            if (nums[i] + nums[i + 1] + nums[i + 2] + nums[i + 3] > target) break;
            //当前数与最大的三个数的和还小，直接下个数
            if (nums[i] + nums[n - 1] + nums[n - 2] + nums[n - 3] < target) continue;
            //固定了第一个数开始找第二个数
            for (int j = i + 1; j < n - 2; j++) {
                //去重
                if (j - i > 1 && nums[j] == nums[j - 1]) continue;
                //四数相加大于target,后面肯定都大于了
                if (nums[i] + nums[j] + nums[j + 1] + nums[j + 2] > target) break;
                //当前数与最大的2个数的和还小，直接下个数
                if (nums[i] + nums[j] + nums[n - 1] + nums[n - 2] < target) continue;
                //寻找最后两个数
                int left = j + 1;
                int right = n - 1;
                while (left < right) {
                    int tmp = nums[i] + nums[j] + nums[left] + nums[right];
                    if (tmp == target) {
                        List<Integer> tmpList = new LinkedList<>(Arrays.asList(nums[i], nums[j], nums[left], nums[right]));
                        ans.add(tmpList);
                        while (left < right && nums[left] == nums[left + 1]) left += 1;
                        while (left < right && nums[right] == nums[right - 1]) right -= 1;
                        left += 1;
                        right -= 1;
                    } else if (tmp > target) right -= 1;
                    else left += 1;
                }
            }
        }
        return ans;
    }


    public int maxPoints(int[][] points) {
        if (points.length < 3) {
            return points.length;
        }
        int result = 0;
        for (int i = 0; i < points.length; i++) {
            int duplicate = 0;
            int max = 0;//保存经过当前点的直线中，最多的点
            //斜率出现的次数
            Map<String, Integer> map = new HashMap<>();
            for (int j = i + 1; j < points.length; j++) {
                //求出分子分母
                int x = points[j][0] - points[i][0];
                int y = points[j][1] - points[i][1];
                if (x == 0 && y == 0) {
                    duplicate++;
                    continue;

                }
                //进行约分
                int gcd = gcd(x, y);
                x = x / gcd;
                y = y / gcd;
                String key = x + "@" + y;
                map.put(key, map.getOrDefault(key, 0) + 1);
                max = Math.max(max, map.get(key));
            }
            result = Math.max(result, max + duplicate + 1);
        }
        return result;
    }

    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = a % b;
            a = b;
            b = temp;
        }
        return a;
    }

    public boolean checkInclusion(String s1, String s2) {
        char[] chars1 = s1.toCharArray();
        Arrays.sort(chars1);
        s1 = new String(chars1);
        for (int i = s1.length(); i <= s2.length(); i++) {
            char[] chars = Arrays.copyOfRange(s2.toCharArray(), i - s1.length(), i);
            Arrays.sort(chars);
            if (s1.equals(new String(chars))) {
                return true;
            }
        }
        return false;
    }

    public String simplifyPath(String path) {
        Stack<String> stack = new Stack<>();
        //根据/分割字符串
        String[] str = path.split("/");
        for (String s : str) {
            if ("..".equals(s)) {
                //若是。。返回上一层，所以当前层的地址去掉
                if (!stack.empty()) {
                    stack.pop();
                }
            } else if (!s.equals("") && !s.equals(".")) {
                stack.push(s);
            }
        }
        if (stack.isEmpty()) {
            return "/";
        }
        // 这里用到 StringBuilder 操作字符串，效率高
        StringBuilder ans = new StringBuilder();
        for (String s : stack) {
            ans.append("/").append(s);
        }
        return ans.toString();
    }

    private long getID(long x, long w) {
        return x < 0 ? (x + 1) / w - 1 : x / w;
    }

    public boolean containsNearbyAlmostDuplicate(int[] nums, int k, int t) {
        if (t < 0) return false;
        Map<Long, Long> d = new HashMap<>();
        long w = (long) t + 1;
        for (int i = 0; i < nums.length; ++i) {
            long m = getID(nums[i], w);
            if (d.containsKey(m))
                return true;
            if (d.containsKey(m - 1) && Math.abs(nums[i] - d.get(m - 1)) < w)
                return true;
            if (d.containsKey(m + 1) && Math.abs(nums[i] - d.get(m + 1)) < w)
                return true;
            d.put(m, (long) nums[i]);
            if (i >= k) d.remove(getID(nums[i - k], w));
        }
        return false;
    }

    public int threeSumClosest(int[] nums, int target) {
        int min = Integer.MAX_VALUE;
        int result = 0;
        //先排序
        Arrays.sort(nums);
        for (int i = 0; i < nums.length; i++) {
            int j = i + 1, k = nums.length - 1;
            while (j < k) {
                int value = nums[i] + nums[j] + nums[k];
                if (Math.abs(value - target) < min) {
                    min = Math.abs(value - target);
                    result = value;
                }
                if (value == target) {
                    return value;
                } else if (value > target) {
                    k--;
                } else {
                    j++;
                }
            }
        }
        return result;
    }

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < nums1.length; i++) {
            list.add(nums1[i]);
        }
        for (int i = 0; i < nums2.length; i++) {
            list.add(nums2[i]);
        }
        list = list.stream().sorted().collect(Collectors.toList());
        if (list.size() % 2 == 0) {
            return new Double(list.get(list.size() / 2 - 1) + list.get(list.size() / 2)) / 2;
        } else {
            return list.get(list.size() / 2);
        }
    }

    public String multiply(String num1, String num2) {
        if (num1.equals("0") || num2.equals("0")) {
            return "0";
        }
        // 保存计算结果
        String res = "0";
        // num2 逐位与 num1 相乘
        for (int i = num2.length() - 1; i >= 0; i--) {
            int carry = 0;
            // 保存 num2 第i位数字与 num1 相乘的结果
            StringBuilder temp = new StringBuilder();
            // 补 0
            for (int j = 0; j < num2.length() - 1 - i; j++) {
                temp.append(0);
            }
            int n2 = num2.charAt(i) - '0';

            // num2 的第 i 位数字 n2 与 num1 相乘
            for (int j = num1.length() - 1; j >= 0 || carry != 0; j--) {
                int n1 = j < 0 ? 0 : num1.charAt(j) - '0';
                int product = (n1 * n2 + carry) % 10;
                temp.append(product);
                carry = (n1 * n2 + carry) / 10;
            }
            // 将当前结果与新计算的结果求和作为新的结果
            res = addStrings(res, temp.reverse().toString());
        }
        return res;
    }

    /**
     * 对两个字符串数字进行相加，返回字符串形式的和
     */
    public String addStrings(String num1, String num2) {
        StringBuilder builder = new StringBuilder();
        int carry = 0;
        for (int i = num1.length() - 1, j = num2.length() - 1;
             i >= 0 || j >= 0 || carry != 0;
             i--, j--) {
            int x = i < 0 ? 0 : num1.charAt(i) - '0';
            int y = j < 0 ? 0 : num2.charAt(j) - '0';
            int sum = (x + y + carry) % 10;
            builder.append(sum);
            carry = (x + y + carry) / 10;
        }
        return builder.reverse().toString();
    }

    public int[] productExceptSelf(int[] nums) {
        int length = nums.length;
        int[] answer = new int[length];
        answer[0] = 1;
        for (int i = 1; i < length; i++) {
            answer[i] = nums[i - 1] * answer[i - 1];
        }
        int R = 1;
        for (int i = length - 1; i >= 0; i--) {
            answer[i] = answer[i] * R;
            R *= nums[i];
        }
        return answer;
    }

    public int[][] generateMatrix(int n) {
        List<Integer> ret = IntStream.range(1, (n * n) + 1).boxed().collect(Collectors.toList());
        int[][] matrix = new int[n][n];
        int b = 0, t = n - 1, l = 0, r = n - 1, k = 0;
        while (k <= ret.size()) {
            for (int j = l; j <= r; j++) matrix[b][j] = ret.get(k++);
            if (++b > t) break;
            for (int i = b; i <= t; i++) matrix[i][r] = ret.get(k++);
            if (--r < l) break;
            for (int j = r; j >= l; j--) matrix[t][j] = ret.get(k++);
            if (--t < b) break;
            for (int i = t; i >= b; i--) matrix[i][l] = ret.get(k++);
            if (++l > r) break;
        }
        return matrix;
    }

    public List<String> restoreIpAddresses(String s) {
        List<String> result = new ArrayList<>();
        StringBuilder ip = new StringBuilder();

        for (int a = 1; a < 4; a++) {
            for (int b = 1; b < 4; b++) {
                for (int c = 1; c < 4; c++) {
                    for (int d = 1; d < 4; d++) {
                        if (a + b + c + d == s.length()) {
                            int seg1 = Integer.parseInt(s.substring(0, a));
                            int seg2 = Integer.parseInt(s.substring(a, a + b));
                            int seg3 = Integer.parseInt(s.substring(a + b, a + b + c));
                            int seg4 = Integer.parseInt(s.substring(a + b + c, a + b + c + d));
                            // 四个段数值满足0~255
                            if (seg1 <= 255 && seg2 <= 255 && seg3 <= 255 && seg4 <= 255) {
                                ip.append(seg1).append(".").append(seg2).append(".").
                                        append(seg3).append(".").append(seg4);
                                // 保障截取的字符串转成int后与输入字符串长度相同
                                if (ip.length() == s.length() + 3) {
                                    result.add(ip.toString());
                                }
                                ip.delete(0, ip.length());
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public boolean validUtf8(int[] data) {
        int num = data[0];
        int temp = 1 << 7;
        int count = 0;
        while (temp > 0) {
            if ((temp & num) == temp) {
                count++;
                temp = temp >> 1;
            } else {
                break;
            }
        }
        if (count == 1 || count > 4) {
            return false;
        }
        temp = 1 << 6;
        for (int i = 1; i < count; i++) {
            int v = data[i];
            if (Integer.highestOneBit(v) != 128 || (temp & v) == temp) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除最外层的括号
     */
    public String removeOuterParentheses(String s) {
        StringBuilder sb = new StringBuilder();
        int level = 0;
        for (char c : s.toCharArray()) {
            if (c == ')') --level;
            if (level >= 1) sb.append(c);
            if (c == '(') ++level;
        }
        return sb.toString();
    }

    public int removePalindromeSub(String s) {
        if (s.length() == 0) {
            return 0;
        }
        char[] chars = s.toCharArray();
        boolean isPalindrome = true;
        for (int i = 0; i < chars.length / 2; i++) {
            if (chars[i] != chars[chars.length - 1 - i]) {
                isPalindrome = false;
                break;
            }
        }
        return isPalindrome ? 1 : 2;
    }

    public int[] kWeakestRows(int[][] mat, int k) {
        Map<Integer, Integer> index = new HashMap<>();
        for (int i = 0; i < mat.length; i++) {
            index.put(i, Arrays.stream(mat[i]).sum());
        }
        return index.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .mapToInt(Map.Entry::getKey)
                .limit(k)
                .toArray();
    }

    public String[] uncommonFromSentences(String A, String B) {
        Map<String, Long> map = Arrays.stream(A.split(" "))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (String s : B.split(" ")) {
            map.put(s, map.getOrDefault(s, 0L) + 1);
        }
        return map.entrySet()
                .stream()
                .filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }

    public int[] getNoZeroIntegers(int n) {
        for (int i = 1; i < n; i++) {
            if (!isContainZero(i) && !isContainZero(n - i)) {
                return new int[]{i, n - i};
            }
        }
        return new int[2];
    }

    private boolean isContainZero(int n) {
        while (n > 0) {
            if (n % 10 == 0) {
                return true;
            }
            n /= 10;
        }
        return false;
    }

    public int[] numSmallerByFrequency(String[] queries, String[] words) {
        int[] ans = new int[queries.length];
        for (int i = 0; i < queries.length; i++) {
            int a = numSmallerByFrequency(queries[i]);
            for (int j = 0; j < words.length; j++) {
                if (a < numSmallerByFrequency(words[j])) {
                    ans[i]++;
                }
            }
        }
        return ans;
    }

    /**
     * 最小字母的出现频次
     */
    private int numSmallerByFrequency(String s) {
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        int count = 1;
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == chars[i - 1]) {
                count++;
            } else {
                return count;
            }
        }
        return count;
    }

    public int compress(char[] chars) {
        if (chars.length <= 1) {
            return chars.length;
        }
        // 从 i = 1 开始遍历, 所以这里对 i = 0 的情况直接进行处理.
        int index = 0;
        int curr = 1;
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == chars[i - 1]) {
                curr++;
            } else {
                if (curr != 1) {
                    char[] array = String.valueOf(curr).toCharArray();
                    for (char c : array) {
                        chars[++index] = c;
                    }
                }
                chars[++index] = chars[i];
                curr = 1;
            }
        }
        if (curr != 1) {
            char[] array = String.valueOf(curr).toCharArray();
            for (char c : array) {
                chars[++index] = c;
            }
        }
        return index + 1;
    }

    public int minMoves(int[] nums) {
        Arrays.sort(nums);
        int count = 0;
        for (int i = nums.length - 1; i > 0; i--) {
            count += nums[i] - nums[0];
        }
        return count;
    }

    public int findContentChildren(int[] g, int[] s) {
        Arrays.sort(g);
        Arrays.sort(s);
        int count = 0;
        for (int i = 0, j = 0; i < s.length; i++) {
            if (j < g.length && s[i] >= g[j]) {
                j++;
                count++;
            }
        }
        return count;
    }

    public boolean repeatedSubstringPattern(String s) {
        int n = s.length();
        //i:重复的个数
        for (int i = 1; i <= n / 2; i++) {
            if (n % i != 0) {
                continue;
            }
            boolean flag = true;
            //循环i个的次数，判断每一个是否隔i个数相等
            a:
            for (int j = 0; j < i; j++) {
                char c = s.charAt(j);
                for (int k = j; k < n; k += i) {
                    if (c != s.charAt(k)) {
                        flag = false;
                        break a;
                    }
                }
            }
            if (flag) {
                return true;
            }
        }
        return false;
    }

    public int findRadius(int[] houses, int[] heaters) {
        //排除两种情况
        Arrays.sort(houses);
        Arrays.sort(heaters);
        if (heaters[0] >= houses[houses.length - 1]) {
            return heaters[0] - houses[0];
        }
        if (heaters[heaters.length - 1] <= houses[0]) {
            return houses[houses.length - 1] - heaters[heaters.length - 1];
        }

        int radius = 0;
        int i = 0;
        int j = 0;
        if (houses[0] < heaters[0]) {
            radius = heaters[0] - houses[0];
        }
        while (houses[i] <= heaters[j]) {
            i++;
        }
        while (j < heaters.length - 1 && i < houses.length) {
            if (houses[i] <= heaters[j + 1]) {
                radius = Math.max(radius, Math.min(houses[i] - heaters[j], heaters[j + 1] - houses[i]));
                i++;
            } else {
                j++;
            }
        }
        if (i < houses.length) {
            radius = Math.max(radius, houses[houses.length - 1] - heaters[heaters.length - 1]);
        }

        return radius;
    }

    public boolean checkPerfectNumber(int num) {
        if (num <= 0) {
            return false;
        }
        int sum = 0;
        for (int i = 1; i * i <= num; i++) {
            if (num % i == 0) {
                sum += i;
                if (i * i != num) {
                    sum += num / i;
                }

            }
        }
        return sum - num == num;
    }

    public boolean validPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;
        return validPalindrome(s, left, right, 1);
    }

    public boolean validPalindrome(String s, int left, int right, int count) {
        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) {
                if (count == 0) {
                    return false;
                } else {
                    count--;
                    return validPalindrome(s, left + 1, right, count) || validPalindrome(s, left, right - 1, count);
                }
            } else {
                left++;
                right--;
            }
        }
        return true;
    }

    /**
     * 是否是回文串
     */
    public boolean isPalindrome(String s) {
        String low = s.toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < low.length(); i++) {
            if (Character.isLetterOrDigit(low.charAt(i))) {
                sb.append(low.charAt(i));
            }
        }
        return sb.toString().equals(sb.reverse().toString());
    }


    public int heightChecker(int[] heights) {
        int count = 0;
        int[] temp = Arrays.stream(heights).sorted().toArray();
        for (int i = 0; i < heights.length; i++) {
            if (heights[i] != temp[i]) {
                count++;
            }
        }
        return count;
    }

    public String[] findWords(String[] words) {
        if (words == null || words.length == 0) return new String[0];
        //用长度为26的数组标识每个字母所在的行号
        int[] map = {2, 3, 3, 2, 1, 2, 2, 2, 1, 2, 2, 2, 3, 3, 1, 1, 1, 1, 2, 1, 1, 3, 1, 3, 1, 3};
        List<String> list = new ArrayList<String>();
        for (String word : words) {
            String tempWord = word.toUpperCase();
            int temp = map[tempWord.charAt(0) - 65];
            boolean flag = true;
            //通过与首字母比较行号确定是否在同一行
            for (int i = 1; i < tempWord.length(); i++) {
                if (temp != map[tempWord.charAt(i) - 65]) {
                    flag = false;
                    break;
                }
            }
            if (flag) list.add(word);
        }
        return list.toArray(new String[list.size()]);
    }

    public boolean judgeCircle(String moves) {
        Map<Integer, Long> map = moves.chars().boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return map.getOrDefault(68, 0L).equals(map.getOrDefault(85, 0L)) && map.getOrDefault(82, 0L).equals(map.getOrDefault(76, 0L));
    }

    public List<Integer> selfDividingNumbers(int left, int right) {
        List<Integer> list = new ArrayList<>();
        for (int i = left; i <= right; i++) {
            if (selfDividingNumbers(i)) {
                list.add(i);
            }
        }
        return list;
    }

    public boolean selfDividingNumbers(int num) {
        int temp = num;
        while (temp > 0) {
            if ((temp % 10) == 0 || num % (temp % 10) != 0) {
                return false;
            }
            temp /= 10;
        }
        return true;
    }

    public int[] diStringMatch(String S) {
        int N = S.length();
        int lo = 0, hi = N;
        int[] ans = new int[N + 1];
        for (int i = 0; i < N; ++i) {
            if (S.charAt(i) == 'I')
                ans[i] = lo++;
            else
                ans[i] = hi--;
        }

        ans[N] = lo;
        return ans;
    }

    public int smallestRangeI(int[] A, int K) {
        int min = Arrays.stream(A).min().orElse(0);
        int max = Arrays.stream(A).max().orElse(0);
        return Math.max(max - min - 2 * K, 0);
    }

    public List<String> commonChars(String[] A) {
        int[] count = new int[26];
        Arrays.fill(count, Integer.MAX_VALUE);

        for (String str : A) {
            int[] temp = new int[26];
            for (int i = 0; i < 26; i++) temp[i] = 0;

            for (char c : str.toCharArray()) temp[c - 'a']++;

            for (int i = 0; i < 26; i++) count[i] = Math.min(count[i], temp[i]);
        }

        List<String> ans = new ArrayList<String>();
        for (int i = 0; i < 26; i++) {
            while (count[i] > 0) {
                ans.add("" + (char) ('a' + i));
                count[i]--;
            }
        }
        return ans;
    }

    public int[] exchange(int[] nums) {
        return Arrays.stream(nums).boxed().sorted(Comparator.<Integer, Integer>comparing(a -> a % 2).reversed()).mapToInt(Integer::intValue).toArray();
    }

    public boolean hasAlternatingBits(int n) {
        int a = -1;
        while (n > 0) {
            int temp = n & 1;
            if (temp == a) {
                return false;
            }
            a = temp;
            n = n >> 1;
        }
        return true;
    }

    public int distributeCandies(int[] candies) {
        Map<Integer, Long> map = Arrays.stream(candies)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return Math.min(map.size(), candies.length / 2);
    }

    public int maxNumberOfBalloons(String text) {
        Map<Integer, Long> map = text.chars()
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return (int) Math.min(map.getOrDefault(97, 0L),
                Math.min(map.getOrDefault(98, 0L),
                        Math.min(map.getOrDefault(108, 0L) / 2,
                                Math.min(map.getOrDefault(110, 0L),
                                        map.getOrDefault(111, 0L) / 2))));
    }

    class Employee {
        public int id;
        public int importance;
        public List<Integer> subordinates;
    }

    public int getImportance(List<Employee> employees, int id) {
        Map<Integer, Employee> map = employees.stream()
                .collect(Collectors.toMap(a -> a.id, Function.identity()));
        return getImportance(map, id);
    }

    public int getImportance(Map<Integer, Employee> map, int id) {
        int sum = 0;
        Employee employee = map.get(id);
        if (employee != null) {
            sum += employee.importance;
            for (Integer subordinate : employee.subordinates) {
                Employee a = map.get(subordinate);
                if (a.subordinates.size() > 0) {
                    sum += getImportance(map, a.id);
                } else {
                    sum += a.importance;
                }
            }
        }
        return sum;
    }

    public int[] sumEvenAfterQueries(int[] A, int[][] queries) {
        int[] answer = new int[A.length];
        int S = 0;
        for (int x : A)
            if (x % 2 == 0)
                S += x;
        for (int i = 0; i < queries.length; i++) {
            int val = queries[i][0], index = queries[i][1];
            if (A[index] % 2 == 0) S -= A[index];
            A[index] += val;
            if (A[index] % 2 == 0) S += A[index];
            answer[i] = S;
        }
        return answer;
    }

    public String minWindow(String s, String t) {
        String result = "";
        List<Integer> index = new LinkedList<>();
        Map<Integer, Long> nums = t.chars()
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.counting()));
        Map<Integer, Long> map = new HashMap<>();
        //窗口滑动
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            //判断元素是否在数组中
            if (nums.containsKey((int) c)) {
                index.add(i);
                map.put((int) c, map.getOrDefault((int) c, 0L) + 1);
                //判断数组元素是否相同
                //记录
                if (minWindowContain(map, nums)) {
                    if ("".equals(result)) {
                        result = s.substring(index.get(0), i + 1);
                    }
                    Iterator<Integer> it = index.iterator();
                    while (it.hasNext()) {
                        Integer k = it.next();
                        //缩小
                        map.put((int) s.charAt(k), map.getOrDefault((int) s.charAt(k), 0L) - 1);
                        if (minWindowContain(map, nums)) {
                            it.remove();
                        } else {
                            if (i - k < result.length()) {
                                result = s.substring(k, i + 1);
                            }
                            map.put((int) s.charAt(k), map.getOrDefault((int) s.charAt(k), 0L) + 1);
                            break;
                        }
                    }
                }

            }
        }
        return result;
    }

    public boolean minWindowContain(Map<Integer, Long> map, Map<Integer, Long> nums) {
        for (Map.Entry<Integer, Long> entry : nums.entrySet()) {
            if (map.getOrDefault(entry.getKey(), 0L) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public int minCostToMoveChips(int[] chips) {
        //偶数
        int[] b = Arrays.stream(chips).filter(a -> a % 2 == 0).toArray();
        //奇数
        int[] c = Arrays.stream(chips).filter(a -> a % 2 == 1).toArray();
        return Math.min(b.length, c.length);
    }

    public int numSpecialEquivGroups(String[] A) {
        Set<String> seen = new HashSet<>();
        for (String S : A) {
            int[] count = new int[52];
            for (int i = 0; i < S.length(); ++i)
                count[S.charAt(i) - 'a' + 26 * (i % 2)]++;
            seen.add(Arrays.toString(count));
        }
        return seen.size();
    }

    public String mostCommonWord(String paragraph, String[] banned) {
        //数组转Set
        Set<String> set = new HashSet<>(Arrays.asList(banned));
        set.add("");
        paragraph += ".";
        //结果单词
        String res = " ";
        //最大单词出现次数
        int times = 0;
        //记录单词出现次数的map
        Map<String, Integer> map = new HashMap<>();
        //i,k为双指针
        int i = 0;
        for (int k = 0; k < paragraph.length(); k++) {
            char c = paragraph.charAt(k);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                continue;
            } else {
                //利用双指针截取出一个单词
                String word = paragraph.substring(i, k);
                //统一将单词转为小写
                word = word.toLowerCase();
                if (!set.contains(word)) {
                    map.put(word, map.getOrDefault(word, 0) + 1);
                    //最大值的获取
                    if (map.get(word) > times) {
                        res = word;
                        times = map.get(word);
                    }
                }
                i = k + 1;
            }
        }
        return res;
    }

    public List<String> letterCasePermutation(String s) {
        return letterCasePermutation(s, 0);
    }

    public List<String> letterCasePermutation(String s, int index) {
        char[] chars = s.toCharArray();
        List<String> list = new LinkedList<>();
        list.add(s);
        for (int i = index; i < s.length(); i++) {
            if (chars[i] >= 'a' && chars[i] <= 'z') {
                char[] c = Arrays.copyOfRange(chars, 0, chars.length);
                c[i] -= 32;
                String s1 = new String(c);
                list.addAll(letterCasePermutation(s1, i + 1));
            } else if (chars[i] >= 'A' && chars[i] <= 'Z') {
                char[] c = Arrays.copyOfRange(chars, 0, chars.length);
                c[i] += 32;
                String s1 = new String(c);
                list.addAll(letterCasePermutation(s1, i + 1));
            }
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    public int lastStoneWeight(int[] stones) {
        int index = stones.length - 1;
        for (int i = 0; i < stones.length - 1; i++) {     //通过stones.length来判断需要操作的次数。（不用将stones.length == 1的情况单独考虑）
            Arrays.sort(stones);                        //将sort放在循环体的开始。（避免在循环体外再写一次重复的sort（））
            stones[index] -= stones[index - 1];           //两种不同情况使用同一表达式处理。（）
            stones[index - 1] = 0;
        }
        return stones[stones.length - 1];
    }

    public boolean checkRecord(String s) {
        Map<Character, Integer> count = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            count.put(s.charAt(i), count.getOrDefault(s.charAt(i), 0) + 1);
        }
        if (count.getOrDefault('A', 0) <= 1) {
            for (int i = 2; i < s.length(); i++) {
                //连续两个L以上
                if (s.charAt(i) == s.charAt(i - 1)
                        && s.charAt(i - 1) == s.charAt(i - 2)
                        && s.charAt(i) == 'L') {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String dayOfTheWeek(int day, int month, int year) {
        String[] weeks = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int value = LocalDate.of(year, month, day)
                .getDayOfWeek()
                .getValue();
        return weeks[value];
    }

    public List<String> subdomainVisits(String[] cpdomains) {
        Map<String, Integer> count = new HashMap<>();
        for (int i = 0; i < cpdomains.length; i++) {
            String[] s = cpdomains[i].split(" ");
            Integer c = Integer.parseInt(s[0]);
            String web = s[1];
            while (web.contains(".")) {
                count.put(web, count.getOrDefault(web, 0) + c);
                web = web.substring(web.indexOf(".") + 1);
            }
            count.put(web, count.getOrDefault(web, 0) + c);
        }
        return count.entrySet().stream().map(entry -> entry.getValue() + " " + entry.getKey()).collect(Collectors.toList());
    }

    public int subarraysDivByK(int[] A, int K) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 1);
        //每一步和
        int sum = 0, res = 0;
        for (int value : A) {
            sum += value;
            int modulus = (sum % K + K) % K;
            res += map.getOrDefault(modulus, 0);
            map.put(modulus, map.getOrDefault(modulus, 0) + 1);
        }
        return res;
    }

    public int[] sortArrayByParityII(int[] A) {
        int j = 1;
        for (int i = 0; i < A.length; i += 2) {
            if (A[i] % 2 == 1) {
                while (A[j] % 2 == 1) {
                    j += 2;
                }
                int tmp = A[i];
                A[i] = A[j];
                A[j] = tmp;
            }
        }
        return A;
    }

    public int largestRectangleArea(int[] heights) {
        int res = 0;
        for (int i = 0; i < heights.length; i++) {
            int min = Integer.MAX_VALUE;
            //从左向右
            for (int j = i; j < heights.length; j++) {
                //从左向右
                //最小值
                min = Math.min(min, heights[j]);
                //宽*高
                res = Math.max((j - i + 1) * min, res);
            }
        }
        return res;
    }

    public boolean canBeEqual(int[] target, int[] arr) {
        Arrays.sort(target);
        Arrays.sort(arr);
        for (int i = 0; i < target.length; i++) {
            if (target[i] != arr[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean hasAllCodes(String s, int k) {
        //生成2进制子串
        Set<String> set = new HashSet<>();
        for (int i = k; i <= s.length(); i++) {
            set.add(s.substring(i - k, i));
        }
        return set.size() == Math.pow(2, k);
    }

    public List<Boolean> checkIfPrerequisite(int n, int[][] prerequisites, int[][] queries) {
        List<Boolean> res = new ArrayList<>();
        boolean[][] pre = new boolean[n][n];
        boolean[] resolved = new boolean[n];
        for (int i = 0; i < prerequisites.length; i++) {
            pre[prerequisites[i][1]][prerequisites[i][0]] = true;
        }
        for (int i = 0; i < n; i++) {
            find(pre, resolved, i);
        }

        for (int i = 0; i < queries.length; i++) {
            res.add(pre[queries[i][1]][queries[i][0]]);
        }
        return res;
    }

    private void find(boolean[][] pre, boolean[] resolved, int i) {
        if (resolved[i]) {
            return;
        }
        for (int k = 0; k < resolved.length; k++) {
            if (pre[i][k]) {
                find(pre, resolved, k);
                for (int j = 0; j < resolved.length; j++) {
                    pre[i][j] = pre[i][j] || pre[k][j];
                }
            }
        }
        resolved[i] = true;
    }

    public int cherryPickup(int[][] grid) {
        int res = 0;
        int n = grid.length;
        int m = grid[0].length;
        int[][][] dp = new int[n][m][m];
        dp[0][0][m - 1] = grid[0][0] + grid[0][m - 1];
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < Math.min(i + 1, m); j++) {
                for (int k = m - 1; k >= Math.max(m - 1 - i, j); k--) {
                    dp[i][j][k] = Math.max(j > 0 ? dp[i - 1][j - 1][k - 1] : 0,
                            Math.max(j > 0 ? dp[i - 1][j - 1][k] : 0,
                                    Math.max(k < m - 1 && j > 0 ? dp[i - 1][j - 1][k + 1] : 0,
                                            Math.max(k > 0 && j < k ? dp[i - 1][j][k - 1] : 0,
                                                    Math.max(dp[i - 1][j][k],
                                                            Math.max(k < m - 1 ? dp[i - 1][j][k + 1] : 0,

                                                                    Math.max(k > 0 && j < m - 1 && j + 1 <= k - 1 ? dp[i - 1][j + 1][k - 1] : 0,
                                                                            Math.max(j < m - 1 && j < k ? dp[i - 1][j + 1][k] : 0,
                                                                                    j < m - 1 && k < m - 1 ? dp[i - 1][j + 1][k + 1] : 0))))))));
                    int value = j == k ? grid[i][j] : grid[i][j] + grid[i][k];
                    dp[i][j][k] += value;
                    res = Math.max(dp[i][j][k], res);
                }
            }
        }
        return res;
    }


    public int maxProduct(int[] nums) {
        int res = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                res = Math.max(res, (nums[i] - 1) * (nums[j] - 1));
            }
        }
        return res;
    }

    public int maxArea(int h, int w, int[] horizontalCuts, int[] verticalCuts) {
        Arrays.sort(horizontalCuts);
        Arrays.sort(verticalCuts);
        int hight = 0, wed = 0;
        //先竖切，记录高
        for (int i = 0; i <= horizontalCuts.length; i++) {
            hight = Math.max(hight, (i == horizontalCuts.length ? h : horizontalCuts[i]) - (i > 0 ? horizontalCuts[i - 1] : 0));
        }
        for (int j = 0; j <= verticalCuts.length; j++) {
            wed = Math.max(wed, (j == verticalCuts.length ? w : verticalCuts[j]) - (j > 0 ? verticalCuts[j - 1] : 0));
        }
        return (int) ((long) ((hight * wed) % (Math.pow(10, 9) + 7)));
    }

    public int minReorder(int n, int[][] connections) {
        Map<Integer, List<Integer>> map1 = Arrays.stream(connections).collect(Collectors.groupingBy(a -> a[0],
                Collectors.mapping(a -> a[1], Collectors.toList())));
        Map<Integer, List<Integer>> map2 = Arrays.stream(connections).collect(Collectors.groupingBy(a -> a[1],
                Collectors.mapping(a -> a[0], Collectors.toList())));
        //寻找能到达的城市
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        //set里包含了所以可以达的
        Set<Integer> set = new HashSet<>();
        set.add(0);
        int count = 0;
        while (!queue.isEmpty()) {
            Integer value = queue.poll();
            //要反的
            List<Integer> list1 = map1.getOrDefault(value, new ArrayList<>());
            for (Integer a : list1) {
                if (!set.contains(a)) {
                    set.add(a);
                    queue.add(a);
                    count++;
                }
            }

            List<Integer> list2 = map2.getOrDefault(value, new ArrayList<>());
            for (Integer a : list2) {
                if (!set.contains(a)) {
                    set.add(a);
                    queue.add(a);
                }
            }
        }
        return count;
    }


    public double getProbability(int[] balls) {
        //总数
        int k = balls.length;
        int n = Arrays.stream(balls).sum() / 2;
        double[] fact = new double[2 * n + 1];
        fact[0] = 1;
        //求阶乘
        for (int i = 1; i <= 2 * n; i++) {
            fact[i] = fact[i - 1] * i;
        }
        // 总的排列方法数      (2n的阶乘 除以 重复个数的阶乘)
        double total = fact[2 * n];
        for (int ball : balls) {
            total /= fact[ball];
        }

        int[][] dp = new int[2 * n + 1][2 * k + 1];
        dp[0][k] = 1;
        int num = 0;

        for (int i = 0; i < k; i++) {
            int[][] next = new int[2 * n + 1][2 * k + 1];
            for (int j = 0; j < balls[i]; j++) {
                int trans = 0;
                trans = j == 0 ? -1 : trans;
                trans = j == balls[i] ? 1 : trans;
                for (int front = 0; front <= 2 * n; front++) {
                    for (int color = 0; color <= 2 * n; color++) {
                        if (dp[front][color] == 0) continue;
                        double ways = dp[front][color];
                        ways *= fact[front + j] / (fact[front] * fact[j]);
                        ways *= fact[num - front + balls[i] - j] / (fact[num - front] * fact[balls[i] - j]);
                        next[front + j][color + trans] += ways;
                    }
                }
            }
            dp = next;
            num += balls[i];
        }
        return dp[n][k] / total;
    }

    public int findJudge(int n, int[][] trust) {
        int[] dp1 = new int[n + 1];
        int[] dp2 = new int[n + 1];
        for (int[] ints : trust) {
            dp1[ints[0]]++;
            dp2[ints[1]]++;
        }
        for (int i = 1; i < dp2.length; i++) {
            if (dp2[i] == n - 1 && dp1[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    public int sumNums(int n) {
        if (n == 0) {
            return 0;
        }
        return n + sumNums(n - 1);
    }

    public int minDeletionSize(String[] a) {
        int count = 0;
        char[][] chars = new char[a.length][];
        for (int i = 0; i < a.length; i++) {
            chars[i] = a[i].toCharArray();
        }
        for (int i = 0; i < chars[0].length; i++) {
            for (int j = 1; j < chars.length; j++) {
                if (chars[j][i] <= chars[j - 1][i]) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    public int findLUSlength(String a, String b) {
        if (a.equals(b))
            return -1;
        return Math.max(a.length(), b.length());
    }

    public int[][] transpose(int[][] A) {
        int R = A.length, C = A[0].length;
        int[][] ans = new int[C][R];
        for (int r = 0; r < R; ++r)
            for (int c = 0; c < C; ++c) {
                ans[c][r] = A[r][c];
            }
        return ans;
    }

    public int countPrimeSetBits(int L, int R) {
        int count = 0;
        for (int i = L; i <= R; i++) {
            int num = Integer.bitCount(i);
            if (num == 2 || num == 3 ||
                    num == 5 || num == 7 ||
                    num == 11 || num == 13 ||
                    num == 17 || num == 19) {
                count++;
            }
        }
        return count;
    }

    public String removeDuplicates(String s) {
        StringBuilder sb = new StringBuilder();
        int sbLength = 0;
        for (char character : s.toCharArray()) {
            if (sbLength != 0 && character == sb.charAt(sbLength - 1))
                sb.deleteCharAt(sbLength-- - 1);
            else {
                sb.append(character);
                sbLength++;
            }
        }
        return sb.toString();
    }

    public int calPoints(String[] ops) {
        Stack<Integer> stack = new Stack();

        for (String op : ops) {
            if (op.equals("+")) {
                int top = stack.pop();
                int newtop = top + stack.peek();
                stack.push(top);
                stack.push(newtop);
            } else if (op.equals("C")) {
                stack.pop();
            } else if (op.equals("D")) {
                stack.push(2 * stack.peek());
            } else {
                stack.push(Integer.valueOf(op));
            }
        }

        int ans = 0;
        for (int score : stack) ans += score;
        return ans;
    }

    public boolean CheckPermutation(String s1, String s2) {
        if (s1.length() != s2.length()) {
            return false;
        }
        char[] chars1 = s1.toCharArray();
        char[] chars2 = s2.toCharArray();
        Arrays.sort(chars1);
        Arrays.sort(chars2);
        for (int i = 0; i < chars1.length; i++) {
            if (chars1[i] != chars2[i]) {
                return false;
            }
        }
        return true;
    }

    public int[] sortByBits(int[] arr) {
        return Arrays.stream(arr)
                .boxed()
                .sorted(Comparator.comparing(Integer::bitCount)
                        .thenComparing(Comparator.naturalOrder()))
                .mapToInt(Integer::intValue)
                .toArray();
    }

    public List<List<Integer>> minimumAbsDifference(int[] arr) {
        Arrays.sort(arr);
        Map<Integer, List<List<Integer>>> map = new HashMap<>();
        int min = Integer.MAX_VALUE;
        for (int i = 1; i < arr.length; i++) {
            min = Math.min(arr[i] - arr[i - 1], min);
            List<List<Integer>> list = map.getOrDefault(arr[i] - arr[i - 1], new ArrayList<>());
            List<Integer> a = new ArrayList<>();
            a.add(arr[i - 1]);
            a.add(arr[i]);
            list.add(a);
            map.put(arr[i] - arr[i - 1], list);
        }
        return map.get(min);
    }

    public int[] relativeSortArray(int[] arr1, int[] arr2) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < arr2.length; i++) {
            map.put(arr2[i], i);
        }
        return Arrays.stream(arr1)
                .boxed()
                .sorted(Comparator.<Integer, Integer>comparing(i -> map.getOrDefault(i, arr2.length)).thenComparing(Comparator.naturalOrder()))
                .mapToInt(Integer::intValue)
                .toArray();
    }

    public int projectionArea(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        int ans = 0;
        for (int i = 0; i < m; i++) {
            int bestRow = 0, bestCol = 0;
            for (int j = 0; j < n; j++) {
                if (grid[i][j] > 0) ans++;  // top shadow
                bestRow = Math.max(bestRow, grid[i][j]);
                bestCol = Math.max(bestCol, grid[j][i]);
            }
            ans += bestRow;
            ans += bestCol;
        }
        return ans;
    }

    public int[] constructArr(int[] a) {
        int[] b = new int[a.length];
        b[0] = 1;
        for (int i = 1; i < a.length; i++) {
            b[i] = b[i - 1] * a[i - 1];
        }
        int c = 1;
        for (int i = a.length - 1; i >= 0; i--) {
            b[i] = b[i] * c;
            c *= a[i];
        }
        return b;
    }

    public boolean isToeplitzMatrix(int[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (i > 0 && j > 0 && matrix[i - 1][j - 1] != matrix[i][j])
                    return false;
            }
        }
        return true;
    }

    /**
     * 回溯法
     */
    public boolean wordBreak(String s, List<String> wordDict) {
        Set<String> wordDictSet = new HashSet(wordDict);
        boolean[] dp = new boolean[s.length() + 1];
        dp[0] = true;
        for (int i = 1; i <= s.length(); i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] && wordDictSet.contains(s.substring(j, i))) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[s.length()];
    }

    /**
     * 回溯
     */
    public int respace(String[] dictionary, String sentence) {
        int n = sentence.length();
        int m = dictionary.length;
        int[] dp = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            dp[i] = dp[i - 1];
            for (int j = 0; j < m; j++) {
                if (i < dictionary[j].length()) continue;
                if (sentence.substring(i - dictionary[j].length(), i).equals(dictionary[j])) {
                    dp[i] = Math.max(dp[i - dictionary[j].length()] + dictionary[j].length(), dp[i]);
                }
            }
        }
        return n - dp[n];
    }

    public int minOperations(String[] logs) {
        int result = 0;
        for (String log : logs) {
            if (log.equals("../") && result > 0) {
                result--;
            } else if (!log.startsWith(".")) {
                result++;
            }
        }
        return result;
    }

    public int minOperationsMaxProfit(int[] customers, int boardingCost, int runningCost) {
        int rs = 0;//次数
        int num = 0;//剩余数
        int count = 0;//人数
        for (int customer : customers) {
            if (customer + num > 0) {
                int result = (customer + num) / 4; //次数
                num = (customer + num) % 4;
                rs += result;
                count += customer;
            }
        }

        rs = num > 0 && (num * boardingCost > runningCost) ? rs + 1 : rs;
        if (rs * runningCost > count * boardingCost) {
            return -1;
        }
        return rs;
    }

    class ThroneInheritance {

        private People king;

        private Map<String, People> map = new HashMap<>();

        class People {

            private String name;

            private boolean dead;

            private List<People> childs = new ArrayList<>();

            public People(String name) {
                this.name = name;
            }
        }

        public ThroneInheritance(String kingName) {
            this.king = new People(kingName);
            map.put(kingName, this.king);
        }

        public void birth(String parentName, String childName) {
            People people = map.get(parentName);
            People child = new People(childName);
            people.childs.add(child);
            map.put(childName, child);
        }

        public void death(String name) {
            People people = map.get(name);
            people.dead = true;
        }

        public List<String> getInheritanceOrder() {
            List<String> list = new ArrayList<>();
            People root = this.king;
            Stack<People> stack = new Stack<>();
            stack.push(root);
            while (!stack.isEmpty()) {
                People pop = stack.pop();
                if (!pop.dead) {
                    list.add(pop.name);
                }
                for (int i = pop.childs.size() - 1; i > 0; i--) {
                    stack.push(pop.childs.get(i));
                }
            }
            return list;
        }
    }


    public static void main(String[] args) {
        System.out.println(new StringTool().minOperationsMaxProfit(new int[]{10, 10, 1, 0, 0}, 4, 4));
    }
}
