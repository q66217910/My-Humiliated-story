# Thread

## 1.线程的生命周期

1. **NEW:** 初始状态，线程被构建，但未调用start方法 (  Thread t = new Thread())

2. **RUNNABLE:**   就绪状态，线程进入就绪状态，等待CPU调度执行 （ start();）

3. **RUNNING:**  运行状态，CPU开始调度RUNNABLE状态的线程，线程开始真正执行

4. **Terminated:**  死亡状态，线程执行完，或者异常退出了
   （run、call方法结束,调用stop方法(此方法不安全已经过时)）

5. **BLOCKED:**  阻塞状态， RUNNING状态的线程暂时放弃了CPU的使用权，变成BLOCKED状态
            ，直到再次进入RUNNABLE状态，再次等待CPU调度。
      **同步阻塞：** 获取synchronized锁失败

6. **WAITING/TIMED_WAITING:**  等待状态,wait方法、sleep()、join()、IO请求

   



## 2.线程的控制

1. **sleep():**  sleep方法是Thread类方法，所以在任何情况下调用，会暂停当前线程（RUNNING->BLOCKED）,
   			**不会释放任何锁资源**。sleep方法自带时间，时间过后，Thread会被自动唤醒，或者通过interrupt()方			法来中断。

2. **wait():**  wait方法是Object类的方法，所以**wait方法只能在synchronized block中使用**，会释放
               synchronized加在Object上的锁。wait的唤醒需要notify() 和 notifyAll()方法，
               唤醒特定wait在Object   上的线程。

3. **join():**   join方式是Thread类的方法，用于将当前线程等待子线程执行结束再执行，把线程变成同步等待。
                 join的底层也是调用了wait方法，所以join方法被synchronized所修饰。

   ```java
   //阻塞当前线程，直到调用join的线程执行完成的线程执行完成
   public final synchronized void join(long millis) throws InterruptedException {
           long base = System.currentTimeMillis();
           long now = 0;
           if (millis < 0) {
               //最少等待0ms
               throw new IllegalArgumentException("timeout value is negative");
           }
   
           if (millis == 0) {
               //立即执行
               while (isAlive()) {
                   //活跃线程，直接调用wait方法
                   wait(0);
               }
           } else {
               while (isAlive()) {
                   //延迟wait
                   long delay = millis - now;
                   if (delay <= 0) {
                       break;
                   }
                   wait(delay);
                   now = System.currentTimeMillis() - base;
               }
           }
       }
   ```

4.  **yield():**  yield方法是Thread类方法,用于让出自己的时间片给其他线程，当前线程转为RUNNABLE
            （RUNNING->RUNNABLE）,等待时间片的分配。

5. **Daemon():**  守护线程，在后台运行。主线程不会等待他退出才退出。

6.  **stop():**  立即中断线程 （RUNNING->Terminated）, 已过时。

7.  **interrupt():**  设置标记位，处于阻塞状态的线程（sleep、join、wait）会抛异常。正常运行的线程
                 不受任何影响，需要Thread.interrupted()进行判断处理。

8.  **park()**  ： 挂起线程，通过二元信号量实现阻塞。通过unpark唤醒。



## 3. Synchronized实现

- **JAVA对象头：** 

  1. Mark Word： 默认存储对象的hashCode，分代年龄，锁类型，锁标记位

     ```
     锁标记位:
     	01(无锁状态): 无锁、偏向锁 (锁类型来判断，0:无锁，1：偏向锁)
     	00: 轻量级锁
     	10: 重量级锁 
     ```

  2. Class Metadata Address： 类型指针，指向对象的类元数据

- **Monitor：**

  ​    可重入 , 非公平锁
  
  1. 访问对象同步代码
  2. 进入EntryList（锁池）
  3. 当线程获取到Monitor对象后，将Monitor中的_owner设置成当前线程，_count++
