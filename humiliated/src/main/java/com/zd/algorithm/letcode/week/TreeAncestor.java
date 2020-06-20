package com.zd.algorithm.letcode.week;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeAncestor {


    private int[][] dp = new int[50001][20];

    public TreeAncestor(int n, int[] parent) {
        int[] temp = new int[20];
        Arrays.fill(temp, -1);
        Arrays.fill(dp, temp);
        for (int i = 0; i < n; i++) dp[i][0] = parent[i];
        for (int j = 1; (1 << j) < n; j++)
            for (int i = 0; i < n; i++)
                if (dp[i][j - 1] != -1)
                    dp[i][j] = dp[dp[i][j - 1]][j - 1];
    }

    public int getKthAncestor(int node, int k) {
        if(k == 0 || node == -1) return node;
        int z =  (int)(Math.log(k)/Math.log(2));
        return getKthAncestor(dp[node][z], k - (1 << z));
    }

}
