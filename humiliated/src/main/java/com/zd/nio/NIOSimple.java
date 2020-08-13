package com.zd.nio;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * nio 例子
 */
public class NIOSimple {


    public static void main(String[] args) throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();

        Object obj = new Object();

        Thread t1 = new Thread(() -> {
            Thread.yield();
            LockSupport.park();
            System.out.println(22);
        });



        Thread t2 = new Thread(() -> {
            System.out.println(11);
        });

        LockSupport.park();
        LockSupport.parkNanos(111);
        t1.wait();
        t1.wait(111);

        t1.start();
        t2.start();
        LockSupport.unpark(t1);

    }
}
