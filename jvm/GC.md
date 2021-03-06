`11 GC
===

垃圾分类
---

    强引用（StrongReference）：暂时不是垃圾，未来不确定
    软引用（SoftReference）：当内存不足，将它当成垃圾回收
    弱引用（WeakReference）：垃圾，由JVM中的垃圾回收器发现并回收
    虚引用 (PhantomReference): 不能单独使用,必须和引用队列一起使用,跟踪对象被回收的状态。
    
垃圾算法
---
 1.引用计数法：
    
    引用计数算法（Reachability Counting）是通过在对象头中分配一个空间来保存该对象被引用的次数（Reference Count）。
    如果该对象被其它对象引用，则它的引用计数加1，如果删除对该对象的引用，那么它的引用计数就减1，当该对象的引用计数为0时，
    那么该对象就会被回收。
    
 2.可达性分析法*：
 
    可达性分析算法（Reachability Analysis）的基本思路是，通过一些被称为垃圾回收根（GC Roots）的对象作为起点，从这些节点开始向下搜索，
    搜索走过的路径被称为引用链（Reference Chain)，当一个对象到 GC Roots 没有任何引用链相连时（即从 GC Roots 节点到该节点不可达），
    则证明该对象是不可用的。
    
    GC ROOT:
        ①虚拟机栈（栈帧中的本地变量表）中引用的对象
        ②方法区中类静态属性引用的对象
        ③方法区中常量引用的对象
        ④本地方法栈中 JNI（即一般说的 Native 方法）引用的对象
        
垃圾回收
---

1.标记-清除法：

    先把内存区域中的这些对象进行标记，哪些属于可回收标记出来，然后把这些垃圾拎出来清理掉。
   
2.复制算法:

    它将可用内存按容量划分为大小相等的两块，每次只使用其中的一块。当这一块的内存用完了，就将还存活着的对象复制到另外一块上面，
    然后再把已使用过的内存空间一次清理掉。
    
3.标记整理(清除)算法:

    标记过程仍然与标记 --- 清除算法一样，但后续步骤不是直接对可回收对象进行清理，而是让所有存活的对象都向一端移动，
    再清理掉端边界以外的内存区域。