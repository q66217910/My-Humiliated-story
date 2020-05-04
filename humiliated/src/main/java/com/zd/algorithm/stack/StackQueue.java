package com.zd.algorithm.stack;

import lombok.Data;

import java.util.Queue;
import java.util.Stack;

/**
 * 两个栈实现队列
 */
@Data
public class StackQueue<T> {

    /**
     * 入栈
     */
    private Stack<T> stackIn;

    /**
     * 出栈
     */
    private Stack<T> stackOut;

    private StackQueue() {
        this.stackIn = new Stack<>();
        this.stackOut = new Stack<>();
    }

    private void add(T value) {
        this.stackIn.push(value);
    }

    private T peek() {
        if (stackOut.isEmpty() && stackIn.isEmpty()) {
            throw new RuntimeException(" stackIn isEmpty  ");
        } else if (this.stackOut.isEmpty()) {
            inToOut();
        }
        return this.stackOut.peek();
    }

    private T poll() {
        if (stackOut.isEmpty() && stackIn.isEmpty()) {
            throw new RuntimeException(" stackIn isEmpty  ");
        } else if (this.stackOut.isEmpty()) {
            inToOut();
        }
        return this.stackOut.pop();
    }



    private void inToOut() {
        while (!this.stackIn.isEmpty()) {
            this.stackOut.push(this.stackIn.pop());
        }
    }

    public boolean empty() {
        return this.stackOut.empty() && this.stackIn.empty();
    }

    public static void main(String[] args) {
        StackQueue<Integer> queue = new StackQueue<>();
        queue.add(1);
        queue.add(2);
        System.out.println(queue.poll());
        System.out.println(queue.poll());
    }
}
