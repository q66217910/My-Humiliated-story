ConcurrentHashMap
===
1.7:
---
    Segment:存放数据时首先需要定位到具体的 Segment 中 
    

1.8:
---
 CAS + synchronized (数组+链表+红黑数的数据结构)

Hash算法
------

    1.为什么要高16位与低16位异或:
        作用:可以将高低位的二进制特征混合,并且可以减少hash碰撞(hash碰撞指的是两个不同的值经过hash后得到的值相同)
            在HashMap中,使用的链地址法(同样hash值的存在同一个table节点)。
        (n - 1) & h: n为table的大小, (n-1)&h 保证了最终结果会落在table大小中.在这，若h不进行^操作，则只与低位&,
             那只要低位相同则hash就会落在同一个节点,若(h ^ (h >>> 16))后,h中包含了低位与高位的信息,需要他们高位和
             低位都相同才会落在同一个table节点,以此来减少hash碰撞。             


```java
class  ConcurrentHashMap{
    
    Class<?> ak = Node[].class;
    
    //arrayIndexScale获取Node[]中一个元素的大小, ASHIFT即为（31-前置0的个数）剩下的就是, 每一个元素的占用的位数
    //U.arrayIndexScale(ak)这个是数组每一格的指针占用位数
    //Integer.numberOfLeadingZeros(U.arrayIndexScale(ak)) 这个是占用位数用二进制表示的前导0个数
    //31 - Integer.numberOfLeadingZeros(U.arrayIndexScale(ak)) 这个是占用位数用二进制表示的长度
    //类似Array.getLength(Object array)方法
    private static final int ASHIFT = 31 - Integer.numberOfLeadingZeros(U.arrayIndexScale(ak));
    
    //获取Node[]第一个元素的偏移地址
    private static final long ABASE = U.arrayBaseOffset(ak);
    
    //int最大值为(1111...1111)
    static final int HASH_BITS = 0x7fffffff;
    
    //用高16位与低16位异或
    static final int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }
    
    //获取table在这hash位置的节点，ASHIFT每个节点的占用字节数,
    // offset(ABASE) + scale * Array.getLength(array) (scale * i 等于i << ASHIFT等于 i*2^scale)
    static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
        return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
    }
    
    //get方法
    public V get(Object key) {
        Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
        //高位与低位异或
        int h = (key.hashCode());
        //(n - 1) & h [n为2的幂次,所以n-1为000...1..,  &h则是取h低n位的值]
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) {
            if ((eh = e.hash) == h) {
                if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                    return e.val;
            }
            else if (eh < 0)
                return (p = e.find(h, key)) != null ? p.val : null;
            while ((e = e.next) != null) {
                if (e.hash == h &&
                    ((ek = e.key) == key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null;
    }
    
}
```


负载因子与表的容量:
----------
                     
     在ConcurrentHashMap中，负载因子默认为0.75.[sizeCtl = n - (n >>> 2)]. 
        注: 负载因子为什么是0.75? 
               一.降低hash的冲突 (查询时间)
               二.防止哈希表过大,占用过多内存。 (占用内存空间)                                                  

 sizeCtl用与控制表的初始化与扩容:

     -1: 表在初始化            
     -N: 扩容中, （最低位-1）个活跃线程
      N: 表容量*负载因子（达到该值需要扩容） 
```java
 class ConcurrentHashMap{
        
    //表的初始容量,表的容量为2的幂次   
    private static final int DEFAULT_CAPACITY = 16; 
    //用于表的初始化和扩容
    private transient volatile int sizeCtl;
                           
    //得出2的幂次，  把最高为的1右移到每一位上,使得最高位右边全是1,然后+1，得到的便是2的幂次
    private static final int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;    
    }
}
```   

表的初始化
-----
当table为null或者table.size为0时初始化
```java
class ConcurrentHashMap{  

   transient volatile Node<K,V>[] table; 

   private final Node<K,V>[] initTable() {
       Node<K,V>[] tab; int sc;
       while ((tab = table) == null || tab.length == 0) {  
           // sizeCtl<0 说明已经有其他的线程在进行初始化
           if ((sc = sizeCtl) < 0)   
               //让出cpu，使得当前wihle空旋，等待其他线程初始化table
               Thread.yield(); 
           //若table还没初始化，将SIZECTL设置为-1，表明正在初始化
           else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
               try {
                   //在做一次判断,防止重复扩容
                   if ((tab = table) == null || tab.length == 0) { 
                       //设置默认容量16
                       int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                       //初始化table
                       Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                       table = tab = nt;
                       //计算需要扩容的值 表容量*负载因子 0.75
                       sc = n - (n >>> 2);
                   }
               } finally { 
                   //需要扩容的值 表容量*负载因子
                   sizeCtl = sc;
               }
               break;
           }
       }
       return tab;
   }
}
```    

表的扩容
====
1.当s(总数)> =sizeCtl时进行扩容

2.如果sizeCtl不小于0，则说明初次扩容

binCount:

    binCount<0: 不需要扩容
    0<binCount<=1: 只需要检查是否有锁的竞争
```java
class ConcurrentHashMap{
                        
    //最大容量
    private static final int MAXIMUM_CAPACITY = 1 << 30;  

    private static int RESIZE_STAMP_BITS = 16;

    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;

    //check为binCount
    private final void addCount(long x, int check) {
        if (check >= 0) {
            Node<K,V>[] tab, nt; int n, sc;  
            //当s(总数)> =sizeCtl时进行扩容
            while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
                   (n = tab.length) < MAXIMUM_CAPACITY) {    
                //低位为从高位开始到第一个非0时的个数,例如n=16,rs的低位为27（011011）
                int rs = resizeStamp(n);
                // sizeCtl为负数,代表正在扩容
                if (sc < 0) {
                    //
                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                        sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                        transferIndex <= 0)
                        break;
                    //若sizeCtl的值相同,则可以多线程扩容,并将sc+1
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }    
                // 如果是sc为正数，上CAS锁，开始扩容，设置 sc的值
                // sc的值为 resizeStamp的值+2
                else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                             (rs << RESIZE_STAMP_SHIFT) + 2))   
                    //开始扩容
                    transfer(tab, null);
                s = sumCount();
            }
        }
    }         
          
    //第16位为1,低位可看作存储n （16位为1是因为在<<16后能将sc变成负值）
    static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
    }       
          
    //从高位开始到第一个非0时，0的位数
    public static int numberOfLeadingZeros(int i) {
        // HD, Figure 5-6
        if (i == 0)
            return 32;
        int n = 1;
        if (i >>> 16 == 0) { n += 16; i <<= 16; }
        if (i >>> 24 == 0) { n +=  8; i <<=  8; }
        if (i >>> 28 == 0) { n +=  4; i <<=  4; }
        if (i >>> 30 == 0) { n +=  2; i <<=  2; }
        n -= i >>> 31;
        return n;
    }

}
```

开始扩容
----
将原表的数据迁移到新表
```java
class ConcurrentHashMap{
    
    private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
        int n = tab.length, stride;
        //stride ,计算每个cpu需要迁移桶的数量
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
            //默认16个
            stride = MIN_TRANSFER_STRIDE;
        //创建新的表,若是协助扩容的线程，则不需要创建
        if (nextTab == null) {           
            try {
                //扩容一倍
                Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
                nextTab = nt;
            } catch (Throwable ex) {     
                //扩容失败，可能是内存溢出，将sizeCtl设置成最大值，使ConcurrentHashMap不再扩容
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab;
            //扩容时下个表的索引
            transferIndex = n;
            ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
            boolean advance = true;
            boolean finishing = false;
        }
    }
}
```

数据结构
----