4. 执行完毕，或者调用了wait方法。释放持有的Monitor，owner设置成NULL，count--
  5. 若是调用了wait方法，当前线程会进入WaitSet。（等待唤醒Monitor对象存储在对象的对象头中）

  ```c
    ObjectMonitor() {
      _header       = NULL;
      _count        = 0; //获取锁的线程数
      _waiters      = 0,
      _recursions   = 0;
      _object       = NULL;
      _owner        = NULL;  //持用objectMonitor的线程
      _WaitSet      = NULL;  //等待池
      _WaitSetLock  = 0 ;
      _Responsible  = NULL ;
      _succ         = NULL ;
      _cxq          = NULL ;
      FreeNext      = NULL ;
      _EntryList    = NULL ; //锁池
      _SpinFreq     = 0 ;
      _SpinClock    = 0 ;
      OwnerIsThread = 0 ;
      _previous_owner_tid = 0;
    }
  
  ```

#### 锁优化：

-  **自适应自旋锁:**  在一个锁对象上，有一个线程通过自旋获取到锁，JVM会认为自旋获取到锁的机会很大，会增加自旋的次数，反之减少自旋次数(preBlockSpin)
-  **锁消除:**   JIT 编译时去除不可能存在竞争的锁
-  **锁粗化:**  通过扩大加锁的范围，避免反复加锁和释放锁



#### 锁升级

- **无锁:**  锁消除的情况
- **偏向锁：**  **当一个线程获取到锁时**，进入偏向锁模式。Mark Word的ThreadID为当前线程。之后该线程再访问同步块时不需要CAS操作来加锁和解锁(CAS修改对象头中存储当前线程的ID)。当一个线程获取到锁后会在对象头里替换偏向锁的线程id。
- **轻量级锁:**  **当发生锁竞争时**，进入轻量锁模式。
  1. 线程进入同步代码时，若对象头锁状态为 01（无锁状态），当前栈帧建立一个Lock Record（锁记录），用于存储对象目前的 Mark Word 的拷贝。
  2. 将对象头的Mark Word拷贝到栈帧的Lock Record
  3. 使用CAS操作尝试将对象的Mark Word 更新为指向 Lock Record 的指针，并将 Lock Record 里的 owner 指针指向对象的 Mark Word
  4. 若设置成功，目前线程就获取到了锁，将对象的 Mark Word 设置为00（轻量级锁）
  5. 若设置失败，（自旋获取锁，自适应自旋锁）。自旋很少获取到该锁，锁冲突严重，会直接阻塞线程，轻量级锁膨胀为重量级锁，锁标志的状态变为"10"，Mark Word 中存储的就变为指向重量级锁的指针，当前线程便尝试使用自旋来获取锁，而后面等待锁的线程要进入阻塞状态。
  6. 解锁，通过 CAS 操作尝试把线程栈帧中复制的Displaced Mark Word 替换到对象当前的 Mark Word
  7. 替换失败，说明由其他线程尝试获取该锁(此时锁已经膨胀为重量级锁)，在释放锁的同时，唤醒被挂起的线程。
-  **重量级锁:** 当锁竞争严重，**轻量级锁自旋很少能获取到锁**，进入重量级锁模式。
  1. 若monitor的_count为0，则该线程获取到锁，_count++，当前线程为monitor的拥有者。
  2. 若还是当前线程进入count++（以此实现可重入）
  3. 若monitor占用失败，线程进入阻塞状态，直到monitor的count为0，再重新尝试获取锁。





## 4. 线程池

#### 线程池参数

-  **corePoolSize：** 核心线程数（空闲状态保持的最大线程数）

-  **maximumPoolSize:**  最大线程数 （线程池允许最大线程数）

-  **keepAliveTime/unit:**  当线程数大于corePoolSize时，多余线程空闲多久被回收。

-  **workQueue:**  工作队列（线程数达到corePoolSize时，任务会加入队列）

  ```
  LinkedBlockingQueue: 无边界
  ArrayBlockingQueue: 有边界
  ```

