计算完全二叉数的节点个数
===

    从根节点开始分别判断左右子树的高度：
    
    若左子树高度等于右子树，说明左子树一定为满二叉树，可得左子树的总节点个数，然后递归求右子树的节点数；
    若左子树高度大于右子树，说明右子树一定为满二叉树，可得右子树的总节点个数，然后递归求左子树的节点数
    
    
[code](https://github.com/q66217910/My-Humiliated-story/blob/master/humiliated/src/main/java/com/zd/algorithm/tree/CountCompleteTreeNodes.java)