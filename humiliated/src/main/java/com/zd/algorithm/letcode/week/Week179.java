package com.zd.algorithm.letcode.week;

public class Week179 {

    private int index = -1;

    public String generateTheString(int n) {
        return string(n);
    }

    public String string(int n) {
        StringBuffer sb = new StringBuffer();
        if (n > 0) {
            if (n % 2 == 1) {
                ++index;
                for (int i = 0; i < n; i++) {
                    sb.append((char) (index + 'a'));
                }
            } else {
                sb.append(string(n - 1));
                sb.append(string(1));
            }
        }
        return sb.toString();
    }

    public int numTimesAllBlue(int[] light) {
        boolean[] result = new boolean[5005];
        int cur = 0, ans = 0, maxi = 0;
        for (int value : light) {
            result[value] = true;
            maxi = Math.max(maxi, value);
            while (result[cur + 1]) {
                ++cur;
            }
            if (cur == maxi) {
                ++ans;
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new Week179().numTimesAllBlue(new int[]{2, 1, 3, 5, 4}));
    }
}
