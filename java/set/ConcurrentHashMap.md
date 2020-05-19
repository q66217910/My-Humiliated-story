







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
    
    //链表转红黑树的数量
    static final int TREEIFY_THRESHOLD = 8;
    
    //用高16位与低16位异或
    static final int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }
    
    //获取table在这hash位置的节点，ASHIFT每个节点的占用字节数,
    // offset(ABASE) + scale * Array.getLength(array) (scale * i 等于i << ASHIFT等于 i*2^scale)
    //其实就是i*指针大小,若指针大小为64则，i*64=i<<8
    static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
        return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
    }
    
    static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                        Node<K,V> c, Node<K,V> v) {
        return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
    }
    
    static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v) {
        U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
    }
    
    //get方法
    public V get(Object key) {
        Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
        //高位与低位异或
        int h = (key.hashCode());
        //(n - 1) & h [n为2的幂次,所以n-1为000...1..,  &h则是取h低n位的值]
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) {
            //hash和取出节点相同,则就是当前的值
            if ((eh = e.hash) == h) {
                if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                    return e.val;
            }
            //hash结构小于0,为红黑树
            else if (eh < 0)
                return (p = e.find(h, key)) != null ? p.val : null;
            //hash值不同,但不为负,说明是链表
            while ((e = e.next) != null) {
                if (e.hash == h &&
                    ((ek = e.key) == key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null;
    }
    
    //put方法
    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException();
        //高位与低位异或
        int hash = spread(key.hashCode());
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                //table不存在，初始化table
                tab = initTable();
            //当前hash要的node节点不存在
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                //保存一个新的节点
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;                  
            }
            //当前节点在扩容
            else if ((fh = f.hash) == MOVED)
                //帮助扩容
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                //对当前节点上锁
                synchronized (f) {
                    //判断当前节点是否正常
                    if (tabAt(tab, i) == f) {
                        //hash值>0,说明是状态正常
                        if (fh >= 0) {
                            //链表节点数量
                            binCount = 1;
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                //若是当前链表节点,则更新值
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    //若存在则不修改值
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                Node<K,V> pred = e;
                                //若最后都没有找到，则新建一个节点
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        //若节点是红黑树
                        else if (f instanceof TreeBin) {
                            Node<K,V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                //若链表节点数量不为0
                if (binCount != 0) {
                    //若链表节点数量大于8将链表转换为红黑树
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount);
        return null;
    }
}
```

数据结构
----
 ConcurrentHashMap中一共两种数据结构:链表、红黑树

    链表:当节点数量小于8时
    红黑树: 当节点数量大于8时会由链表转红黑树,数量小于6会由红黑树转为链表
    注: 为什么是8和6？
        因为红黑树的查询时间复杂度是O(log(n))、链表的平均时间复杂度为O(n/2),长度为8时,log8=3，n/2=4,红黑树更快.
        而在6转回链表,是为了在链表和红黑树之间转换过于频繁。 

 红黑树中key的顺序

    1.根据hashcode判断,因为是二叉搜索树所以顺序按照当前节点比左节点大，比右节点小。
        (hashcode小了找左子树,大了找右子树)
    2.当hashcode相同并且 equels方法得出结果相同或者对象指针相同，则代表是当前节点
    3.当hashcode相同但是equels并不等,则根据Comparable方法，比较当前节点key与查询key
       的比值。
    4.若Comparable得出的结果还相同,比较原始的hashcode(System.identityHashCode())
    5.若还相等则默认插入左子节点

```java
class ConcurrentHashMap{
    
    //最小出现红黑树的table大小
    static final int MIN_TREEIFY_CAPACITY = 64;
    
    //节点,用作链表，红黑树节点继承它
    static class Node<K,V> implements Map.Entry<K,V> {
        //key的hash值
        final int hash;
        //key
        final K key;
        //value
        volatile V val;
        //链表的下一个节点
        volatile Node<K,V> next;

        Node(int hash, K key, V val, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        public final K getKey()       { return key; }
        public final V getValue()     { return val; }
        public final int hashCode()   { return key.hashCode() ^ val.hashCode(); }
        public final String toString(){ return key + "=" + val; }
        public final V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public final boolean equals(Object o) {
            Object k, v, u; Map.Entry<?,?> e;
            return ((o instanceof Map.Entry) &&
                    (k = (e = (Map.Entry<?,?>)o).getKey()) != null &&
                    (v = e.getValue()) != null &&
                    (k == key || k.equals(key)) &&
                    (v == (u = val) || v.equals(u)));
        }

        /**
         * 链表的查找
         */
        Node<K,V> find(int h, Object k) {
            Node<K,V> e = this;
            if (k != null) {
                do {
                    K ek;
                    if (e.hash == h &&
                        ((ek = e.key) == k || (ek != null && k.equals(ek))))
                        return e;
                } while ((e = e.next) != null);
            }
            return null;
        }
    }
    
    //红黑树节点
    static final class TreeNode<K,V> extends Node<K,V> {
        //父节点
        TreeNode<K,V> parent;
        //左节点
        TreeNode<K,V> left;
        //右节点
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    
        boolean red;

        TreeNode(int hash, K key, V val, Node<K,V> next,
                 TreeNode<K,V> parent) {
            super(hash, key, val, next);
            this.parent = parent;
        }

        Node<K,V> find(int h, Object k) {
            return findTreeNode(h, k, null);
        }

        /**
         *  红黑树的查找
         */
        final TreeNode<K,V> findTreeNode(int h, Object k, Class<?> kc) {
            if (k != null) {
                TreeNode<K,V> p = this;
                do  {
                    //ph当前节点的hash
                    int ph, dir; K pk; TreeNode<K,V> q;
                    TreeNode<K,V> pl = p.left, pr = p.right;
                    if ((ph = p.hash) > h)
                        p = pl;
                    else if (ph < h)
                        p = pr;
                    else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
                        return p;
                    else if (pl == null)
                        p = pr;
                    else if (pr == null)
                        p = pl;
                    else if ((kc != null ||
                              (kc = comparableClassFor(k)) != null) &&
                             (dir = compareComparables(kc, k, pk)) != 0)
                        p = (dir < 0) ? pl : pr;
                    else if ((q = pr.findTreeNode(h, k, kc)) != null)
                        return q;
                    else
                        p = pl;
                } while (p != null);
            }
            return null;
        }
    }
    
    //链表转红黑树
    private final void treeifyBin(Node<K,V>[] tab, int index) {
        Node<K,V> b; int n, sc;
        if (tab != null) {
            //若table大小小于64
            if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
                //table扩容,说明table小并且冲突严重
                tryPresize(n << 1);
            else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
                //对当前node上锁
                synchronized (b) {
                    if (tabAt(tab, index) == b) {
                        //将链表转化为双向链表
                        //hd:root节点,tl上一个节点
                        TreeNode<K,V> hd = null, tl = null;
                        for (Node<K,V> e = b; e != null; e = e.next) {
                            TreeNode<K,V> p =
                                new TreeNode<K,V>(e.hash, e.key, e.val,
                                                  null, null);
                            if ((p.prev = tl) == null)
                                hd = p;
                            else
                                tl.next = p;
                            tl = p;
                        }
                        //new TreeBin():将双向链表转化为红黑树
                        setTabAt(tab, index, new TreeBin<K,V>(hd));
                    }
                }
            }
        }
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
---
扩容的触发：

```
1.sizeCtl(当前size>=sizeCtl,sizeCtl为 table*负载因子)
2.当table.length<64,并且出现单个链表长度大于8,说明table太小并且hash冲突严重，这时候扩容而不是链表转化成红黑树。
```

binCount:

    binCount<0: 不需要扩容
    0<binCount<=1: 只需要检查是否有锁的竞争
    binCount:(链表时表示为链表节点个数，红黑树恒为2) 
CounterCell:

```
	节点数量计数,相当于LongAdder,可以看做是一个AtomicLong,是将值拆分存储,减少写时资源竞争。
```

```java
class ConcurrentHashMap{
                        
    //最大容量
    private static final int MAXIMUM_CAPACITY = 1 << 30;  

    private static int RESIZE_STAMP_BITS = 16;

    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;

    //check为binCount
    private final void addCount(long x, int check) {
        //CounterCell 计数器，相当于LongAdder
        CounterCell[] as; long b, s;
        if ((as = counterCells) != null ||
            !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            CounterCell a; long v; int m;
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
                !(uncontended =
                  U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
                fullAddCount(x, uncontended);
                return;
            }
            if (check <= 1)
                return;
            s = sumCount();
        }
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
                        //协助扩容,nt:新table，协助将老数据迁入新表
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
            //传输标识节点,在传输过程加入头节点,使其他线程读取当前节点时，hash标识为MOVE
            ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
            boolean advance = true;
            boolean finishing = false;
           //i:代表table的第几个节点 
           //bound: 处理的边界,处理到这为止 
           //stride:当次处理的个数
           for (int i = 0, bound = 0;;) {
            Node<K,V> f; int fh;
            //可以看作领取任务，获取i为分配的任务起始点，bound为任务的终点
            //stride为要处理的任务数，处理完了会重新领取stride个   
            while (advance) {
                int nextIndex, nextBound;
                //当前所在节点-1 还大于
                if (--i >= bound || finishing)
                    advance = false;
                //无可以领取的任务
                else if ((nextIndex = transferIndex) <= 0) {
                    i = -1;
                    advance = false;
                }
                //领取任务
                else if (U.compareAndSwapInt
                         (this, TRANSFERINDEX, nextIndex,
                          nextBound = (nextIndex > stride ?
                                       nextIndex - stride : 0))) {
                    bound = nextBound;
                    i = nextIndex - 1;
                    advance = false;
                }
            }
            //i的值不在table范围了
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                //判断finishing标识
                if (finishing) {
                    //如果迁移结束了,将nextTable置空,当前table设置为新的table,并重新设置sizeCtl值
                    nextTable = null;
                    table = nextTab;
                    sizeCtl = (n << 1) - (n >>> 1);
                    return;
                }
                //如果标识没设置为结束,先更新下SIZECTL的值,线程数-1，
                //直到线程为1,表示只有当前线程了，其他线程都已经处理结束了
                //可以设置finishing表示
                if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                        return;
                    finishing = advance = true;
                    i = n;
                }
            }
            //当前节点为空
            else if ((f = tabAt(tab, i)) == null)
                //将原table当前节点设置为迁移节点，设置成功并重新领取任务
                advance = casTabAt(tab, i, null, fwd);
            else if ((fh = f.hash) == MOVED)
                //当前节点已经是迁移节点，重新去领取任务
                advance = true; 
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K,V> ln, hn;
                        //hash值大于0，是链表
                        if (fh >= 0) {
                            int runBit = fh & n;
                            Node<K,V> lastRun = f;
                            //判断hash值，因为会出现所以hash在扩容后或者扩容前的链表
                            for (Node<K,V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            }
                            else {
                                hn = lastRun;
                                ln = null;
                            }
                            for (Node<K,V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash; K pk = p.key; V pv = p.val;
                                //(ph & n) == 0 代表在扩容前的链表，否则在扩容后
                                // 因为扩容后table.length为2n 
                                if ((ph & n) == 0)
                                    ln = new Node<K,V>(ph, pk, pv, ln);
                                else
                                    hn = new Node<K,V>(ph, pk, pv, hn);
                            }
                            //设置链表
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            //将原节点设置为迁移中
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                        else if (f instanceof TreeBin) {
                            //红黑树迁移
                            //同链表差不多，先按照hash值转化成两个双向链表，双向链表再生成红黑树
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> lo = null, loTail = null;
                            TreeNode<K,V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
                            for (Node<K,V> e = t.first; e != null; e = e.next) {
                                int h = e.hash;
                                TreeNode<K,V> p = new TreeNode<K,V>
                                    (h, e.key, e.val, null, null);
                                if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null)
                                        lo = p;
                                    else
                                        loTail.next = p;
                                    loTail = p;
                                    ++lc;
                                }
                                else {
                                    if ((p.prev = hiTail) == null)
                                        hi = p;
                                    else
                                        hiTail.next = p;
                                    hiTail = p;
                                    ++hc;
                                }
                            }
                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                                (hc != 0) ? new TreeBin<K,V>(lo) : t;
                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                                (lc != 0) ? new TreeBin<K,V>(hi) : t;
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                    }
                }
            }
        }
    }
}
```

红黑树
---
```java
static final class TreeBin<K,V> extends Node<K,V> {
        TreeNode<K,V> root;
        volatile TreeNode<K,V> first;
        volatile Thread waiter;
        volatile int lockState;
        // values for lockState
        static final int WRITER = 1; // set while holding write lock
        static final int WAITER = 2; // set when waiting for write lock
        static final int READER = 4; // increment value for setting read lock

        /**
         * Tie-breaking utility for ordering insertions when equal
         * hashCodes and non-comparable. We don't require a total
         * order, just a consistent insertion rule to maintain
         * equivalence across rebalancings. Tie-breaking further than
         * necessary simplifies testing a bit.
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }

        /**
         * Creates bin with initial set of nodes headed by b.
         */
        TreeBin(TreeNode<K,V> b) {
            super(TREEBIN, null, null, null);
            this.first = b;
            TreeNode<K,V> r = null;
            for (TreeNode<K,V> x = b, next; x != null; x = next) {
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                if (r == null) {
                    x.parent = null;
                    x.red = false;
                    r = x;
                }
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K,V> p = r;;) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                  (kc = comparableClassFor(k)) == null) ||
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);
                            TreeNode<K,V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            r = balanceInsertion(r, x);
                            break;
                        }
                    }
                }
            }
            this.root = r;
            assert checkInvariants(root);
        }

        /**
         * Acquires write lock for tree restructuring.
         */
        private final void lockRoot() {
            if (!U.compareAndSwapInt(this, LOCKSTATE, 0, WRITER))
                contendedLock(); // offload to separate method
        }

        /**
         * Releases write lock for tree restructuring.
         */
        private final void unlockRoot() {
            lockState = 0;
        }

        /**
         * Possibly blocks awaiting root lock.
         */
        private final void contendedLock() {
            boolean waiting = false;
            for (int s;;) {
                if (((s = lockState) & ~WAITER) == 0) {
                    if (U.compareAndSwapInt(this, LOCKSTATE, s, WRITER)) {
                        if (waiting)
                            waiter = null;
                        return;
                    }
                }
                else if ((s & WAITER) == 0) {
                    if (U.compareAndSwapInt(this, LOCKSTATE, s, s | WAITER)) {
                        waiting = true;
                        waiter = Thread.currentThread();
                    }
                }
                else if (waiting)
                    LockSupport.park(this);
            }
        }

        /**
         * Returns matching node or null if none. Tries to search
         * using tree comparisons from root, but continues linear
         * search when lock not available.
         */
        final Node<K,V> find(int h, Object k) {
            if (k != null) {
                for (Node<K,V> e = first; e != null; ) {
                    int s; K ek;
                    if (((s = lockState) & (WAITER|WRITER)) != 0) {
                        if (e.hash == h &&
                            ((ek = e.key) == k || (ek != null && k.equals(ek))))
                            return e;
                        e = e.next;
                    }
                    else if (U.compareAndSwapInt(this, LOCKSTATE, s,
                                                 s + READER)) {
                        TreeNode<K,V> r, p;
                        try {
                            p = ((r = root) == null ? null :
                                 r.findTreeNode(h, k, null));
                        } finally {
                            Thread w;
                            if (U.getAndAddInt(this, LOCKSTATE, -READER) ==
                                (READER|WAITER) && (w = waiter) != null)
                                LockSupport.unpark(w);
                        }
                        return p;
                    }
                }
            }
            return null;
        }

        //put时调用
        final TreeNode<K,V> putTreeVal(int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph; K pk;
                if (p == null) {
                    //若没有根节点创建新节点
                    first = root = new TreeNode<K,V>(h, k, v, null, null);
                    break;
                }
                //左节点
                else if ((ph = p.hash) > h)
                    dir = -1;
                //右节点
                else if (ph < h)
                       dir = 1;
                //当前节点
                else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
                    return p;
                //若hashcode相同，但是equels不等
                //k不是实现Comparable类或者实现了当前节点的key与插入节点的key相等
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                             (q = ch.findTreeNode(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.findTreeNode(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                //若左节点或者右节点
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    TreeNode<K,V> x, f = first;
                    //创建一个子节点
                    first = x = new TreeNode<K,V>(h, k, v, f, xp);
                    if (f != null)
                        f.prev = x;
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    //设置是红黑节点,父节点是黑色，当前节点是红色
                    if (!xp.red)
                        x.red = true;
                    else {
                        //对跟节点上锁
                        lockRoot();
                        try {
                            //旋转树，达到平衡
                            root = balanceInsertion(root, x);
                        } finally {
                            unlockRoot();
                        }
                    }
                    break;
                }
            }
            assert checkInvariants(root);
            return null;
        }

        /**
         * Removes the given node, that must be present before this
         * call.  This is messier than typical red-black deletion code
         * because we cannot swap the contents of an interior node
         * with a leaf successor that is pinned by "next" pointers
         * that are accessible independently of lock. So instead we
         * swap the tree linkages.
         *
         * @return true if now too small, so should be untreeified
         */
        final boolean removeTreeNode(TreeNode<K,V> p) {
            TreeNode<K,V> next = (TreeNode<K,V>)p.next;
            TreeNode<K,V> pred = p.prev;  // unlink traversal pointers
            TreeNode<K,V> r, rl;
            if (pred == null)
                first = next;
            else
                pred.next = next;
            if (next != null)
                next.prev = pred;
            if (first == null) {
                root = null;
                return true;
            }
            if ((r = root) == null || r.right == null || // too small
                (rl = r.left) == null || rl.left == null)
                return true;
            lockRoot();
            try {
                TreeNode<K,V> replacement;
                TreeNode<K,V> pl = p.left;
                TreeNode<K,V> pr = p.right;
                if (pl != null && pr != null) {
                    TreeNode<K,V> s = pr, sl;
                    while ((sl = s.left) != null) // find successor
                        s = sl;
                    boolean c = s.red; s.red = p.red; p.red = c; // swap colors
                    TreeNode<K,V> sr = s.right;
                    TreeNode<K,V> pp = p.parent;
                    if (s == pr) { // p was s's direct parent
                        p.parent = s;
                        s.right = p;
                    }
                    else {
                        TreeNode<K,V> sp = s.parent;
                        if ((p.parent = sp) != null) {
                            if (s == sp.left)
                                sp.left = p;
                            else
                                sp.right = p;
                        }
                        if ((s.right = pr) != null)
                            pr.parent = s;
                    }
                    p.left = null;
                    if ((p.right = sr) != null)
                        sr.parent = p;
                    if ((s.left = pl) != null)
                        pl.parent = s;
                    if ((s.parent = pp) == null)
                        r = s;
                    else if (p == pp.left)
                        pp.left = s;
                    else
                        pp.right = s;
                    if (sr != null)
                        replacement = sr;
                    else
                        replacement = p;
                }
                else if (pl != null)
                    replacement = pl;
                else if (pr != null)
                    replacement = pr;
                else
                    replacement = p;
                if (replacement != p) {
                    TreeNode<K,V> pp = replacement.parent = p.parent;
                    if (pp == null)
                        r = replacement;
                    else if (p == pp.left)
                        pp.left = replacement;
                    else
                        pp.right = replacement;
                    p.left = p.right = p.parent = null;
                }

                root = (p.red) ? r : balanceDeletion(r, replacement);

                if (p == replacement) {  // detach pointers
                    TreeNode<K,V> pp;
                    if ((pp = p.parent) != null) {
                        if (p == pp.left)
                            pp.left = null;
                        else if (p == pp.right)
                            pp.right = null;
                        p.parent = null;
                    }
                }
            } finally {
                unlockRoot();
            }
            assert checkInvariants(root);
            return false;
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        //平衡红黑树
        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            x.red = true;
            //xp:父节点
            //xpp:祖父节点
            //xppl/xppr：叔叔节点
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                //1.当前节点父节点为null，当前节点为黑色
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                //2.父节点为黑色节点，且祖父节点为null，则返回原先的root节点
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                //3.查看叔叔节点
                //(若叔叔节点是红色,把叔叔节点/父节点设置为黑色,祖父节点为红色)
                //（若叔叔节点是黑色，父节点是右节点左旋，是左节点右旋）
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        //把节点置到祖父节点，接着循环，直到root返回
                        x = xpp;
                    }
                    else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;)  {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                            (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                    null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                            (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                    null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * Recursive invariant check
         */
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                tb = t.prev, tn = (TreeNode<K,V>)t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }

        private static final sun.misc.Unsafe U;
        private static final long LOCKSTATE;
        static {
            try {
                U = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TreeBin.class;
                LOCKSTATE = U.objectFieldOffset
                    (k.getDeclaredField("lockState"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    } 
```