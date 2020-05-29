package com.zd.algorithm.stack;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
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
        if (record[0] != preAction && from.peek() < to.peek()) {
            to.push(from.pop());
            record[0] = nowAction;
            return 1;
        }
        return 0;
    }

    public void hanota(List<Integer> A, List<Integer> B, List<Integer> C) {
        hanoi(A.size(), A, B, C);
    }

    private void hanoi(int n, List<Integer> A, List<Integer> B, List<Integer> C) {
        if (n > 0) {
            //先将前n-1个碟子由A通过C移到B
            hanoi(n - 1, A, C, B);
            //将第n个碟子由A移到C
            move(A, C);
            //最后将前n-1个碟子由B通过A移到C
            hanoi(n - 1, B, A, C);
        }
    }

    private void move( List<Integer> A, List<Integer> B) {
        B.add(A.remove(A.size() - 1));
    }


    public enum Action {
        NO, LTOM, MTOL, MTOR, RTOM
    }

    public static void main(String[] args) {
        HanoiTower hanoiTower = new HanoiTower(4);
        hanoiTower.hanota(Lists.newArrayList(1, 2, 3)
                , Lists.newArrayList(), Lists.newArrayList());
    }
}
