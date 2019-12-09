package com.zd.algorithm.tree;

import lombok.Data;

@Data
public class Node {

    /**
     * 当前节点值
     */
    private int value;

    /**
     * 左节点
     */
    private Node left;

    /**
     * 右节点
     */
    private Node right;

    public Node(int value) {
        this.value = value;
    }

    public Node left(int value) {
        Node node = new Node(value);
        this.setLeft(node);
        return node;
    }

    public Node right(int value) {
        Node node = new Node(value);
        this.setRight(node);
        return node;
    }

    public Node andLeft(int value) {
        this.setLeft(new Node(value));
        return this;
    }

    public Node andRight(int value) {
        this.setRight(new Node(value));
        return this;
    }
}
