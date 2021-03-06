package com.zd.algorithm.letcode.week;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Week177 {

    public static int daysBetweenDates(String date1, String date2) {
        int ret = 0;
        int RNum = 0;
        int[] moth = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        String[] date3 = date1.split("-");
        String[] date4 = date2.split("-");
        int year1 = Integer.parseInt(date3[0]);
        int year2 = Integer.parseInt(date4[0]);
        int month1 = Integer.parseInt(date3[1]);
        int month2 = Integer.parseInt(date4[1]);
        int day1 = Integer.parseInt(date3[2]);
        int day2 = Integer.parseInt(date4[2]);
        RNum = countYear(year1, year2, month1, month2);
        ret = (year1 - year2) * 365;
        for (int i = 0; i < month1; i++) {
            ret += moth[i];
        }
        for (int i = 0; i < month2; i++) {
            ret -= moth[i];
        }
        return ret - day2 + day1 + RNum;
    }

    private static int countYear(int year1, int year2, int month1, int month2) {
        int ret = 0;
        int end = year1;
        int start = year2;
        if (month1 >= 2) {
            end += 1;
        }
        if (month2 >= 2) {
            start += 1;
        }
        for (int i = start; i < end; i++) {
            if (isRUN(i)) {
                ret++;
            }
        }
        return ret;
    }

    private static boolean isRUN(int year) {
        if (year % 400 == 0) {
            return true;
        }
        if (year % 100 == 0) {
            return true;
        }
        if (year % 4 == 0) {
            return true;
        }
        return false;
    }

    class Entry {

        Entry left;

        Entry right;

        int value;

        public Entry(int value) {
            this.value = value;
        }
    }

    public boolean validateBinaryTreeNodes(int n, int[] leftChild, int[] rightChild) {
        Entry parent = new Entry(0);
        for (int i = 0; i < Math.abs(leftChild.length - rightChild.length); i++) {
            if (leftChild[i] != -1) {
                parent.left = new Entry(0);
            }
        }
        return false;
    }

    /**
     * 生命游戏
     */
    public int[][] gameOfLife(int[][] board) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                int count = 0;
                //计算周围细胞存活数量
                for (int k = -1; k < 2; k++) {
                    for (int l = -1; l < 2; l++) {
                        if (i + k > board.length - 1
                                || i + k < 0
                                || j + l > board[0].length - 1
                                || j + l < 0) continue;
                        if (board[i + k][j + l] == 1) count++;
                    }
                }
                //若当前是活细胞
                if (board[i][j] == 1) {
                    count--;
                    if (count < 2 || count > 3) {
                        map.put(i + "_" + j, 0);
                    }
                } else if (board[i][j] == 0) {
                    if (count == 3) {
                        map.put(i + "_" + j, 1);
                    }
                }
            }
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String[] s = entry.getKey().split("_");
            board[Integer.parseInt(s[0])][Integer.parseInt(s[1])] = entry.getValue();
        }
        return board;
    }

    public int movingCount(int m, int n, int k) {
        boolean[][] visited = new boolean[m][n];
        int res = 0;
        Queue<int[]> queue = new LinkedList<int[]>();
        queue.add(new int[]{0, 0, 0, 0});
        while (queue.size() > 0) {
            int[] x = queue.poll();
            int i = x[0], j = x[1], si = x[2], sj = x[3];
            if (i < 0 || i >= m || j < 0 || j >= n || k < si + sj || visited[i][j]) continue;
            visited[i][j] = true;
            res++;
            queue.add(new int[]{i + 1, j, (i + 1) % 10 != 0 ? si + 1 : si - 8, sj});
            queue.add(new int[]{i, j + 1, si, (j + 1) % 10 != 0 ? sj + 1 : sj - 8});
        }
        return res;
    }

    List<String>[] cache = new ArrayList[100];

    public List<String> generateParenthesis(int n) {
        return generate(n);
    }

    private List<String> generate(int n) {
        if (cache[n] != null) {
            return cache[n];
        }
        List<String> ans = new ArrayList<>();
        if (n == 0) {
            ans.add("");
        } else {
            for (int i = 0; i < n; i++) {
                for (String left : generate(i))
                    for (String right : generate(n - 1 - i))
                        ans.add("(" + left + ")" + right);
            }
        }
        cache[n] = ans;
        return ans;
    }

    public String reverseWords(String s) {
        s = s.trim();
        StringBuilder result = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) != ' ') {
                sb.append(s.charAt(i));
            }
            if (i >= 1 && s.charAt(i) == ' ' && s.charAt(i - 1) != ' ') {
                result.append(sb.reverse().toString()).append(" ");
                sb = new StringBuilder();
            }
        }
        result.append(sb.reverse().toString());
        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println(new Week177().reverseWords(
                "the sky is blue"));
    }
}
