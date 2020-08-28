package com.zd.nio;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class NettySimple {

    public static void main(String[] args) throws InterruptedException {

        

        ForkJoinPool pool = ForkJoinPool.commonPool();

        RecursiveAction recursiveAction = new RecursiveAction() {

            private  int a;

            @Override
            protected void compute() {
                if (a<100){
                    ForkJoinTask<Void> fork = this.fork();
                    a++;
                }
                System.out.println(0xc0000000);
            }

        };

        recursiveAction.invoke();
        System.out.println(recursiveAction.isCancelled());

    }

}
