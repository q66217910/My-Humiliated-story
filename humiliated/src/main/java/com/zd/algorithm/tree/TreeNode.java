package com.zd.algorithm.tree;

import lombok.Data;

@Data
public class TreeNode {

    /**
     * 当前节点值
     */
    private int value;

    /**
     * 左节点
     */
    private TreeNode left;

    /**
     * 右节点
     */
    private TreeNode right;

    public TreeNode(int value) {
        this.value = value;
    }

    public TreeNode left(int value) {
        TreeNode treeNode = new TreeNode(value);
        this.setLeft(treeNode);
        return treeNode;
    }

    public TreeNode right(int value) {
        TreeNode treeNode = new TreeNode(value);
        this.setRight(treeNode);
        return treeNode;
    }

    public TreeNode andLeft(int value) {
        this.setLeft(new TreeNode(value));
        return this;
    }

    public TreeNode andRight(int value) {
        this.setRight(new TreeNode(value));
        return this;
    }
}
