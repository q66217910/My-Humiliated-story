package com.zd.algorithm.letcode.dp;

import java.util.*;


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
        List<Integer> res = new ArrayList<Integer>() {{ add(0); }};
        int head = 1;
        for (int i = 0; i < n; i++) {
            for (int j = res.size() - 1; j >= 0; j--)
                res.add(head + res.get(j));
            head <<= 1;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(new Dp().mincostTickets(new int[]{1, 4, 6, 7, 8, 20}, new int[]{2, 7, 15}));
    }
}
