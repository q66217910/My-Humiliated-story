package com.zd.nio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NettySimple {

       //打印次数
    private static int n = 102;
    //ALI三个字母信号量
    private static Semaphore semaphoreA=new Semaphore(0);
    private static Semaphore semaphoreB=new Semaphore(0);
    private static Semaphore semaphoreC=new Semaphore(0);


    //先打印a，然后按顺序释放l，l执行完成释放i，每个执行完后锁住自身
    public static void main(String[] args) throws InterruptedException {

        
        

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < n; i++) {
                System.out.printf("a");
                semaphoreB.release();
                try {
                    semaphoreA.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < n; i++) {
                try {
                    semaphoreB.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.printf("l");
                semaphoreC.release();
            }
        });

        Thread thread3 = new Thread(() -> {
            for (int i = 0; i < n; i++) {

                try {
                    semaphoreC.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.printf("i");
                semaphoreA.release();
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();
    }


}
