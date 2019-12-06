HashMap
===
1.8中，HashMap是数组+链表+红黑数的数据结构


       DEFAULT_LOAD_FACTOR:负载因子，用于扩容
       
       tableSizeFor：table的大小，2的N次方
       static final int tableSizeFor(int cap) {
               int n = cap - 1;
               n |= n >>> 1;
               n |= n >>> 2;
               n |= n >>> 4;
               n |= n >>> 8;
               n |= n >>> 16;
               return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        }
        
        TREEIFY_THRESHOLD：当链表长度大于这个值（默认8），将链表转为红黑树
        UNTREEIFY_THRESHOLD： 当小于这个值（默认6），将红黑树转回链表
        
        
        
        hash():可以将hashcode高位和低位的值进行混合做异或运算，
        而且混合后，低位的信息中加入了高位的信息，
        这样高位的信息被变相的保留了下来。掺杂的元素多了，
        那么生成的hash值的随机性会增大。
        static final int hash(Object key) {
                int h;
                return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
        }
        
        
        resize():初始化table或者扩容
        因为扩容是2次的幂次扩容，所以，元素的位置要么是在原位置，
        要么是在原位置再移动2次幂的位置。
        
  
哈希碰撞:
---
两个不同的输入值，经过同一散列函数计算出的散列值相同的现象叫做哈希碰撞。

    常见的解决哈希碰撞的方法有如下几种：
    
    开放地址法：一旦发生冲突，就去寻找下一个空的散列地址，只要散列表足够大，空的散列地址就能够找到，并将记录存入。
    链地址法：将哈希表的每个单元作为链表的头结点，所有哈希地址为 i 的元素构成一个同义词链表。即发生冲突时，就把该关键字链放在以该单位为头结点的链表的尾部。
    再哈希法：当哈希地址出现冲突后，用其他函数计算另一个哈希函数的地址，直到冲突不再产生为止。
    建立公共溢出区：将哈希表分为基本表和溢出表两部分，发生冲突的元素都放入溢出表中。
