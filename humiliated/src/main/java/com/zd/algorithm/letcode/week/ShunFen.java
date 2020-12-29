package com.zd.algorithm.letcode.week;


import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

/**
 * @Description: 顺丰
 * @Author zd
 * @Date 2020/12/5
 * @Version 1.0
 */
public class ShunFen {

    public static Integer daLao1(String s, int n, int m) {
        int max = Integer.MIN_VALUE;
        int[][] dp = new int[n + 1][m + 1];
        //dp:表示几个 *号, 最后在第几位最佳乘积
        for (int i = 1; i < m; i++) {
            dp[1][i] = Integer.parseInt(s.substring(0, i));
            if (1 == n) {
                max = Math.max(max, dp[1][i] * Integer.parseInt(s.substring(i)));
            }
        }


        //i:几个*
        for (int i = 2; i <= n; i++) {
            //j:结尾
            for (int j = i; j < s.length() - 1; j++) {
                dp[i][j] = dp[i - 1][j - 1] * Integer.parseInt(s.substring(i, j + 1));
                if (i == n) {
                    max = Math.max(max, dp[i][j] * Integer.parseInt(s.substring(i)));
                }
            }
        }

        return max;
    }


    /**
     * @param s   最小字母序号
     * @param t   最大字母序号
     * @param w   位数
     * @param str 字符串
     */
    public static void daLao2(int s, int t, int w, String str) {
        int min = 97 + s - 1;
        int max = 97 + t - 1;
        char[] chars = str.toCharArray();

        //第几位
        for (int i = 0; i < 5; i++) {
            //从左到右递增
            for (int j = 1; j < str.length(); j++) {
                if (chars[j] <= chars[j - 1]) {
                    if (str.charAt(j - 1) + 1 <= max) {
                        chars[j] = (char) (chars[j - 1] + 1);
                    } else {
                        return;
                    }
                }
            }
            System.out.println(new String(chars));
        }
    }

    public static boolean isUnique(String str) {
        //从左到右递增
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i) <= str.charAt(i - 1)) {
                return false;
            }
        }
        return true;
    }

    public static int daLao3(char[][] arr, int n, int m) {
        //找出@
        int x = 0, y = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (arr[i][j] == '@') {
                    x = i;
                    y = j;
                }
            }
        }

        Set<String> set = new HashSet<>();
        Stack<String> stack = new Stack<>();
        stack.push(x + "_" + y);
        set.add(x + "_" + y);
        while (!stack.isEmpty()) {
            String pop = stack.pop();
            String[] s = pop.split("_");
            int a = Integer.parseInt(s[0]);
            int b = Integer.parseInt(s[1]);
            if (a > 0) {
                //向左
                if (arr[a - 1][b] == '.') {
                    set.add((a - 1) + "_" + b);
                    stack.push((a - 1) + "_" + b);
                }
            }
            if (a < n - 1) {
                //向右
                if (arr[a + 1][b] == '.') {
                    set.add((a + 1) + "_" + b);
                    stack.push((a + 1) + "_" + b);
                }
            }
            if (b > 0) {
                //向左
                if (arr[a][b - 1] == '.') {
                    set.add((a) + "_" + (b - 1));
                    stack.push((a) + "_" + (b - 1));
                }
            }
            if (b < m - 1) {
                //向左
                if (arr[a][b + 1] == '.') {
                    set.add((a) + "_" + (b + 1));
                    stack.push((a) + "_" + (b + 1));
                }
            }
        }
        return set.size();
    }

    public static void main(String[] args) {
        daLao2(2, 10, 5, "bdfij");
    }

}
