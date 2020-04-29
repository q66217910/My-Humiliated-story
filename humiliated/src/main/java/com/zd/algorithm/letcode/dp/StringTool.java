package com.zd.algorithm.letcode.dp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
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

    public static void main(String[] args) {
        System.out.println(new StringTool().reverseWords("Let's take LeetCode contest"));
    }
}