-  **threadFactory：**  线程工厂 （用于生产work线程）

  ```
  默认的线程会设置成非守护线程，分配默认的优先级（5）。
  线程的优先级:
  	public final static int MIN_PRIORITY = 1; //最低优先级
  	public final static int NORM_PRIORITY = 5; //默认优先级
  	public final static int MAX_PRIORITY = 10; //最大优先级 
  work线程设置为非守护线程：
  	若为守护线程,主线程会退出,持有线程池的线程退出后,失去GC ROOT,线程池可能会被回收
  ```

-  **handler：**  拒绝策略 （当队列满了，线程数量达到maximumPoolSize，还有任务进来时执行）

  ```
  AbortPolicy: 全部抛RejectedExecutionException异常
  CallerRunsPolicy: 若线程池没有shutdown,直接开启线程运行
  DiscardOldestPolicy: 重新加回队列
  DiscardPolicy: 直接丢弃
  ```



#### 线程池执行

1. workcount<corePoolSize,直接启动一个新线程执行。
2. 若workcount>=corePoolSize，则把任务加入队列。
3. 若添加队列失败(队列满了)，则判断workcount<maximumPoolSize,启动一个新线程执行。
4. 若workcount>=maximumPoolSize,执行拒绝策略。

```java
//ctl, 前三位代表线程池状态,后面表示当前运行线程数workcount
//111：RUNNING (运行状态)
//000：SHUTDOWN (不接受任务,但是处理任务)
//001：STOP (不接收任务，不处理任务,中断进行中的任务)
//010：TIDYING (所有的任务都中止,workcount为0)
//100：TERMINATED (已关闭线程池)
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

//执行某个线程任务
public void execute(Runnable command) {
    	//任务不能为空
        if (command == null)
            throw new NullPointerException();
        int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) {
            //workcount<corePoolSize,直接启动一个新线程执行
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
    	//若workcount>=corePoolSize，则把任务加入队列
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
    	//若添加队列失败(队列满了)，则判断workcount<maximumPoolSize,启动一个新线程执行。
        else if (!addWorker(command, false))
            //若workcount>=maximumPoolSize,执行拒绝策略。
            reject(command);
    }
```



#### 线程池的worker线程的工作原理

1. 先解锁释放资源，防止中断
2. 自选从工作队列中获取任务
3. 进行加锁操作
4. 如果线程池停止，确保线程中断
5. 执行beforeExecute
6. 执行任务的run方法
7. 执行afterExecute
8. 释放锁

```java
private final class Worker extends AbstractQueuedSynchronizer implements Runnable{
    
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        //解锁，允许线程中断
        w.unlock();
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {
                w.lock();
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    //任务执行前
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        //开始执行任务
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        //任务执行后
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }
    
}
```



#### 线程池的异常处理

- **直接在Runnable任务中try catch**
- **重写线程池的afterExecute方法**
- **实现Thread.UncaughtExceptionHandler接口和继承ThreadGroup**
- **采用Future模式(pool.sumbit)**



#### 线程池核心数的选择

 *Nthreads = Ncpu \* (1+w/c)*  （w: *阻塞时间*  ,c: *计算时间* ）

1.  **CPU密集型：** cpu核心数+1，若是计算密集型 （io时间短于计算时间）w/c<1
2.  **IO密集型：**  cpu核数*2   , 若是io密集型 w/c>1



#### 为什么创建线程开销大

线程的创建需要进行，**分配内存**、 **列入调度** 、 **内存换页** 、 **清空缓存**和**重新读取** 。因为线程的创建和销毁，开销比较大所以，采用池化技术（**线程池**）

1. **内存开销：** java一个线程栈默认大小1M。
2. **调度资源：** 频繁申请/销毁资源和调度资源 



## 5.volatile

- **可见性:** 线程对volatile修饰的变量修改，会立即被其他线程感知
- **禁止指令重排:**  在jvm编译器和cpu中，有时候会为了优化效率会对正常的操作指令进行重新排序，volatile变量会禁止指令重排序。 



#### 可见性

