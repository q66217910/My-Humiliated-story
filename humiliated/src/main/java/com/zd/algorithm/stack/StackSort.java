package com.zd.algorithm.stack;

import lombok.Data;

import java.util.Stack;

/**
 * 排序栈
 */
@Data
public class StackSort<T extends Number> {

    private Stack<T> stack;

    public void add(T value) {
        if (stack.isEmpty()) {
            stack.push(value);
        }else {
            doChange(value);
        }
    }

    private void doChange(T value) {
        if (stack.isEmpty()||stack.peek().doubleValue()<=value.doubleValue()){
            stack.push(value);
        }else {
            T last = stack.pop();
            doChange(value);
            stack.push(last);
        }
    }

    public static void main(String[] args) {
        StackSort<Integer> stackSort = new StackSort<>();
        stackSort.setStack(new Stack<>());
        stackSort.add(4);
        stackSort.add(1);
        stackSort.add(2);
    }
}
