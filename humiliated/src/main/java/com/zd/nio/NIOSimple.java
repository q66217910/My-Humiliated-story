package com.zd.nio;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * nio 例子
 */
public class NIOSimple {

    static int i = 0;

    public static void main(String[] args) throws InterruptedException {


        ReentrantLock lock = new ReentrantLock();

        for (int j = 0; j < 20000; j++) {
            new Thread(() -> {
                lock.lock();
                i=i+1;
                lock.unlock();
            }).start();
        }

        for (int j = 0; j < 20000; j++) {
            lock.lock();
            i=i+1;
            lock.unlock();
        }

        Thread.sleep(10000);
        System.out.println(i);
    }
}
