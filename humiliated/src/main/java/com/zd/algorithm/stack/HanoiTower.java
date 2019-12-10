package com.zd.algorithm.stack;

import lombok.Data;

import java.util.Stack;

/**
 * 汉诺塔
 */
@Data
public class HanoiTower {

    private Stack<Integer> leftStack;
    private Stack<Integer> midStack;
    private Stack<Integer> rightStack;

    private int num;

    public HanoiTower(int num) {
        leftStack = new Stack<>();
        midStack = new Stack<>();
        rightStack = new Stack<>();
        this.num = num;
        for (int i = num; i > 0; i--) {
            leftStack.push(i);
        }
    }

    public int hanoi() {
        Action[] record = {Action.NO};
        int step = 0;
        while (rightStack.size() != num + 1) {
            step += stackToStack(record, Action.MTOL, Action.LTOM, leftStack, midStack);
            step += stackToStack(record, Action.LTOM, Action.MTOL, midStack, leftStack);
            step += stackToStack(record, Action.RTOM, Action.MTOR, midStack, rightStack);
            step += stackToStack(record, Action.MTOR, Action.RTOM, rightStack, midStack);
        }
        return step;
    }

    private int stackToStack(Action[] record, Action preAction, Action nowAction, Stack<Integer> from, Stack<Integer> to) {
        if ((record[0] != preAction && from.peek() < to.peek())||to.isEmpty()) {
            to.push(from.pop());
            record[0] = nowAction;
            return 1;
        }
        return 0;
    }

    public enum Action {
        NO, LTOM, MTOL, MTOR, RTOM
    }

    public static void main(String[] args) {
        HanoiTower hanoiTower = new HanoiTower(4);
        System.out.println(hanoiTower.hanoi());
    }
}
