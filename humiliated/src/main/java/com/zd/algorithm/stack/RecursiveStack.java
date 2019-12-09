package com.zd.algorithm.stack;

import lombok.Data;

import java.util.Stack;

/**
 * 递归栈
 */
public class RecursiveStack {

    private static <T> T getAndRemoveLastElement(Stack<T> stack) {
        T value = stack.pop();
        if (stack.isEmpty()) {
            return value;
        } else {
            T last = getAndRemoveLastElement(stack);
            stack.push(value);
            return last;
        }
    }

    private static <T> void reverse(Stack<T> stack) {
        if (stack.isEmpty()) {
            return;
        }
        T value = getAndRemoveLastElement(stack);
        reverse(stack);
        stack.push(value);
    }


    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        reverse(stack);
    }
}
