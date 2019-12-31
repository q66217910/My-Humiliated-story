package com.zd.nio.future;

import org.checkerframework.checker.units.qual.A;

public class AwaitSimple {

    public static void main(String[] args) {
        AwaitSimple simple = new AwaitSimple();
        for (int i = 0; i < 3; i++) {
            new Thread(simple::awaitFirst).start();
        }
    }

    private  void awaitFirst(){
        boolean ret = true;
        synchronized (this){
            while (ret){
                try {
                    wait();
                    ret = false;
                    System.out.println(Thread.activeCount());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            notifyAll();
        }
    }
}
