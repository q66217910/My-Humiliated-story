线程池
===


|                         | corePoolSize | maximumPoolSize   | keepAliveTime | workQueue           | threadFactory        | handler        |
| ----------------------- | ------------ | ----------------- | ------------- | ------------------- | -------------------- | -------------- |
| newCachedThreadPool     | 0            | Integer.MAX_VALUE | 60s           | SynchronousQueue    | defaultThreadFactory | defaultHandler |
| newFixedThreadPool      | n            | n                 | 0s            | LinkedBlockingQueue |                      |                |
| newScheduledThreadPool  | n            | Integer.MAX_VALUE | 0s            | DelayedWorkQueue    |                      |                |
| newSingleThreadExecutor | 1            | 1                 | 0s            | LinkedBlockingQueue |                      |                |



1.参数
---

    corePoolSize：池中保留线程数
    maximumPoolSize： 池中允许最大线程数
    keepAliveTime： 新任务等待最大时间
    workQueue：任务队列
    threadFactory： 线程工厂
    handler： 拒绝策略
    
    ctl：当前线程数+线程池当前状态
        高三位表示线程池的状态：
            111：RUNNING
            000：SHUTDOWN
            001：STOP
            010：TIDYING
            100：TERMINATED
            
            workerCountOf方法用来取低29位的数值，返回线程池的线程数。
            runStateOf方法则用来取高3位的数值，返回当前线程池的状态。

2.线程池执行过程
---

    1.收到一个任务提交，比较正在工作的线程数 与核心线程数
        2.如果核心线程数没跑满，直接运行
    3.否则，判断线程池状态是否在运行状态，并且加入队列
    4.若线程池shutdown，执行拒绝策略
    
    一个work一个线程，从队列里取任务进行消费
### 3.拒绝策略

拒绝策略的触发：

​		1.线程池已shutdown

​		2.队列满了并且当前运行线程数超过线程池允许最大线程数

```
1.AbortPolicy:丢弃任务并抛异常
2.DiscardPolicy：丢弃任务，不做任何操作
3.CallerRunsPolicy: 如果任务被拒绝，若线程池没有关闭，真交由线程处理
4.DiscardOldestPolicy：重新加回队列，重新提交任务
```

