package com.zd.algorithm.letcode.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AllOne {

    class AllOneNode {

        private AllOneNode next;

        private AllOneNode pre;

        private Set<String> set;

        private Integer val;

        public AllOneNode(Integer val) {
            this.val = val;
            this.set = new HashSet<>();
        }
    }

    private AllOneNode head;

    private AllOneNode tail;

    private Map<String, AllOneNode> map;

    public AllOne() {
        this.map = new HashMap<>();
        this.head = new AllOneNode(-1);
        this.tail = new AllOneNode(-1);
        head.next = tail;
        tail.pre = head;
    }

    public void inc(String key) {
        if (this.map.containsKey(key)) {
            AllOneNode node = map.get(key);
            AllOneNode pre = node.pre;
            node.set.remove(key);
            if (pre == head || pre.val != node.val + 1) {
                //若前面是head,则创建一个新节点
                AllOneNode a = new AllOneNode(node.val + 1);
                a.set.add(key);
                a.next = node;
                node.pre = a;
                a.pre = pre;
                pre.next = a;
                this.map.put(key, a);
            } else {
                //前面不是head,并且是
                pre.set.add(key);
                this.map.put(key, pre);
            }
            if (node.set.size() == 0) {
                //当前节点没数据删除
                AllOneNode n = node.next;
                AllOneNode p = node.pre;
                p.next = n;
                n.pre = p;
            }
        } else {
            AllOneNode pre = this.tail.pre;
            if (pre.val == 1) {
                pre.set.add(key);
                this.map.put(key, pre);
            } else {
                AllOneNode a = new AllOneNode(1);
                a.set.add(key);
                a.next = tail;
                a.pre = pre;
                tail.pre = a;
                pre.next = a;
                this.map.put(key, a);
            }
        }
    }

    public void dec(String key) {
        if (this.map.containsKey(key)) {
            AllOneNode node = map.get(key);
            AllOneNode next = node.next;
            node.set.remove(key);
            if (node.val == 1) {
                //value是1，则删除key
                this.map.remove(key);
            } else if (next.val == node.val - 1) {
                next.set.add(key);
                this.map.put(key, next);
            } else {
                AllOneNode a = new AllOneNode(node.val - 1);
                a.set.add(key);
                node.next = a;
                a.pre = node;
                next.pre = a;
                a.next = next;
                this.map.put(key, a);
            }
            if (node.set.size() == 0) {
                //当前节点没数据删除
                AllOneNode n = node.next;
                AllOneNode p = node.pre;
                p.next = n;
                n.pre = p;
            }
        }
    }

    public String getMaxKey() {
        return head.next.set.stream().findAny().orElse("");
    }

    public String getMinKey() {
        return tail.pre.set.stream().findAny().orElse("");
    }

    public static void main(String[] args) {
        AllOne obj = new AllOne();
        obj.inc("a");
        obj.inc("b");
        obj.inc("b");
        obj.inc("b");
        obj.inc("b");
        obj.dec("b");
        obj.dec("b");
        obj.getMaxKey();
        obj.getMinKey();
    }
}
