package com.zd.nio;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * nio 例子
 */
public class NIOSimple {


    public static void main(String[] args) throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();

        Condition condition = lock.newCondition();

        new Thread(() -> {
            if (lock.tryLock()) {
                try {
                    condition.await();
                    System.out.println(11);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            if (lock.tryLock()) {
                condition.signalAll();
                System.out.println(22);
                lock.unlock();
            }
        }).start();


    }
}
