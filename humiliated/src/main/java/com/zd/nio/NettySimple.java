package com.zd.nio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NettySimple {

    public static void main(String[] args) throws InterruptedException {
        Map<Integer,Integer> map = new HashMap<>();
        map.put(5,2);
        map.put(2,5);
        List<Integer> list = new ArrayList<>();

        list.forEach(System.out::println);
    }

}