1. **为什么会有可见性问题(CPU多级缓存)：**

    cpu的运行非常快，而磁盘的io却很慢，于是产生了内存。但即使有了内存，CPU的运算速度和内存的读写依旧有1：100的差距，所以给CPU设计了三级缓存与寄存器，为了方便CPU更快的访问内存。由于有分级缓存，会造成内存脏读的情况。
   **@Sun.misc.Contended可以使两个对象不存在一个内存行**

2. **volatile的作用(Lock前缀的指令):**

    volatile可以被修饰的变量，修改的数据行直接写回主存，使其他CPU里缓存了该内存地址的数据无效

3. **缓存一致性(总线嗅探):**

   在多核的情况下，就需要总线嗅探机制。**每个CPU通过嗅探在总线上传播的数据来检查自己缓存的值是否过期**，当其他cpu嗅探到自己相应的缓存行被修改，会令该缓存行失效。该CPU再操作时会从主存再次读取。



#### 禁止指令重排

1. **什么是指令重排：**

   CPU和编译器为了提升程序执行的效率，允许进行指令优化，语句中没有数据依赖关系，便可以重排。

2. **什么是happens-before：**

    程序顺序规则:  一个线程中的每个操作 happens-before 该线程中任意的后续操作 

    监视器锁规则:  对一个线程的解锁 happens-before 于随后该线程或者其它线程对这个对象的加锁 

    volatile 变量规则 :  对一个 volatile 域的写 happens-before 于任意后续对这个 volatile 域的读 

    传递性规则：  如果 A happens-before B, B happens-before C 那么 A happens-before C 

3.  **如何阻止指令重排(内存屏障):**

   1.  在每个volatile写操作的**前面**插入一个**StoreStore屏障**(禁止上面的普通写和下面的volatile写重排序)
   2.  在每个volatile写操作的**后面**插入一个**StoreLoad屏障**(防止上面的volatile写与下面可能有的volatile读/写重排序 )
   3.  在每个volatile读操作的**后面**插入一个**LoadLoad屏障**( 禁止下面所有的普通读操作和上面的volatile读重排序 )
   4.  在每个volatile读操作的**后面**插入一个**LoadStore屏障**( 禁止下面所有的普通写操作和上面的volatile读重排序 )

   **执行顺序:**  StoreStore->volatile写->StoreLoad->volatile读->LoadLoad->LoadStore



## 6.AQS（AbstractQueuedSynchronizer）

#### Node节点

  用于存放 Sync Queue 、 Condition Queue 

```java
public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer
    implements java.io.Serializable {
   
    private volatile int state;//同步状态(若是独占锁只有0/1，共享锁为n，n为资源数)
    
    private transient volatile Node head;//同步队列的头节点
    private transient volatile Node tail;//同步队列的尾节点
    
    static final long spinForTimeoutThreshold = 1000L;//自旋纳秒数
    
}

/**
           +------+  prev +-----+       +-----+
      head |      | <---- |     | <---- |     |  tail
           +------+       +-----+       +-----+
**/
static final class Node {

    static final Node SHARED = new Node();//共享节点
    static final Node EXCLUSIVE = null; //独占节点
    
    static final int CANCELLED =  1;//线程被取消
    static final int SIGNAL    = -1;//当前线程已经准备好，正等待其实线程释放锁
    static final int CONDITION = -2;//正在等待条件(条件队列)
    static final int PROPAGATE = -3;//表示下一个acquireShared应无条件传播
    
    
    volatile int waitStatus;//节点状态(CANCELLED/SIGNAL/CONDITION/PROPAGATE)
    
    
    volatile Node prev;//前置节点
    volatile Node next;//后置节点
    
    
    volatile Thread thread;//被包装成节点的线程
    Node nextWaiter;//下一个等待节点，可以用来判断共享锁或者独占锁
    
    
}

//添加节点(默认快速往队尾加入，添加失败循环添加直至成功)
private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
    	//循环添加直至成功
        enq(node);
        return node;
}

private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
}
```

#### 独占锁、共享锁

- **独占锁:**  tryAcquire、tryRelease
- **共享锁:**  tryAcquireShared、tryReleaseShared

#### Sync Queue(同步队列)

