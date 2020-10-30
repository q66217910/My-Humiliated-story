package com.zd.algorithm.tree;

import sun.misc.Unsafe;

/**
 * 计算完全二叉树节点数
 */
public class CountCompleteTreeNodes {

    public static void main(String[] args) {
        TreeNode root = new TreeNode(3);
        TreeNode left = new TreeNode(2);
        TreeNode right = new TreeNode(4);
        TreeNode left1 = new TreeNode(1);
        left.setLeft(left1);
        root.setLeft(left);
        root.setRight(right);
        System.out.println(countCompleteTreeNodes(root));
    }

    private static int countCompleteTreeNodes(TreeNode root) {
        //根节点不存在
        if (root == null) {
            return 0;
        }
        //左子树高
        int leftHigh = countChildTreeHigh(root.getLeft());
        int rightHigh = countChildTreeHigh(root.getRight());
        if (leftHigh == rightHigh) {
            //如果左子树与右子数同高，说明左子树是满树,递归右子树
            return (1 << leftHigh) + countCompleteTreeNodes(root.getRight());
        }
        //如果左子树与右子数不同高，说明右子树是满树,递归左子树
        return countCompleteTreeNodes(root.getLeft()) + (1 << rightHigh);
    }

    /**
     * 计算子数的高度(递归计算)
     */
    private static int countChildTreeHigh(TreeNode child) {
        if (child == null) {
            return 0;
        }
        return countChildTreeHigh(child.getLeft()) + 1;
    }

    public int sumNumbers(TreeNode root) {
        if (root == null) {
            return 0;
        }
        return sumNumbers(root, 0);
    }

    private int sumNumbers(TreeNode node, int value) {
        value = value * 10 + node.value;
        if (node.left == null && node.right == null) {
            return value;
        }
        return (node.left != null ? sumNumbers(node.left, value) :
            0) + (node.right != null ? sumNumbers(node.right, value) : 0);
    }
}
