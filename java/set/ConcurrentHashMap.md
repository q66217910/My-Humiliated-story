ConcurrentHashMap
===
1.7:
---
    Segment:存放数据时首先需要定位到具体的 Segment 中 (
    

1.8:
---
 CAS + synchronized |  数组+链表+红黑数的数据结构
 
        initTable : 初始化table数组
        hash = (h ^ (h >>> 16)) & HASH_BITS; = -1 ：扩容