1. 尝试获取锁
2. 获取锁失败，把当前线程包装成同步队列节点加入队列。
3. 获取当前节点的前置节点，若前置节点是头节点，尝试去获取锁。
4. 若成功获取到锁，将当前节点设置成头节点。(成功获取锁，返回)
5. 若前置节点不是头节点(没有排到当前节点)或者获取锁失败(可能是非公平锁，被先获取了)
6. 过滤前面被取消的节点，并把前置节点状态设置为SIGNAL，并挂起当前线程。
7. 等待正在执行的线程释放锁，并唤醒下一个未被取消的节点。

```java
//获取锁
public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
}

final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null;
                    failed = false;
                    return interrupted;
                }
                //过滤前面被取消的节点，并把前置节点状态设置为SIGNAL，并挂起线程。
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            //获取锁失败，取消节点
            if (failed)
                cancelAcquire(node);
        }
}

private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    	//前一个节点状态
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            //SIGNAL状态说明只需要等待锁
            return true;
        if (ws > 0) {
           	//waitStatus大于0,节点被取消
            do {
                //删除被取消的节点
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
           	//前置节点waitStatus设置为-1,代表已经准备好了
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
}

//释放资源
public final boolean release(int arg) {
    	//尝试释放锁
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
}

private void unparkSuccessor(Node node) {
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);
		
    	//获取头结点的下一个节点
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            //找到第一个没有被取消的节点
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            //唤醒该节点
            LockSupport.unpark(s.thread);
}
```



#### Condition Queue （等待队列）

```java
 public class ConditionObject implements Condition, java.io.Serializable {
 	
     private transient Node firstWaiter;//第一个节点
     private transient Node lastWaiter;//最后一个节点
     
     private static final int REINTERRUPT =  1;//reinterrupt等待退出
     private static final int THROW_IE    = -1;//抛出InterruptedException等待退出
     
 }
```

##### await:

**await():**   await期间响应中断，如果阻塞太久可以随时中断唤醒

**await(long time, TimeUnit unit):**  可以设置等待超时时间，并可以响应中断

**awaitUninterruptibly:**  await期间不响应中断，非得等到条件满足被唤醒



1. 将当前线程封装成Node节点并加入等待队列(Condition queue)的尾部

2. 释放所有锁和资源

3. 死循环直到节点转移到同步队列(Sync Queue),循环中会挂起线程,等待唤醒。

   await: 线程被中断会转移到同步队列

   awaitUninterruptibly: 只能signal转移到同步队列

   await(long time, TimeUnit unit): 超时会同步到同步队列

4. 处理同步队列

```java
final ConditionObject newCondition() {
     return new ConditionObject();
}

public final void await() throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    //往队尾添加新节点
    Node node = addConditionWaiter();
    //尝试释放所有资源
    int savedState = fullyRelease(node);
    int interruptMode = 0;
    //判断条件队列节点是否转移到同步队列
    while (!isOnSyncQueue(node)) {
        //是条件队列挂起当前线程
        LockSupport.park(this);
        //检查线程是否被中断(如果中断，取消等待队列节点)
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
    //处理同步队列
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null)
        //去除非条件节点
        unlinkCancelledWaiters();
    //抛出异常
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);            
}

//往队尾添加新节点
private Node addConditionWaiter() {
    Node t = lastWaiter;
    //尾节点已经不存在
    if (t != null && t.waitStatus != Node.CONDITION) {
        //去除非条件节点
        unlinkCancelledWaiters();
        t = lastWaiter;
    }
    //将当前节点包装成条件节点
    Node node = new Node(Thread.currentThread(), Node.CONDITION);
    if (t == null)
        firstWaiter = node;
    else
        t.nextWaiter = node;
    lastWaiter = node;
    return node;
}
```

##### signal

**signal:**  移动等待时间最长的线程，进入同步节点

**signalAll: **   移动所有的线程，进入同步节点

