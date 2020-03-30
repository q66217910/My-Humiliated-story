ConcurrentHashMap
===
1.7:
---
    Segment:存放数据时首先需要定位到具体的 Segment 中 (
    

1.8:
---
 CAS + synchronized |h >>> 16)) & HASH_BITS; = -1 ：扩容


                     负载因子与表的容量
                     ---
                     在ConcurrentHashMap中，负载因子默认为0.75.[sizeCtl = n - (n >>> 2)].

                     sizeCtl用与控制表的初始化与扩容

                            0:初始值
                            -1: 表在初  数组+链表+红黑数的数据结构
 
        initTable : 初始化table数组
        hash = (h ^ (始化
       -N: 扩容 （N-1）个活跃线程 
       N : 表容量*负载因子（需要扩容的值） 
       初始化： 初始化时 为 table的容量
```java
 class ConcurrentHashMap{
        
    //表的初始容量,表的容量为2的幂次   
    private static final int DEFAULT_CAPACITY = 16; 
    //用于表的初始化和扩容
    private transient volatile int sizeCtl;
                           
    //得出2的幂次，把最高为的1右移到每一位上,使得最高位右边全是1,然后+1，得到的便是2的幂次
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
当table为null或者table.size时初始化
```java
class ConcurrentHashMap{  

   transient volatile Node<K,V>[] table; 

   private final Node<K,V>[] initTable() {
       Node<K,V>[] tab; int sc;
       while ((tab = table) == null || tab.length == 0) {  
           // sizeCtl<0 说明已经有其他
           if ((sc = sizeCtl) < 0)   
               //让出cpu，使得当前wihle空旋，等待其他线程初始化table
               Thread.yield(); 
           //若table还没初始化，将SIZECTL设置为-1，表明正在初始化
           else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
               try {
                   if ((tab = table) == null || tab.length == 0) { 
                       //设置默认容量16
                       int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                       @SuppressWarnings("unchecked")  
                       //初始化table
                       Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                       table = tab = nt;
                       //需要扩容的值 表容量*负载因子
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

#表的扩容
1.当s(总数)> =sizeCtl时进行扩容

2.resizeStamp:最高位为1，低位为table的最高位

3.如果sizeCtl不小于0，则说明初次扩容
```java
class ConcurrentHashMap{
                        
    //最大容量
    private static final int MAXIMUM_CAPACITY = 1 << 30;  

    private static int RESIZE_STAMP_BITS = 16;

    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;

    //check为binCount，
    private final void addCount(long x, int check) {
        if (check >= 0) {
            Node<K,V>[] tab, nt; int n, sc;  
            //当s(总数)> =sizeCtl时进行扩容
            while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
                   (n = tab.length) < MAXIMUM_CAPACITY) {    
                //n,为2的幂次
                int rs = resizeStamp(n);
                if (sc < 0) {
                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                        sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                        transferIndex <= 0)
                        break;
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }    
                // 如果是sc为正数，上CAS锁，开始扩容，设置 sc的值
                else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                             (rs << RESIZE_STAMP_SHIFT) + 2))   
                    //开始扩容
                    transfer(tab, null);
                s = sumCount();
            }
        }
    }         
          
    //高位为15位1，低位为0的个数
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
