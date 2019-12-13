package com.zd.algorithm.linked;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.*;

@Data
public class LinkedNode {

    private int value;

    private LinkedNode next;

    public LinkedNode(int value) {
        this.value = value;
    }

    public LinkedNode next(LinkedNode node) {
        this.next = node;
        return node;
    }

    public LinkedNode next(int value) {
        LinkedNode node = new LinkedNode(value);
        LinkedNode old = this;
        while (old.next != null) {
            old = old.next;
        }
        old.next = node;
        return this;
    }

    @Override
    public String toString() {
        List<Integer> list = Lists.newArrayList();
        LinkedNode node = this;
        while (node != null) {
            list.add(node.value);
            node = node.next;
        }
        return Arrays.toString(list.toArray());
    }
}