```java
private void doSignal(Node first) {
    		//将first转移到同步队列
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
            // 如果转移不成功且还有后续节点，那么继续后续节点的转移    
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
}

final boolean transferForSignal(Node node) {
       
    	//讲节点状态从条件状态-> 默认状态
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        //往同步队列队尾加入当前节点
        Node p = enq(node);
        int ws = p.waitStatus;
    	//节点已经被取消或者节点状态设置成SIGNAL失败，会唤起节点
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
}
```



#### 共享锁

```java
private void doAcquireShared(int arg) {
    //将当前线程包装成节点，mode为SHARED
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                //尝试获取锁,r为剩余资源
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    //重设头节点，并唤醒后续节点
                    setHeadAndPropagate(node, r);
                    p.next = null; 
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```



#### ForkJoinPool

**常用参数：**

-  **parallelism：** 并行级别，（0-2^16-1）
-  **factory:**  工作线程线程工厂
-  **handler：** 异常处理
-  **asyncMode：** 是否异步调度

**分而治之/工作窃取:**

​	ForkJoinPool采取的是一个分而治之+工作窃取的模式，当执行一个新的任务时，会将任务拆分成更小的任务，并将小任务加入线程队列WorkQueue中，当其他线程任务先执行完，会窃取其他的队列的任务来执行。WorkQueue是一个双端队列（push()/pop()仅在其所有者工作线程中调用，poll()是由其它线程窃取任务时调用的；），当只剩下一个任务时，通过CAS进行竞争。

**WorkQueue：**

```java
static final class WorkQueue {
    
    ForkJoinTask<?>[] array;//工作任务数组
    volatile int base; //下一个要出队列的索引（出队索引）
    int top; //下一个要推送进来的索引（入队索引）
    
}
```

**ForkJoinTask：**

```java
public abstract class ForkJoinTask<V> implements Future<V>, Serializable {
	
    volatile int status; //任务状态
    static final int DONE_MASK   = 0xf0000000;  // 用于屏蔽未完成的bit位 (1111000...)
    static final int NORMAL      = 0xf0000000;  // 正常运行
    static final int CANCELLED   = 0xc0000000;  // 取消状态 (1100000..)
    static final int EXCEPTIONAL = 0x80000000;  // 异常状态(1000000..)
    static final int SIGNAL      = 0x00010000;  // 大于1 << 16
    static final int SMASK       = 0x0000ffff;  //(1111111111111111)
    
    //当前工作线程加入队列，若不是work线程，加入线程池筛选的队列
    public final ForkJoinTask<V> fork() {
        Thread t;
        if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
            ((ForkJoinWorkerThread)t).workQueue.push(this);
        else
            ForkJoinPool.common.externalPush(this);
        return this;
    }
    
    //当处理结束返回结果集
    public final V join() {
        int s;
        if ((s = doJoin() & DONE_MASK) != NORMAL)
            reportException(s);
        return getRawResult();
    }
}
```

**线程池快速入队的执行:**

1. 若不是ForkJoinTask，包装成RunnableExecuteAction。
2. 根据probe值判断落在哪个workqueue中，并对该workqueue加锁。
3. 任务总数 > (队列下一个进队索引-队列下一个出队索引)
4. 若大于，则表示可以存入队列，计算存储的索引，进行存储并且(top++)，释放锁。若出队索引小于等于1，尝试开启(激活)一个新的工作线程。
5. 若不满足3，则说明队列满了。会释放锁，然后执行(完整版入队操作externalSubmit)

