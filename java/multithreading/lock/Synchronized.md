# Synchronized



### 1.作用范围

​	

```
1.方法: 对象实例this
2.静态方法: 对class上锁,锁住所有调用这个静态方法的线程
3.obj: 该对象为锁的代码块
```



### 2.核心组件



```
1.WaitSet: 调用wait被阻塞的线程
2.contentionList: 竞争队列,所有请求锁的线程都会被加入该队列
3.EntryList: 竞争队列中有资格成为候选资源的线程（防止大量的contentionList 并发CAS访问）
```

