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



## 5.volatile

- **可见性:** 线程对volatile修饰的变量修改，会立即被其他线程感知
- **禁止指令重排**



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

2. 

