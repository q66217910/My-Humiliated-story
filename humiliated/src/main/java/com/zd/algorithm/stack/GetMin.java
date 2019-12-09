package com.zd.algorithm.stack;

import lombok.Data;

import java.util.Stack;

/**
 * getMin 功能的栈
 */
@Data
public class GetMin {

    /**
     * 正常栈
     */
    private Stack<Integer> stackData;

    /**
     * 保存每一步的最小值
     */
    private Stack<Integer> stackMin;


    private GetMin() {
        stackData = new Stack<>();
        stackMin = new Stack<>();
    }

    private void push(int value) {
        stackData.push(value);
        if (stackMin.isEmpty()) {
            stackMin.push(value);
        } else if (getMin() >= value) {
            stackMin.push(value);
        }
    }

    private int pop() {
        if (stackData.isEmpty()) {
            throw new RuntimeException(" stackData isEmpty  ");
        }
        Integer value = stackData.pop();
        if (value == getMin()) {
            stackMin.pop();
        }
        return value;
    }

    private int getMin() {
        if (this.stackMin.isEmpty()) {
            throw new RuntimeException(" stackMin isEmpty  ");
        }
        return this.stackMin.peek();
    }

    public static void main(String[] args) {
        GetMin getMin = new GetMin();
        getMin.push(3);
        System.out.println(getMin.getMin());
        getMin.push(1);
        System.out.println(getMin.getMin());
        getMin.push(5);
        System.out.println(getMin.getMin());
        System.out.println("pop "+ getMin.pop());
        System.out.println(getMin.getMin());
        System.out.println("pop "+ getMin.pop());
        System.out.println(getMin.getMin());
        System.out.println("pop "+ getMin.pop());
    }
}
