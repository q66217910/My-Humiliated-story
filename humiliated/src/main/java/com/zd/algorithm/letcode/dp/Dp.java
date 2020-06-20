package com.zd.algorithm.letcode.dp;

import org.checkerframework.checker.units.qual.A;
import sun.misc.Unsafe;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Dp {

    /**
     * 不同路径
     */
    public int uniquePaths(int m, int n) {
        int[][] dp = new int[m][n];
        //第一行和第一列都只有一种路径
        for (int i = 0; i < m; i++) dp[i][0] = 1;
        for (int i = 0; i < n; i++) dp[0][i] = 1;
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
            }
        }
        return dp[m - 1][n - 1];
    }

    public int uniquePathsWithObstacles(int[][] obstacleGrid) {
        if (obstacleGrid[0][0] == 1) {
            return 0;
        }
        //初始行数量
        obstacleGrid[0][0] = 1;
        //第一行和第一列都只有一种路径,如果前一个有障碍,后面都会0
        for (int i = 1; i < obstacleGrid.length; i++)
            obstacleGrid[i][0] = (obstacleGrid[i][0] == 0 && obstacleGrid[i - 1][0] == 1) ? 1 : 0;
        for (int j = 1; j < obstacleGrid[0].length; j++)
            obstacleGrid[0][j] = (obstacleGrid[0][j] == 0 && obstacleGrid[0][j - 1] == 1) ? 1 : 0;

        for (int k = 1; k < obstacleGrid.length; k++) {
            for (int l = 1; l < obstacleGrid[0].length; l++) {
                if (obstacleGrid[k][l] == 0) {
                    obstacleGrid[k][l] = obstacleGrid[k - 1][l] + obstacleGrid[k][l - 1];
                } else {
                    obstacleGrid[k][l] = 0;
                }
            }
        }
        return obstacleGrid[obstacleGrid.length - 1][obstacleGrid[0].length - 1];
    }

    /**
     * 最小路径和
     */
    public int minPathSum(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        for (int i = 1; i < m; i++) grid[i][0] = grid[i - 1][0] + grid[i][0];
        for (int i = 1; i < n; i++) grid[0][i] = grid[0][i - 1] + grid[0][i];
        for (int k = 1; k < m; k++) {
            for (int l = 1; l < n; l++) {
                grid[k][l] = Math.min(grid[k - 1][l] + grid[k][l], grid[k][l - 1] + grid[k][l]);
            }
        }
        return grid[m - 1][n - 1];
    }

    /**
     * 编辑距离
     */
    public int minDistance(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        //有一个为0，只需要增删
        if (m * n == 0) {
            return n + m;
        }
        //初始化dp
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) dp[i][0] = i;
        for (int i = 0; i < m + 1; i++) dp[0][i] = i;
        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                if (word1.charAt(j - 1) == word2.charAt(i - 1)) {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j - 1] - 1, dp[i - 1][j]), dp[i][j - 1]);
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j - 1], dp[i - 1][j]), dp[i][j - 1]);
                }
            }
        }
        return dp[n][m];
    }

    /**
     * 最大矩阵
     */
    public int maximalRectangle(char[][] matrix) {
        if (matrix.length == 0) return 0;
        int m = matrix.length;
        int n = matrix[0].length;

        //初始化边界dp
        int[] left = new int[n];
        int[] right = new int[n];
        int[] height = new int[n];
        //右边界默认最大值
        Arrays.fill(right, n);
        int maxArea = 0;
        for (char[] chars : matrix) {
            int curLeft = 0, curRight = n;
            for (int j = 0; j < n; j++) {
                //算每个i行j的高度
                if (chars[j] == '1') height[j]++;
                else height[j] = 0;
            }
            for (int j = 0; j < n; j++) {
                //左边界取 当前和历史左边界最大值
                if (chars[j] == '1') left[j] = Math.max(left[j], curLeft);
                else {
                    left[j] = 0;
                    curLeft = j + 1;
                }
            }
            for (int j = n - 1; j >= 0; j--) {
                if (chars[j] == '1') right[j] = Math.min(right[j], curRight);
                else {
                    right[j] = n;
                    curRight = j;
                }
            }
            for (int j = 0; j < n; j++) {
                maxArea = Math.max(maxArea, (right[j] - left[j]) * height[j]);
            }
        }
        return maxArea;
    }

    /**
     * 解码方法
     */
    public int numDecodings(String s) {
        char[] arr = s.toCharArray();
        int[] dp = new int[s.length() + 1];
        dp[0] = 1;
        dp[1] = arr[0] == '0' ? 0 : 1;
        if (s.length() <= 1) return dp[1];
        for (int i = 2; i <= s.length(); i++) {
            int n = (arr[i - 2] - '0') * 10 + (arr[i - 1] - '0');
            if (arr[i - 1] == '0' && arr[i - 2] == '0') {
                return 0;
            } else if (arr[i - 2] == '0') {
                dp[i] = dp[i - 1];
            } else if (arr[i - 1] == '0') {
                if (n > 26) return 0;
                dp[i] = dp[i - 2];
            } else if (n > 26) {
                dp[i] = dp[i - 1];
            } else {
                dp[i] = dp[i - 1] + dp[i - 2];
            }
        }
        return dp[dp.length - 1];
    }

    /**
     * 使用最小花费爬楼梯
     */
    public int minCostClimbingStairs(int[] cost) {
        int size = cost.length;
        int[] minCost = new int[size];
        minCost[0] = 0;
        minCost[1] = Math.min(cost[0], cost[1]);
        for (int i = 2; i < size; i++) {
            minCost[i] = Math.min(minCost[i - 1] + cost[i], minCost[i - 2] + cost[i - 1]);
        }
        return minCost[size - 1];
    }

    public int rob(int[] nums) {
        if (nums.length == 1) {
            return nums[0];
        }
        int[] a = new int[nums.length - 1];
        int[] b = new int[nums.length - 1];
        System.arraycopy(nums, 1, a, 0, nums.length - 1);
        System.arraycopy(nums, 0, b, 0, nums.length - 1);
        return Math.max(robSub(a), robSub(b));
    }

    public int robSub(int[] nums) {
        int pre = 0, cur = 0, tmp;
        for (int i = 0; i < nums.length; i++) {
            tmp = cur;
            cur = Math.max(pre + nums[i], cur);
            pre = tmp;
        }
        return cur;
    }

    public int[][] updateMatrix(int[][] matrix) {
        int n = matrix.length;
        int m = matrix[0].length;
        int[][] result = new int[n][m];
        for (int[] ints : result) {
            Arrays.fill(ints, 99);
        }
        //元素为0的距离一定为0
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                if (matrix[i][j] == 0) {
                    result[i][j] = 0;
                }
            }
        }
        //向左，然后向上
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                if (i - 1 >= 0) {
                    result[i][j] = Math.min(result[i][j], result[i - 1][j] + 1);
                }
                if (j - 1 >= 0) {
                    result[i][j] = Math.min(result[i][j], result[i][j - 1] + 1);
                }
            }
        }
        //向右，然后向下
        for (int i = n - 1; i >= 0; --i) {
            for (int j = m - 1; j >= 0; --j) {
                if (i + 1 < n) {
                    result[i][j] = Math.min(result[i][j], result[i + 1][j] + 1);
                }
                if (j + 1 < m) {
                    result[i][j] = Math.min(result[i][j], result[i][j + 1] + 1);
                }
            }
        }
        return result;
    }

    public boolean canJump(int[] nums) {
        boolean[] dp = new boolean[nums.length];
        dp[0] = true;
        for (int i = 0; i < nums.length; i++) {
            if (dp[i]) {
                for (int j = 0; j <= nums[i]; j++) {
                    if (i + j < nums.length) dp[i + j] = true;
                }
            }
        }
        return dp[nums.length - 1];
    }

    public int maxArea(int[] height) {
        int n = height.length;
        int[] dp = new int[n];
        dp[0] = 0;
        int result = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int temp = Math.min(height[i], height[j]) * (j - i);
                dp[i] = Math.max(temp, dp[i]);
            }
            result = Math.max(result, dp[i]);
        }
        return result;
    }

    public int arrangeCoins(int n) {
        int num = 1;
        while (n >= 0) {
            n -= num++;
        }
        return num - 2;
    }

    public int numberOfBoomerangs(int[][] points) {
        int ans = 0;
        for (int i = 0; i < points.length; i++) {
            Map<Integer, Integer> map = new HashMap<>();
            for (int j = 0; j < points.length; j++) {
                if (i != j) {
                    int dis = dis(points[i], points[j]);
                    map.put(dis, map.getOrDefault(dis, 0) + 1);
                }
            }
            // 排列组合，例如：三个数选两个并且可以改变顺序，也就是A32 = 3 * 2
            for (int dis : map.keySet()) {
                ans += (map.get(dis)) * (map.get(dis) - 1);
            }
        }
        return ans;
    }

    // 计算距离（平方和）
    private int dis(int[] a, int[] b) {
        return (a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]);
    }

    public List<Integer> findDisappearedNumbers(int[] nums) {
        List<Integer> list = new ArrayList<>();
        Arrays.sort(nums);
        int j = 1;
        for (int i = 0; i < nums.length; i++, j++) {
            if (j != nums[i]) {
                list.add(j);
                i--;
            }
        }
        return list;
    }

    public int[] sumZero(int n) {
        int[] result = new int[n];
        for (int i = 0; i < n / 2; i++) {
            int j = i + 1;
            result[i] = j;
            result[n - i - 1] = -j;
        }
        if (n % 2 == 1) {
            result[n / 2] = 0;
        }
        return result;
    }

    public int[] sortArrayByParity(int[] A) {
        return Arrays.stream(A).boxed().sorted(Comparator.comparing(a -> a % 2 == 1)).mapToInt(Integer::intValue).toArray();
    }

    public int maximum(int a, int b) {
        // 先考虑没有溢出时的情况，计算 b - a 的最高位，依照题目所给提示 k = 1 时 a > b，即 b - a 为负
        int k = b - a >>> 31;
        // 再考虑 a b 异号的情况，此时无脑选是正号的数字
        int aSign = a >>> 31, bSign = b >>> 31;
        // diff = 0 时同号，diff = 1 时异号
        int diff = aSign ^ bSign;
        // 在异号，即 diff = 1 时，使之前算出的 k 无效，只考虑两个数字的正负关系
        k = k & (diff ^ 1) | bSign & diff;
        return a * k + b * (k ^ 1);
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

    public boolean isInterleave(String s1, String s2, String s3) {
        if (s1.length() + s2.length() != s3.length()) {
            return false;
        }
        boolean[][] dp = new boolean[s1.length() + 1][s2.length() + 1];
        dp[0][0] = true;
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (j > 0 && i == 0) {
                    dp[i][j] = dp[i][j - 1] && s2.charAt(j - 1) == s3.charAt(i + j - 1);
                } else if (j == 0 && i > 0) {
                    dp[i][j] = dp[i - 1][j] && s1.charAt(i - 1) == s3.charAt(i + j - 1);
                } else if (i > 0 && j > 0) {
                    dp[i][j] = (dp[i - 1][j] && s1.charAt(i - 1) == s3.charAt(i + j - 1))
                            || (dp[i][j - 1] && s2.charAt(j - 1) == s3.charAt(i + j - 1));
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    public int maxProfit(int[] prices) {
        int[] dp = new int[prices.length];
        return dp[prices.length - 1];
    }

    /**
     * 归并排序
     */
    public int reversePairs(int[] nums) {
        int n = nums.length;
        //临时排序数组
        int[] dp = new int[n];
        return mergeSort(nums, dp, 0, n - 1);
    }

    private int mergeSort(int[] nums, int[] dp, int l, int r) {
        if (l >= r) {
            return 0;
        }
        int mid = (l + r) / 2;
        //归并排序,将数组一分为2个
        int count = mergeSort(nums, dp, l, mid) + mergeSort(nums, dp, mid + 1, r);
        int i = l, j = mid + 1, pos = l;
        //两个数组往前比较
        while (i <= mid && j <= r) {
            //前与后比
            if (nums[i] <= nums[j]) {
                //第一位为小的
                dp[pos] = nums[i];
                i++;
                count += (j - (mid + 1));
            } else {
                //否则后面小
                dp[pos] = nums[j];
                j++;
            }
            pos++;
        }
        for (int k = i; k <= mid; k++) {
            dp[pos++] = nums[k];
            count += (j - (mid + 1));
        }
        for (int k = j; k <= r; ++k) {
            dp[pos++] = nums[k];
        }
        System.arraycopy(dp, l, nums, l, r + 1 - l);
        return count;
    }

    public boolean isUnique(String astr) {
        int[] dp = new int[127];
        for (int i = 0; i < astr.length(); i++) {
            dp[astr.charAt(i)]++;
            if (dp[astr.charAt(i)] > 1) {
                return false;
            }
        }
        return true;
    }

    public List<List<Integer>> permute(int[] nums) {
        return permutes(nums, new ArrayList<>());
    }

    List<List<Integer>> result = new ArrayList<>();

    public List<List<Integer>> permutes(int[] nums, List<Integer> list) {
        for (int num : nums) {
            if (!list.contains(num)) {
                List<Integer> temp = new ArrayList<>(list);
                temp.add(num);
                if (temp.size() == nums.length) {
                    result.add(temp);
                } else {
                    permutes(nums, temp);
                }
            }
        }
        return result;
    }

    public int minTime(int[] time, int m) {
        if (time.length <= m) {
            return 0;
        }
        int l = 0;
        int r = Integer.MAX_VALUE;
        while (l < r) {
            int mid = (l + r) / 2;
            if (canSplit(time, m, mid)) {
                r = mid;
            } else {
                l = mid + 1;
            }
        }
        return r;
    }

    private boolean canSplit(int[] time, int m, int k) {
        int cnt = 0;
        int sum = 0;
        int maxT = 0;
        for (int value : time) {
            sum += value;
            maxT = Math.max(maxT, value);
            if (sum - maxT > k) {
                if (++cnt == m) return false;
                sum = value;
                maxT = value;
            }
        }
        return true;
    }

    public int expectNumber(int[] scores) {
        Arrays.sort(scores);
        int result = 1;
        int temp = scores[0];
        for (int score : scores) {
            if (score != temp) {
                result++;
                temp = score;
            }
        }
        return result;
    }

    public int search(int[] nums, int target) {
        int l = 0;
        int r = nums.length - 1;
        while (l <= r) {
            int mid = (l + r) / 2;
            if (nums[mid] == target) {
                return mid;
            }
            //若mid位小于第一位,说明mid没有在旋转过的一边
            if (nums[0] <= nums[mid]) {
                //taget在0和mid之间
                if (nums[0] <= target && target < nums[mid]) {
                    r = mid - 1;
                } else {
                    l = mid + 1;
                }
            } else {
                //mid在旋转过的一边
                if (nums[mid] < target && target <= nums[nums.length - 1]) {
                    //taget在mid和最后一位之间
                    l = mid + 1;
                } else {
                    r = mid - 1;
                }
            }
        }
        return -1;
    }

    public int findDuplicate(int[] nums) {
        int l = 0;
        int r = nums.length - 1;
        while (l < r) {
            int mid = (l + r) >>> 1;
            long count = Arrays.stream(nums).filter(a -> a <= mid).count();
            if (count > mid) {
                r = mid;
            } else {
                l = mid + 1;
            }
        }
        return l;
    }

    /**
     * 斐波那契数列
     */
    public int fib(int N) {
        if (N == 0) {
            return 0;
        }
        if (N == 1) {
            return 1;
        }
        int[] dp = new int[N + 1];
        dp[0] = 0;
        dp[1] = 1;
        for (int i = 2; i <= N; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[N];
    }

    public double myPow(double x, int n) {
        long m = n;
        //把负数化为1/x的n次幂
        if (m < 0) {
            x = 1 / x;
            m = -m;
        }
        //结果
        double ans = 1;
        //当前值
        double cur = x;
        for (long i = m; i > 0; i /= 2) {
            if ((i % 2) == 1) {
                ans = ans * cur;
            }
            cur = cur * cur;
        }
        return ans;
    }

    public int kthGrammar(int N, int K) {
        if (N == 1) return 0;
        return (~K & 1) ^ kthGrammar(N - 1, (K + 1) / 2);
    }

    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        int[] result = new int[nums1.length];
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums2.length; i++) {
            for (int j = i; j < nums2.length; j++) {
                if (nums2[j] > nums2[i]) {
                    map.put(nums2[i], nums2[j]);
                    break;
                }
            }
        }
        for (int i = 0; i < nums1.length; i++) {
            result[i] = map.getOrDefault(nums1[i], -1);
        }
        return result;
    }

    public int[] singleNumbers(int[] nums) {
        //计算两个数ab的异或值
        int result = 0;
        for (int i = 0; i < nums.length; i++) {
            result ^= nums[i];
        }
        //结果中找到为1的位
        int div = 1;
        while ((div & result) == 0) {
            div <<= 1;
        }
        //说明当前位 a为1，b为0，分组，再异或去重
        int a = 0, b = 0;
        for (int i = 0; i < nums.length; i++) {
            if ((div & nums[i]) == 0) {
                a ^= nums[i];
            } else {
                b ^= nums[i];
            }
        }
        return new int[]{a, b};
    }

    public int pivotIndex(int[] nums) {
        int sum = Arrays.stream(nums).sum();
        int result = 0;
        for (int i = 0; i < nums.length; i++) {
            if ((sum - nums[i]) == 2 * result) {
                return i;
            }
            result += nums[i];
        }
        return -1;
    }

    public int dominantIndex(int[] nums) {
        //第一大的数和第二大的数
        int max = nums[0], max2 = 0, index = 0;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] > max) {
                max2 = max;
                max = nums[i];
                index = i;
            } else {
                max2 = Math.max(max2, nums[i]);
            }
        }
        if (max >= 2 * max2) {
            return index;
        }
        return -1;
    }

    public int[] plusOne(int[] digits) {
        boolean ret = false;
        for (int i = digits.length - 1; i >= 0; i--) {
            if (digits[i] < 9) {
                digits[i] = digits[i] + 1;
                ret = false;
                break;
            } else {
                digits[i] = 0;
            }
            ret = true;
        }
        if (ret) {
            digits = new int[digits.length + 1];
            digits[0] = 1;
        }
        return digits;
    }

    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> ret = new ArrayList<>();
        int m = matrix.length;
        if (m == 0) return ret;
        int n = matrix[0].length;
        int b = 0, t = m - 1, l = 0, r = n - 1;
        while (true) {
            for (int j = l; j <= r; j++) ret.add(matrix[b][j]);
            if (++b > t) break;
            for (int i = b; i <= t; i++) ret.add(matrix[i][r]);
            if (--r < l) break;
            for (int j = r; j >= l; j--) ret.add(matrix[t][j]);
            if (--t < b) break;
            for (int i = t; i >= b; i--) ret.add(matrix[i][l]);
            if (++l > r) break;
        }
        return ret;
    }

    public int findInMountainArray(int target, List<Integer> mountainArr) {
        int size = mountainArr.size();
        // 步骤 1：先找到山顶元素所在的索引
        int mountaintop = findMountaintop(mountainArr, 0, size - 1);
        // 步骤 2：在前有序且升序数组中找 target 所在的索引
        int res = findFromSortedArr(mountainArr, 0, mountaintop, target);
        if (res != -1) {
            return res;
        }
        // 步骤 3：如果步骤 2 找不到，就在后有序且降序数组中找 target 所在的索引
        return findFromInversedArr(mountainArr, mountaintop + 1, size - 1, target);
    }

    private int findFromInversedArr(List<Integer> mountainArr, int l, int r, int target) {
        // 在后有序且降序数组中找 target 所在的索引
        while (l < r) {
            int mid = l + (r - l) / 2;
            // 与 findFromSortedArr 方法不同的地方仅仅在于由原来的小于号改成大于好
            if (mountainArr.get(mid) > target) {
                l = mid + 1;
            } else {
                r = mid;
            }

        }
        // 因为不确定区间收缩成 1个数以后，这个数是不是要找的数，因此单独做一次判断
        if (mountainArr.get(l) == target) {
            return l;
        }
        return -1;
    }

    private int findFromSortedArr(List<Integer> mountainArr, int l, int r, int target) {
        // 在前有序且升序数组中找 target 所在的索引
        while (l < r) {
            int mid = l + (r - l) / 2;
            if (mountainArr.get(mid) < target) {
                l = mid + 1;
            } else {
                r = mid;
            }

        }
        // 因为不确定区间收缩成 1个数以后，这个数是不是要找的数，因此单独做一次判断
        if (mountainArr.get(l) == target) {
            return l;
        }
        return -1;
    }

    private int findMountaintop(List<Integer> mountainArr, int l, int r) {
        // 返回山顶元素
        while (l < r) {
            int mid = l + (r - l) / 2;
            // 取左中位数，因为进入循环，数组一定至少有 2 个元素
            // 因此，左中位数一定有右边元素，数组下标不会发生越界
            if (mountainArr.get(mid) < mountainArr.get(mid + 1)) {
                // 如果当前的数比右边的数小，它一定不是山顶
                l = mid + 1;
            } else {
                r = mid;
            }
        }
        // 根据题意，山顶元素一定存在，因此退出 while 循环的时候，不用再单独作判断
        return l;
    }

    public int evalRPN(String[] tokens) {
        if (tokens.length == 1) {
            return Integer.parseInt(tokens[0]);
        }
        Stack<Integer> stack = new Stack<>();
        int result = 0;
        for (String token : tokens) {
            switch (token) {
                case "+":
                    int a = stack.pop();
                    int b = stack.pop();
                    result = a + b;
                    stack.push(result);
                    break;
                case "-":
                    a = stack.pop();
                    b = stack.pop();
                    result = b - a;
                    stack.push(result);
                    break;
                case "*":
                    a = stack.pop();
                    b = stack.pop();
                    result = a * b;
                    stack.push(result);
                    break;
                case "/":
                    a = stack.pop();
                    b = stack.pop();
                    result = b / a;
                    stack.push(result);
                    break;
                default:
                    stack.push(Integer.parseInt(token));
            }
        }
        return result;
    }

    public int jump(int[] nums) {
        //既然我们肯定可以到达最后的位置，dp[]用来记录到达当前位置的最小跳跃数
        if (nums.length < 2) return 0;
        int[] dp = new int[nums.length];
        dp[0] = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] >= 0 && nums[j] + j >= i) {
                    //有个技巧可以利用，就是我们从左往右第一个能够跳到i处位置的一定是最小跳跃数的
                    dp[i] = dp[j] + 1;
                    break;
                }

            }
        }
        return dp[nums.length - 1];
    }

    public int mincostTickets(int[] days, int[] costs) {
        //票可以买的日期
        int[] durations = new int[]{1, 7, 30};
        //最低价
        int[] low = new int[]{Math.min(Math.min(costs[0], costs[1]), costs[2]), Math.min(costs[1], costs[2]), costs[2]};
        int[] dp = new int[days.length];
        //dp[0] 为最低票价 （防止7天 30天最低情况）
        dp[0] = Math.min(Math.min(costs[0], costs[1]), costs[2]);
        for (int i = 1; i < days.length; i++) {
            //最近1天、最近7内、最近30天分别+他们的上一次，的最小值，就是当前天数的最小值
            //直接买一天的票
            dp[i] = dp[i - 1] + low[0];
            //买7天或者30天的
            for (int k = 1; k < 3; k++) {
                //记录上一次的值(感觉可以优化，可以从后往前遍历找)
                int temp = 0;
                for (int j = 0; j < i; j++) {
                    //最近7天内,或30天内
                    if (days[i] - days[j] >= durations[k]) {
                        temp = dp[j];
                    }

                }
                dp[i] = Math.min(dp[i], temp + low[k]);
            }
        }
        return dp[days.length - 1];
    }

    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        for (int num : nums) {
            for (List<Integer> value : new ArrayList<>(list)) {
                List<Integer> temp = new ArrayList<>(value);
                temp.add(num);
                list.add(temp);
            }
        }
        return list;
    }

    public List<Integer> grayCode(int n) {
        List<Integer> res = new ArrayList<Integer>() {{
            add(0);
        }};
        int head = 1;
        for (int i = 0; i < n; i++) {
            for (int j = res.size() - 1; j >= 0; j--)
                res.add(head + res.get(j));
            head <<= 1;
        }
        return res;
    }

    public int findLengthOfLCIS(int[] nums) {
        if (nums.length == 0) {
            return 0;
        }
        int temp = 1, result = 1;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] > nums[i - 1]) {
                temp++;
            } else {
                temp = 1;
            }
            result = Math.max(result, temp);
        }
        return result;
    }

    public int longestConsecutive(int[] nums) {
        if (nums.length == 0) {
            return 0;
        }
        Arrays.sort(nums);
        int temp = 1, result = 1;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] - nums[i - 1] == 1) {
                temp++;
            } else if (nums[i] != nums[i - 1]) {
                temp = 1;
            }
            result = Math.max(result, temp);
        }
        return result;
    }

    public String getPermutation(int n, int k) {
        char[] c = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};
        char[] result = new char[n];
        //阶乘
        int[] factorials = new int[n];
        factorials[0] = 1;
        for (int i = 1; i < n; i++) {
            factorials[i] = factorials[i - 1] * i;
        }
        k--;
        for (int i = n - 1; i >= 0; i--) {
            //处于第几个区间就是几
            int a = k / factorials[i];
            k -= a * factorials[i];
            result[n - 1 - i] = c[a];
            System.arraycopy(c, a + 1, c, a, c.length - a - 1);
        }
        return new String(result);
    }

    public int findCircleNum(int[][] M) {
        int m = M.length;
        int n = M[0].length;
        //每个人是否有朋友圈
        int[] f = new int[m];
        //所有的朋友关系
        Queue<Integer> queue = new ArrayDeque<>();
        int count = 0;
        for (int i = 0; i < m; i++) {
            if (f[i] == 0) {
                queue.add(i);
                while (!queue.isEmpty()) {
                    Integer value = queue.poll();
                    f[value] = 1;
                    for (int j = 0; j < n; j++) {
                        if (M[value][j] == 1 && f[j] == 0) {
                            queue.add(j);
                        }
                    }
                }
                count++;
            }
        }
        return count;
    }

    public boolean increasingTriplet(int[] nums) {
        int n = nums.length;
        //上一个数
        for (int j = 1; j < n - 1; j++) {
            int i = Arrays.stream(nums).limit(j).min().getAsInt();
            int k = Arrays.stream(nums).skip(j).max().getAsInt();
            if (nums[j] > i && nums[j] < k) {
                return true;
            }
        }
        return false;
    }

    public List<String> letterCombinations(String digits) {

        Map<Character, Character[]> map = new HashMap<>();
        map.put('2', new Character[]{'a', 'b', 'c'});
        map.put('3', new Character[]{'d', 'e', 'f'});
        map.put('4', new Character[]{'g', 'h', 'i'});
        map.put('5', new Character[]{'j', 'k', 'l'});
        map.put('6', new Character[]{'m', 'n', 'o'});
        map.put('7', new Character[]{'p', 'q', 'r', 's'});
        map.put('8', new Character[]{'t', 'u', 'v'});
        map.put('9', new Character[]{'w', 'x', 'y', 'z'});

        List<String> list = new ArrayList<>();
        if ("".equals(digits)) {
            return list;
        }
        list.add("");
        for (int i = 0; i < digits.length(); i++) {
            List<String> temps = new ArrayList<>();
            char c = digits.charAt(i);
            Character[] cs = map.get(c);
            for (Character character : cs) {
                for (String s : list) {
                    temps.add(s + character);
                }
            }
            list = temps;
        }
        return list;
    }

    public int subarraySum(int[] nums, int k) {
        int count = 0, pre = 0;
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 1);
        for (int num : nums) {
            pre += num;
            if (map.containsKey(pre - k)) {
                count += map.get(pre - k);
            }
            map.put(pre, map.getOrDefault(pre, 0) + 1);
        }
        return count;
    }

    /**
     * 6 9 互换
     */
    public int maximum69Number(int num) {
        String s = String.valueOf(num);
        char[] chars = s.toCharArray();
        int i = s.indexOf('6');
        if (i >= 0) {
            chars[i] = '9';
        }
        return Integer.parseInt(new String(chars));
    }

    /**
     * 唯一摩尔斯密码词
     */
    public int uniqueMorseRepresentations(String[] words) {
        String[] s = new String[]{".-", "-...", "-.-.", "-..", ".", "..-.",
                "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.",
                "---", ".--.", "--.-", ".-.", "...", "-",
                "..-", "...-", ".--", "-..-", "-.--", "--.."};
        Set<String> set = new HashSet<>();
        for (String word : words) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                sb.append(s[word.charAt(i) - 'a']);
            }
            set.add(sb.toString());
        }
        return set.size();
    }

    /**
     * 反转图像
     */
    public int[][] flipAndInvertImage(int[][] a) {
        int m = a.length;
        int n = a[0].length;
        int[][] res = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                res[i][n - j - 1] = a[i][j] == 0 ? 1 : 0;
            }
        }
        return res;
    }

    /**
     * 乘积最大子数组
     */
    public int maxProduct(int[] nums) {
        int maxF = nums[0], minF = nums[0], ans = nums[0];
        for (int i = 1; i < nums.length; ++i) {
            int mx = maxF, mn = minF;
            maxF = Math.max(mx * nums[i], Math.max(nums[i], mn * nums[i]));
            minF = Math.min(mn * nums[i], Math.min(nums[i], mx * nums[i]));
            ans = Math.max(maxF, ans);
        }
        return ans;
    }

    public int robotSim(int[] commands, int[][] obstacles) {
        //初始点,
        int[] dx = new int[]{0, 1, 0, -1};
        int[] dy = new int[]{1, 0, -1, 0};
        int x = 0, y = 0, di = 0;

        //障碍点
        Set<Long> obstacleSet = new HashSet();
        for (int[] obstacle : obstacles) {
            long ox = (long) obstacle[0] + 30000;
            long oy = (long) obstacle[1] + 30000;
            obstacleSet.add((ox << 16) + oy);
        }

        int ans = 0;
        for (int cmd : commands) {
            if (cmd == -2)  //left
                di = (di + 3) % 4;
            else if (cmd == -1)  //right
                di = (di + 1) % 4;
            else {
                for (int k = 0; k < cmd; ++k) {
                    int nx = x + dx[di];
                    int ny = y + dy[di];
                    long code = (((long) nx + 30000) << 16) + ((long) ny + 30000);
                    if (!obstacleSet.contains(code)) {
                        x = nx;
                        y = ny;
                        ans = Math.max(ans, x * x + y * y);
                    }
                }
            }
        }
        return ans;
    }

    public int minEatingSpeed(int[] piles, int H) {
        int left = 1;
        int right = (int) Math.pow(10, 9);
        while (left < right) {
            int mid = (left + right) / 2;
            if (possible(piles, H, mid)) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }

    /**
     * 判断是否能吃完
     */
    public boolean possible(int[] piles, int H, int k) {
        int time = 0;
        for (int pile : piles) {
            time += (pile - 1) / k + 1;
        }
        return time <= H;
    }

    public int lenLongestFibSubseq(int[] A) {
        int n = A.length;
        //存储每个数的值与索引
        Map<Integer, Integer> index = new HashMap();
        for (int i = 0; i < n; i++) {
            index.put(A[i], i);
        }
        Map<Integer, Integer> longest = new HashMap();
        int ans = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                //A[i] - A[j]的差，查看他们的索引是否存在
                int k = index.getOrDefault(A[i] - A[j], -1);
                if (k >= 0 && k < j) {
                    //说明存在值,并且索引在j之前
                    int cand = longest.getOrDefault(k * n + j, 2) + 1;
                    //设置值,值为前一个数量+1，前一个没有为2
                    longest.put(j * n + i, cand);
                    ans = Math.max(ans, cand);
                }
            }
        }
        return ans;
    }

    public String freqAlphabets(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            if (i < s.length() - 2 && s.charAt(i + 2) == '#') {
                sb.append((char) ('a' + Integer.parseInt(s.substring(i, i + 2)) - 1));
                i += 3;
            } else {
                sb.append((char) ('a' + Integer.parseInt(s.substring(i, i + 1)) - 1));
                i++;
            }
        }
        return sb.toString();
    }

    public int[] fraction(int[] cont) {
        int[] ans = {1, 1};
        int len = cont.length;
        if (len == 0) return ans;
        ans[1] = cont[len - 1];
        for (int i = len - 2; i >= 0; i--) {
            int tmp = ans[1];
            ans[1] = cont[i] * ans[1] + ans[0];
            ans[0] = tmp;
        }
        ;
        ans[0] = ans[0] ^ ans[1];
        ans[1] = ans[0] ^ ans[1];
        ans[0] = ans[0] ^ ans[1];
        return ans;
    }

    public boolean checkSubarraySum(int[] nums, int k) {
        int sum = 0;
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, -1);
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];
            if (k != 0) sum = sum % k;
            if (map.containsKey(sum)) {
                if (i - map.get(sum) > 1)
                    return true;
            } else {
                map.put(sum, i);
            }
        }
        return false;
    }

    public int countSquares(int[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] dp = new int[m][n];
        int res = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                //开始数量
                if (i == 0 || j == 0) {
                    dp[i][j] = matrix[i][j];
                } else if (matrix[i][j] == 0) {
                    dp[i][j] = 0;
                } else {
                    //前面的最小数+本身的1
                    //也就是左/上/左上都满足时
                    dp[i][j] = Math.min(Math.min(dp[i][j - 1], dp[i - 1][j]), dp[i - 1][j - 1]) + 1;
                }
                res += dp[i][j];
            }
        }
        return res;
    }

    public int numDupDigitsAtMostN(int n) {
        List<Integer> digits = new ArrayList<>();
        int temp = n;
        while (temp > 0) {
            digits.add(temp % 10);
            temp /= 10;
        }
        //位数
        int k = digits.size();
        //0-9,被使用次数,不重复
        int[] used = new int[10];
        int total = 0;

        //给每一位安排不一样的数字,预处理
        for (int i = 0; i < k; i++) {
            //A(i,j)种放置方法
            total += 9 * A(9, i - 1);
        }

        //从高位开始处理，(最后加入最高位的情况)
        for (int i = k - 1; i >= 0; i--) {
            //每位的数字
            int num = digits.get(i);

            //最高位从1开始，因为最高位不能为0
            for (int j = i == k - 1 ? 1 : 0; j < num; j++) {
                //当前数已被使用
                if (used[j] != 0) {
                    continue;
                }
                total += A(10 - (k - i), i);
            }

            if (++used[num] > 1) {
                break;
            }

            if (i == 0) {
                total += 1;
            }
        }
        //总数-不重复=含重复
        return n - total;
    }


    public int A(int i, int j) {
        return fact(i) / fact(i - j);
    }

    /**
     * 求阶乘
     */
    public int fact(int n) {
        if (n == 1 || n == 0) {
            return 1;
        }
        return n * fact(n - 1);
    }

    public int palindromePartition(String s, int k) {
        //i:前i个字符, j:分割了几次
        int[][] dp = new int[s.length() + 1][k + 1];
        for (int[] ints : dp) {
            Arrays.fill(ints, Integer.MAX_VALUE);
        }
        dp[0][0] = 0;
        for (int i = 1; i <= s.length(); i++) {
            for (int j = 1; j <= Math.min(i, k); j++) {
                if (j == 1) {
                    //第一次分割,直接前i个数
                    dp[i][j] = cost(s, 0, i - 1);
                } else {
                    for (int l = j - 1; l < i; l++) {
                        //前面1次+从位置到当前位置的 最小值
                        dp[i][j] = Math.min(dp[i][j], dp[l][j - 1] + cost(s, l, i - 1));
                    }
                }
            }
        }
        return dp[s.length()][k];
    }

    /**
     * i-j字符,变成回文串需要的变化次数
     */
    public int cost(String s, int l, int r) {
        int res = 0;
        for (int i = l, j = r; i < j; i++, j--) {
            if (s.charAt(i) != s.charAt(j)) {
                res++;
            }
        }
        return res;
    }

    public int videoStitching(int[][] clips, int t) {
        //从i-j的片段需要的片段数
        int[][] dp = new int[t + 1][t + 1];
        for (int[] ints : dp) {
            Arrays.fill(ints, 101);
        }
        //初始化
        for (int[] clip : clips) {
            for (int j = clip[0]; j <= clip[1]; j++) {
                for (int k = j; k <= clip[1]; k++) {
                    if (j <= t && k <= t) {
                        dp[j][k] = 1;
                    }
                }
            }
        }
        //
        for (int i = 0; i <= t; i++) {
            for (int j = 0; j <= t; j++) {
                if (dp[i][j] == 101) {
                    for (int k = i; k < j; k++) {
                        dp[i][j] = Math.min(dp[i][k] + dp[k][j], dp[i][j]);
                    }
                }
            }
        }
        return dp[0][t] == 101 ? -1 : dp[0][t];
    }

    public boolean divisorGame(int n) {
        //动态规划

        if (n == 1) {
            return false;
        }
        //dp[i]存的是操作数为i时的玩家的获胜情况
        boolean[] dp = new boolean[n + 1];

        //初始化dp数组
        dp[1] = false;
        dp[2] = true;

        //遍历3-N并求解整个dp数组
        for (int i = 3; i <= n; i++) {
            //先置dp[i]为false，符合条件则置true
            dp[i] = false;

            //玩家都以最佳状态，即玩家操作i后的操作数i-x应尽可能使对手输，即dp[i-x]应尽可能为false
            //所以遍历x=1~i-1,寻找x的约数，使得dp[i-x]=false，那么dp[i]=true即当前操作数为i的玩家能获胜
            //如果找不到则为false，会输掉
            for (int x = 1; x < i; x++) {
                if (i % x == 0 && !dp[i - x]) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[n];
    }

    public int waysToStep(int n) {
        int[] dp = new int[Math.max(n + 1, 4)];
        dp[1] = 1;
        dp[2] = 2;
        dp[3] = 4;
        for (int i = 4; i <= n; i++) {
            dp[i] = ((dp[i - 1] + dp[i - 2]) % 1000000007 + dp[i - 3]) % 1000000007;
        }
        return dp[n];
    }

    public int[] shortestToChar(String s, char c) {
        int[] res = new int[s.length()];
        int last = Integer.MAX_VALUE;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                for (int j = last == Integer.MAX_VALUE ? 0 : last; j < i; j++) {
                    res[j] = Math.min(Math.abs(j - last), Math.abs(j - i));
                }
                last = i;
            }
        }
        for (int j = last == Integer.MAX_VALUE ? 0 : last; j < s.length(); j++) {
            res[j] = Math.abs(j - last);
        }
        return res;
    }

    public boolean validMountainArray(int[] A) {
        int N = A.length;
        int i = 0;
        // walk up
        while (i + 1 < N && A[i] < A[i + 1])
            i++;
        // peak can't be first or last
        if (i == 0 || i == N - 1)
            return false;
        // walk down
        while (i + 1 < N && A[i] > A[i + 1])
            i++;
        return i == N - 1;
    }

    public int findSpecialInteger(int[] arr) {
        if (arr.length == 1) {
            return arr[0];
        }
        int n = arr.length, temp = 1;
        for (int i = 1; i < n; i++) {
            if (arr[i] == arr[i - 1]) {
                temp++;
            } else {
                temp = 1;
            }
            if (temp * 4 > n) {
                return arr[i];
            }
        }
        return -1;
    }

    public String[] findOcurrences(String text, String first, String second) {
        List<String> list = new ArrayList<>();
        String[] s = text.split(" ");
        for (int i = 1; i < s.length - 1; i++) {
            if (s[i - 1].equals(first) && s[i].equals(second)) {
                list.add(s[i + 1]);
            }
        }
        return list.stream().toArray(String[]::new);
    }

    public int twoCitySchedCost(int[][] costs) {
        int sum = 0;
        //根据花费的差值排序
        Arrays.sort(costs, Comparator.comparing(a -> a[0] - a[1]));
        //前面是差值小的选大的,后面差值大的选小的
        for (int i = 0; i < costs.length / 2; i++) {
            sum += costs[i][0];
            sum += costs[costs.length - 1 - i][1];
        }
        return sum;
    }

    public double largestTriangleArea(int[][] points) {
        int N = points.length;
        double ans = 0;
        for (int i = 0; i < N; ++i)
            for (int j = i + 1; j < N; ++j)
                for (int k = j + 1; k < N; ++k)
                    ans = Math.max(ans, area(points[i], points[j], points[k]));
        return ans;
    }

    public double area(int[] P, int[] Q, int[] R) {
        return 0.5 * Math.abs(P[0] * Q[1] + Q[0] * R[1] + R[0] * P[1]
                - P[1] * Q[0] - Q[1] * R[0] - R[1] * P[0]);
    }

    public int rotatedDigits(int n) {
        return (int) IntStream.range(1, n + 1).filter(this::isRotatedDigits).count();
    }

    private boolean isRotatedDigits(int i) {
        int count = 0;
        while (i > 0) {
            int a = i % 10;
            if (a == 3 || a == 4 || a == 7) {
                return false;
            }
            if (a == 2 || a == 5 || a == 6 || a == 9) {
                count++;
            }
            i /= 10;
        }
        return count > 0;
    }

    public int findShortestSubArray(int[] nums) {
        List<Integer> collect = Arrays.stream(nums).boxed().collect(Collectors.toList());
        Map<Integer, Long> map = Arrays.stream(nums)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.counting()));
        Integer i = map.entrySet().stream().sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()
                .thenComparing(v -> collect.lastIndexOf(v.getKey()) - collect.indexOf(v.getKey()))).findFirst().map(Map.Entry::getKey).get();
        return collect.lastIndexOf(i) - collect.indexOf(i) + 1;
    }

    public String[] findRelativeRanks(int[] nums) {
        List<Integer> list = Arrays.stream(nums)
                .boxed()
                .sorted(Comparator.<Integer, Integer>comparing(Function.identity()).reversed())
                .collect(Collectors.toList());
        String[] res = new String[nums.length];
        for (int i = 0; i < nums.length; i++) {
            int index = list.indexOf(nums[i]);
            if (index == 0) {
                res[i] = "Gold Medal";
            } else if (index == 1) {
                res[i] = "Silver Medal";
            } else if (index == 2) {
                res[i] = "Bronze Medal";
            } else {
                res[i] = String.valueOf(index + 1);
            }
        }
        return res;
    }

    public boolean canPlaceFlowers(int[] flowerbed, int n) {
        int count = 0;
        for (int i = 0; i < flowerbed.length; i++) {
            if (flowerbed[i] == 0) {
                //没种植花
                int j = i > 0 ? flowerbed[i - 1] : 0;
                int k = i < flowerbed.length - 1 ? flowerbed[i + 1] : 0;
                if (j == 0 && k == 0) {
                    //若前后都没有则种上花
                    count++;
                    flowerbed[i] = 1;
                }
            }
        }
        return count >= n;
    }

    public int[] findErrorNums(int[] nums) {
        Arrays.sort(nums);
        int[] res = new int[2];
        for (int i = 0, j = 1; i < nums.length; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) {
                //重复
                res[0] = nums[i - 1];
                continue;
            }
            if (nums[i] != j && res[1] == 0) {
                //缺失的数据
                res[1] = j;
            }
            j++;
        }
        if (res[1] == 0) {
            res[1] = nums.length;
        }
        return res;
    }

    public boolean checkPossibility(int[] nums) {
        if (nums.length < 3) {
            return true;
        }
        int count = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            if (nums[i] > nums[i + 1]) {
                count++;
                if (count > 1) {
                    break;
                }
                if (i - 1 >= 0 && nums[i - 1] > nums[i + 1]) {
                    nums[i + 1] = nums[i];
                } else {
                    nums[i] = nums[i + 1];
                }
            }
        }
        return count <= 1;
    }

    public double new21Game(int n, int k, int w) {
        if (k == 0) {
            return 1;
        }
        //最多能抽出这么大的数，dp；抽出这个数的概率
        double[] dp = new double[w + k];
        //初始化最后一次的值
        for (int i = k; i <= Math.min(k + w, n); i++) {
            dp[i] = 1;
        }
        dp[k - 1] = 1.0 * Math.min(n - k + 1, w) / w;
        //算出所有数抽到的概率
        for (int i = k - 2; i >= 0; i--) {
            dp[i] = dp[i + 1] - (dp[i + w + 1] - dp[i + 1]) / w;
        }
        return dp[0];
    }

    public String licenseKeyFormatting(String s, int k) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c != '-') {
                if (sb.length() % (k + 1) == k) {
                    sb.append('-');
                }
                sb.append(Character.toUpperCase(c));
            }
        }
        return sb.reverse().toString();
    }

    public int tribonacci(int n) {
        int[] dp = new int[Math.max(n + 1, 3)];
        dp[0] = 0;
        dp[1] = 1;
        dp[2] = 1;
        for (int i = 3; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2] + dp[i - 3];
        }
        return dp[n];
    }

    public int[] numMovesStones(int a, int b, int c) {
        int[] res = new int[2];
        int[] array = {a, b, c};
        Arrays.sort(array);
        //两数相差大于1，就直接挪到中间数旁边，需要一次；两数挨着，就不动
        res[0] = (array[1] - array[0] == 1 ? 0 : 1) + (array[2] - array[1] == 1 ? 0 : 1);
        //两数相差等于2，就直接把另一个数挪中间
        res[0] = (array[1] - array[0] == 2 || array[2] - array[1] == 2) ? 1 : res[0];
        //最大值就是两边距离
        res[1] = array[2] - array[0] - 2;
        return res;
    }

    public boolean equationsPossible(String[] equations) {
        // 并查集
        Map<Character, Character> father = new HashMap<>();
        // 先将字符串方程从相等到不等排序
        Arrays.sort(equations, (s1, s2) -> {
            if (s1.charAt(1) == s2.charAt(1)) return 0;
            return s1.charAt(1) == '=' ? -1 : 1;
        });

        for (String s : equations) {
            char[] chars = s.toCharArray();
            char first = chars[0];
            char second = chars[3];
            // 获取根代表
            while (father.containsKey(first)) first = father.get(first);
            while (father.containsKey(second)) second = father.get(second);
            // 如果是不等，但根代表相同，说明出错
            if (chars[1] == '!') {
                if (first == second) return false;
                // 如果是相等，跳过根代表相同的情况，把一个根代表连接到另一个根代表上（合并集合）
            } else {
                if (first == second) continue;
                father.put(first, second);
            }
        }
        return true;
    }

    public int largestSumAfterKNegations(int[] A, int K) {
        for (int i = 0; i < K; i++) {
            Arrays.sort(A);
            A[0] = -A[0];
        }
        return Arrays.stream(A).sum();
    }

    public boolean checkStraightLine(int[][] coordinates) {
        int x1 = coordinates[1][0] - coordinates[0][0];
        int y1 = coordinates[1][1] - coordinates[0][1];
        for (int i = 2; i < coordinates.length; i++) {
            int x2 = coordinates[i][0] - coordinates[0][0];
            int y2 = coordinates[i][1] - coordinates[0][1];
            if (x1 * y2 != x2 * y1) {
                return false;
            }
        }
        return true;
    }

    public int convertInteger(int A, int B) {
        return Integer.bitCount(A ^ B);
    }

    public int numEquivDominoPairs(int[][] dominoes) {
        int ans = 0;
        int[] cp = new int[100];
        for (int[] arr : dominoes) {
            Arrays.sort(arr);
            ans += cp[arr[0] * 10 + arr[1]]++;
        }
        return ans;
    }

    public void duplicateZeros(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 0) {
                System.arraycopy(arr, i, arr, i + 1, arr.length - i - 1);
                i++;
            }
        }
    }

    public int maxScoreSightseeingPair(int[] A) {
        int ans = 0, mx = A[0];
        for (int j = 1; j < A.length; ++j) {
            ans = Math.max(ans, mx + A[j] - j);
            // 边遍历边维护
            mx = Math.max(mx, A[j] + j);
        }
        return ans;
    }

    public int findUnsortedSubarray(int[] nums) {
        int length = nums.length;
        int left = 0, right = -1;
        int max = nums[0], min = nums[length - 1];
        for (int i = 0; i < length; i++) {
            if (nums[i] < max) right = i;
            else max = nums[i];
            if (nums[length - i - 1] > min) left = length - i - 1;
            else min = nums[length - i - 1];
        }
        return right - left + 1;
    }

    public double findMaxAverage(int[] nums, int k) {
        double sum = 0;
        for (int i = 0; i < k; i++)
            sum += nums[i];
        double res = sum;
        for (int i = k; i < nums.length; i++) {
            sum += nums[i] - nums[i - k];
            res = Math.max(res, sum);
        }
        return res / k;
    }

    public int translateNum(int num) {
        return translateNum(String.valueOf(num), 0);
    }

    public int translateNum(String num, int index) {
        int result = 0;
        if (index >= num.length() - 1) {
            return 1;
        }
        result += translateNum(num, index + 1);
        if (index < num.length() - 1 && ((num.charAt(index) == '2' && num.charAt(index + 1) >= '0' && num.charAt(index + 1) <= '5')
                || num.charAt(index) == '1')) {
            result += translateNum(num, index + 2);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(new Dp().maxScoreSightseeingPair(
                new int[]{8, 1, 5, 2, 6}));
        System.out.println(new Dp().translateNum(11));
        new Dp().duplicateZeros(
                new int[]{1, 0, 2, 3, 0, 4, 5, 0});
        System.out.println();
    }
}
