package com.zd.nio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NettySimple {

    public static void main(String[] args) throws InterruptedException {
        synchronized (new Object()) {
            System.out.println(11);
        }
    }

}
