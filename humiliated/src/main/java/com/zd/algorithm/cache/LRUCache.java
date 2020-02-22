package com.zd.algorithm.cache;

import java.util.HashMap;
import java.util.Map;

public class LRUCache<K,V> {

    Entry<K,V> head, tail;

    int capacity;
    int size;
    Map<K, Entry<K,V>> cache;

    public static class Entry<K,V> {
        public Entry<K,V> pre;
        public Entry<K,V> next;
        public K key;
        public V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public Entry() {
        }
    }

    /**
     * 如果节点不存在，返回 -1.如果存在，将节点移动到头结点，并返回节点的数据。
     */
    public V get(K key) {
        Entry<K,V> node = cache.get(key);
        if (node == null) {
            return null;
        }
        // 存在移动节点
        moveToHead(node);
        return node.value;
    }

    /**
     * 将节点加入到头结点，如果容量已满，将会删除尾结点
     */
    public void put(K key, V value) {
        Entry<K,V> node = cache.get(key);
        if (node != null) {
            node.value = value;
            moveToHead(node);
            return;
        }
        // 不存在。先加进去，再移除尾结点
        // 此时容量已满 删除尾结点
        if (size == capacity) {
            Entry<K,V> lastNode = tail.pre;
            deleteNode(lastNode);
            cache.remove(lastNode.key);
            size--;
        }
        // 加入头结点

        Entry<K,V>  newNode = new Entry<>();
        newNode.key = key;
        newNode.value = value;
        addNode(newNode);
        cache.put(key, newNode);
        size++;
    }

    private void moveToHead(Entry<K,V> node) {
        // 首先删除原来节点的关系
        deleteNode(node);
        addNode(node);
    }

    private void deleteNode(Entry<K,V> node) {
        node.pre.next = node.next;
        node.next.pre = node.pre;
    }

    private void addNode(Entry<K,V> node) {
        head.next.pre = node;
        node.next = head.next;

        node.pre = head;
        head.next = node;
    }



    public LRUCache(int capacity) {
        this.capacity = capacity;
        // 初始化链表
        initLinkedList();
        size = 0;
        cache = new HashMap<>(capacity + 2);
    }

    private void initLinkedList() {
        head = new Entry<>();
        tail = new Entry<>();

        head.next = tail;
        tail.pre = head;

    }
}
