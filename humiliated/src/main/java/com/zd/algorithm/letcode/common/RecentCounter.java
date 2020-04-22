package com.zd.algorithm.letcode.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecentCounter {

    private List<Integer> list;

    public RecentCounter() {
        list = new ArrayList<>();
    }

    public int ping(int t) {
        list.add(t);
        list=list.stream().filter(a->a>=t-3000).collect(Collectors.toList());
        return list.size();
    }

}
