package com.zd.algorithm.letcode.dp;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


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
            Arrays.fill(ints, Integer.MAX_VALUE);
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

    public static void main(String[] args) {
        System.out.println(new Dp().search(new int[]{4, 5, 6, 7, 0, 1, 2}, 0));
    }
}
