package com.zd.algorithm.letcode.dp;

import java.util.Arrays;

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

    public static void main(String[] args) {
        System.out.println(new Dp().minPathSum(new int[][]{{1, 3, 1}, {1, 5, 1}, {4, 2, 1}}));
    }
}
