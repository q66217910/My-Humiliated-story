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