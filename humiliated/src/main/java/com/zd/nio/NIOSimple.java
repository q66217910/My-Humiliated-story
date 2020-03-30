package com.zd.nio;

import java.util.concurrent.ConcurrentHashMap;

/**
 * nio 例子
 */
public class NIOSimple {

    public static void main(String[] args) {
        ConcurrentHashMap<Integer,Integer> map = new ConcurrentHashMap<>();
        for (int i = 0; i < 20; i++) {
            map.put(i,i);
        }
    }

}
