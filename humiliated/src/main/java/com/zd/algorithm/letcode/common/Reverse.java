package com.zd.algorithm.letcode.common;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Reverse {

    /**
     * 整数反转
     * 1. 取余获取最后一位pop，push到新数的第一位
     */
    public static int reverse(int x) {
        int rev = 0;
        while (x != 0) {
            int pop = x % 10;
            x /= 10;
            if (rev > Integer.MAX_VALUE / 10 || rev < Integer.MIN_VALUE / 10) return 0;
            rev = rev * 10 + pop;
        }
        return rev;
    }

    /**
     * 回文数  （左->右==右->左） 负数与个位数不算
     * 1. 通过 x%10 x/10 得到一个y=x%10 * 10+y  取反的新数
     * 2. 当 y>x 时终止，因为数字已经大于原始值
     * 3. 为防止数字是奇数个
     */
    public static boolean isPalindrome(int x) {
        //负数和个位数排除
        if (x < 0 || (x % 10 == 0 && x != 0)) {
            return false;
        }
        int rev = 0;
        while (x > rev) {
            rev = rev * 10 + x % 10;
            x /= 10;
        }
        return x == rev || x == rev / 10;
    }

    private static ImmutableMap<String, Integer> romanMap = ImmutableMap
            .<String, Integer>builder()
            .put("I", 1)
            .put("V", 5)
            .put("X", 10)
            .put("L", 50)
            .put("C", 100)
            .put("D", 500)
            .put("M", 1000)
            .put("IV", 4)
            .put("IX", 9)
            .put("XL", 40)
            .put("XC", 90)
            .put("CD", 400)
            .put("CM", 900)
            .build();

    /**
     * 罗马数字转整数
     * I : 1
     * V : 5
     * X : 10
     * L : 50
     * C : 100
     * D : 500
     * M : 1000
     * <p>
     * 1.比较连续2个值
     * 2.当小值在大值的左边 减小值
     * 3.小值在大值的右边，则加小值
     */
    public static int romanToInt(String s) {
        int sum = 0;
        int preNum = romanMap.get(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            int num = romanMap.get(s.charAt(i));
            if (preNum < num) {
                sum -= preNum;
            } else {
                sum += preNum;
            }
            preNum = num;
        }
        sum += preNum;
        return sum;
    }


    public String sortString(String s) {
        StringBuffer sb = new StringBuffer();
        List<Character> list = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            list.add(s.charAt(i));
        }
        return sort(true, list, sb);
    }

    private String sort(boolean flag, List<Character> list, StringBuffer sb) {
        List<Character> resultB = new ArrayList<>();
        if (flag) {
            char temp = 0;
            list.sort(Comparator.comparing(Function.identity()));
            for (Character character : list) {
                if (character > temp) {
                    temp = character;
                    sb.append(character);
                } else {
                    resultB.add(character);
                }
            }
        } else {
            char temp = Character.MAX_VALUE;
            list.sort(Comparator.reverseOrder());
            for (Character character : list) {
                if (character < temp) {
                    temp = character;
                    sb.append(character);
                } else {
                    resultB.add(character);
                }
            }
        }
        if (resultB.size() > 0) {
            sort(!flag, resultB, sb);
        }
        return sb.toString();
    }


    public int findTheLongestSubstring(String s) {
        int[] hs = new int[100];
        Arrays.fill(hs, -1);
        int result = 0;
        int index = 0;
        int h = 0;
        hs[0] = 0;
        for (int i = 0; i < s.length(); i++) {
            ++index;
            char c = s.charAt(i);
            switch (c) {
                case 'a':
                    h ^= (1 << 1);
                    break;
                case 'e':
                    h ^= (1 << 2);
                    break;
                case 'i':
                    h ^= (1 << 3);
                    break;
                case 'o':
                    h ^= (1 << 4);
                    break;
                case 'u':
                    h ^= (1);
                    break;
            }
            if (hs[h] >= 0) {
                result = Math.max(result, index - hs[h]);
            } else {
                hs[h] = index;
            }
        }
        return result;
    }

    public String reverseStr(String s, int k) {
        char[] a = s.toCharArray();
        for (int start = 0; start < s.length(); start += 2 * k) {
            int i = start, j = Math.min(start + k - 1, a.length - 1);
            while (i < j) {
                char tmp = a[i];
                a[i++] = a[j];
                a[j--] = tmp;
            }
        }
        return new String(a);
    }

    public static void main(String[] args) {
        new Reverse().reverseStr("abcdefg", 2);
    }
}