```java
public class ForkJoinPool extends AbstractExecutorService {

    //runState
    private static final int  RSLOCK     = 1;
    private static final int  RSIGNAL    = 1 << 1;
    private static final int  STARTED    = 1 << 2; //起始状态
    private static final int  STOP       = 1 << 29; //停止状态(拒绝接受任务，不处理任务)
    private static final int  TERMINATED = 1 << 30; //拒绝接受任务，处理任务
    private static final int  SHUTDOWN   = 1 << 31; //线程池已经停止
    
	public void execute(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>) 
            job = (ForkJoinTask<?>) task;
        else
            job = new ForkJoinTask.RunnableExecuteAction(task);
        externalPush(job);
    }
    
    final void externalPush(ForkJoinTask<?> task) {
        //ws:工作队列数组
        WorkQueue[] ws; WorkQueue q; int m;
        int r = ThreadLocalRandom.getProbe();
        //运行状态
        int rs = runState;
        //判断在哪个存储桶的任务（最大数量126），并且对队列上锁
        if ((ws = workQueues) != null && (m = (ws.length - 1)) >= 0 &&
            (q = ws[m & r & SQMASK]) != null && r != 0 && rs > 0 &&
            U.compareAndSwapInt(q, QLOCK, 0, 1)) {
            ForkJoinTask<?>[] a; int am, n, s;
            //任务总数(am) > (队列下一个进队索引(s)-队列下一个出队索引)(n)
            if ((a = q.array) != null &&
                (am = a.length - 1) > (n = (s = q.top) - q.base)) {
                //得出当前任务存储的索引
                int j = ((am & s) << ASHIFT) + ABASE;
                //存储
                U.putOrderedObject(a, j, task);
                U.putOrderedInt(q, QTOP, s + 1);
                U.putIntVolatile(q, QLOCK, 0);
                //若是出队索引小于1
                if (n <= 1)
                    signalWork(ws, q);
                return;
            }
            U.compareAndSwapInt(q, QLOCK, 1, 0);
        }
        //完整版入队操作
        externalSubmit(task);
    }
}
```

**工作线程运行：**

```java
final void runWorker(WorkQueue w) {
    	//resize队列（原队列扩展一倍）
        w.growArray();   
    	//初始化窃取指数(随机)
        int seed = w.hint;               
        int r = (seed == 0) ? 1 : seed;  
        for (ForkJoinTask<?> t;;) {
            //扫描，并试图窃取任务
            if ((t = scan(w, r)) != null)
                //运行任务
                w.runTask(t);
            else if (!awaitWork(w, r))
                break;
            r ^= r << 13; r ^= r >>> 17; r ^= r << 5;
        }
    }
```



## 7.异步回调任务FutureTask

#### 任务的运行

1. 判断task的状态是new，并且runner没有被其他线程占用
2. 执行包装了runable方法的callable方法。
3. 失败则捕获异常设置异常值
4. 若执行成功(将状态设置为COMPLETING)后，会设置结果值，然后将task的状态设置成NORMAL
5. 最后唤醒等待节点(在get的时候会park线程，任务执行结束才会去unpark)。

```java
private volatile int state;
private static final int NEW          = 0; //新建任务
private static final int COMPLETING   = 1; //完成中
private static final int NORMAL       = 2; //正常 NEW -> COMPLETING -> NORMAL
private static final int EXCEPTIONAL  = 3; //异常 NEW -> COMPLETING -> EXCEPTIONAL
private static final int CANCELLED    = 4; //取消 NEW -> CANCELLED
private static final int INTERRUPTING = 5; //中断中
private static final int INTERRUPTED  = 6; //已中断 NEW -> INTERRUPTING -> INTERRUPTED

private volatile Thread runner;//可调用线程
private volatile WaitNode waiters;//等待线程包装成的节点

public class FutureTask<V> implements RunnableFuture<V> {

    public void run() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return;
        try {
            //runnable包装的callable
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)
                    set(result);
            }
        } finally {
            runner = null;
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }
    
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)
            s = awaitDone(false, 0L);
        return report(s);
    }
}
```



## 8. RedLock

1.  获取当前时间（毫秒） 
2.  尝试按顺序获取锁(lua setnx),计算锁的过期时间，使得所有的节点的锁在同时过期。（会设置超时时候,若某个实例不可用，尽快尝试下一个）
3.  当且仅当客户端在半数节点上都成功获得了锁  ，而且总时间消耗小于锁有效时间，锁被认为获取成功 。
4.  如果锁获取成功了，那么它的有效时间就是最初的锁有效时间减去之前获取锁所消耗的时间 
5.  锁获取失败了 ， 将会尝试释放所有节点的锁 。