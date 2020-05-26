# SynchronousQueue

只包含一个元素的队列，可以看做一个点。

### 1.offer()和poll()

​	

```
1.当获取时没有元素，会阻塞，直到插入数据(读)
2.当插入元素时，已存在元素，则阻塞,等到元素被读取(写)
```



### 2.实现TransferStack(LIFO)和TransferQueue(FIFI)



TransferStack：

​	transfer():无论是是put还是get都是调用的transfer方法

```java
 static final class TransferStack<E> extends Transferer<E> {
     //消费者
     static final int REQUEST    = 0;
     //生产者
     static final int DATA       = 1;
     
     //栈的头节点
     volatile SNode head;
     
     E transfer(E e, boolean timed, long nanos) {
         
         	//需要重建/构造的节点
            SNode s = null; 
         	//根据有没有传入数据判断是生产者还是消费者
            int mode = (e == null) ? REQUEST : DATA;

            for (;;) {
                SNode h = head;
                //头节点为空，或者模式相同
                if (h == null || h.mode == mode) {  
                    //默认不等待 transferer.transfer(e, true, 0)
                    if (timed && nanos <= 0) {      
                        if (h != null && h.isCancelled())
                            //pop 当前节点
                            casHead(h, h.next);     
                        else
                            return null;
                        //创建一个节点，并设置为head节点
                    } else if (casHead(h, s = snode(s, e, h, mode))) {
                        //自旋等待（相同操作会执行，这样保证了一次只有一个元素）
                        SNode m = awaitFulfill(s, timed, nanos);
                        if (m == s) {              
                            clean(s);
                            return null;
                        }
                        if ((h = head) != null && h.next == s)
                            casHead(h, s.next);     
                        //或是消费者，返回要消费的对象,生产者返回存储的对象
                        return (E) ((mode == REQUEST) ? m.item : s.item);
                    }
                    //当前节点没处理结束
                } else if (!isFulfilling(h.mode)) { 
                    if (h.isCancelled())    
                        //当前节点完成，切换下一个节点
                        casHead(h, h.next);   
                    //更新为创建的一个已完成节点
                    else if (casHead(h, s=snode(s, e, h, FULFILLING|mode))) {
                        for (;;) { 
                            SNode m = s.next;       
                            if (m == null) {        
                                casHead(s, null);   
                                s = null;           
                                break;             
                            }
                            SNode mn = m.next;
                            if (m.tryMatch(s)) {
                                casHead(s, mn);     
                                return (E) ((mode == REQUEST) ? m.item : s.item);
                            } else                  
                                s.casNext(m, mn);  
                        }
                    }
                } else {
                    //模式不同且前一个节点已处理
                    SNode m = h.next;              
                    if (m == null)                 
                        casHead(h, null);           
                    else {
                        SNode mn = m.next;
                        if (m.tryMatch(h))          
                            casHead(h, mn);        
                        else                       
                            h.casNext(m, mn);      
                    }
                }
            }
        }
 }
```

