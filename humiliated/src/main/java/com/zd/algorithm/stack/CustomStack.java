package com.zd.algorithm.stack;

public class CustomStack {

    private Integer[] ele;

    private int size;

    public CustomStack(int maxSize) {
        ele = new Integer[maxSize];
    }

    public void push(int x) {
        if (size >= ele.length) {
            return;
        }
        ele[size++] = x;
    }

    public int pop() {
        if (size == 0) {
            return -1;
        }
        int value = ele[--size];
        ele[size] = null;
        return value;
    }

    public void increment(int k, int val) {
        for (int i = 0; i < Math.min(k, ele.length); i++) {
            if (ele[i] == null) {
                break;
            }
            ele[i] += val;
        }
    }

    public static void main(String[] args) {
        CustomStack customStack = new CustomStack(2); // 栈是空的 []
        customStack.push(2);
        customStack.pop();
        customStack.increment(8, 100);
        customStack.pop();
        customStack.increment(9, 11);
        customStack.push(63);
        customStack.pop();
        customStack.push(84);
        customStack.increment(10, 93);
        customStack.increment(6, 45);
        customStack.increment(10, 4);
    }
}
