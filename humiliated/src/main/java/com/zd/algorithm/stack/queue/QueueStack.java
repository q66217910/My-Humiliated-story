package com.zd.algorithm.stack.queue;

import java.util.ArrayDeque;

public class QueueStack {

    private ArrayDeque<Integer> queue;

    /** Initialize your data structure here. */
    public QueueStack() {
        this.queue = new ArrayDeque();
    }

    /** Push element x onto stack. */
    public void push(int x) {
        queue.push(x);
    }

    /** Removes the element on top of the stack and returns that element. */
    public int pop() {
        return this.queue.pollFirst();
    }

    /** Get the top element. */
    public int top() {
        return queue.peekFirst();
    }

    /** Returns whether the stack is empty. */
    public boolean empty() {
        return queue.isEmpty();
    }
}
