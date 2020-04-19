package com.zd.algorithm.letcode.dp;

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

}
