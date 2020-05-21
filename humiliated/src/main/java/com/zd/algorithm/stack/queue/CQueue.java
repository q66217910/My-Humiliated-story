package com.zd.algorithm.stack.queue;

import java.util.Stack;

public class CQueue {

    private Stack<Integer> stackIn;

    private Stack<Integer> stackOut;

    private int size;

    public CQueue() {
        this.stackIn = new Stack<>();
        this.stackOut = new Stack<>();
    }

    public void appendTail(int value) {
        while (!stackIn.isEmpty()) {
            stackOut.push(stackIn.pop());
        }
        stackIn.push(value);
        while (!stackOut.isEmpty()) {
            stackIn.push(stackOut.pop());
        }
        size++;
    }

    public int deleteHead() {
        if (size == 0) {
            return -1;
        }
        size--;
        return stackIn.pop();
    }

}
