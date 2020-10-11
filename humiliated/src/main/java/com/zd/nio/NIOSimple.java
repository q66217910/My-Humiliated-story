package com.zd.nio;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * nio 例子
 */
public class NIOSimple {


    public static void main(String[] args) throws InterruptedException {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0,
                TimeUnit.DAYS, new LinkedBlockingDeque<>(),
                r -> {
                    Thread thread = new Thread() {

                        @Override
                        public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
                            new ExceptionHandler();
                        }

                        @Override
                        public void run() {
                            r.run();
                        }

                    };
                    thread.setDaemon(false);
                    thread.setUncaughtExceptionHandler(new ExceptionHandler());
                    return thread;
                });
        Future<Integer> submit = threadPoolExecutor.submit(() -> {
            System.out.println(11);
            return 1;
        });
        try {
            System.out.println(submit.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    static class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            System.out.println(111);
        }
    }
}
