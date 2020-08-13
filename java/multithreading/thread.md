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

5. **Daemon():**  守护线程，在后台运行。不会随着主线程不会等待他退出才退出。

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
- **偏向锁：**  **当一个线程获取到锁时**，进入偏向锁模式。Mark Word的ThreadID为当前线程。之后该线程再访问同步块时不需要CAS操作来加锁和解锁。当一个线程获取到锁后会在对象头里替换偏向锁的线程id。
- **轻量级锁:**  **当发生锁竞争时**，进入轻量锁模式。
  1. 线程进入同步代码时，若对象头锁状态为 01（无锁状态），当前栈帧建立一个Lock Record（锁记录），用于存储对象目前的 Mark Word 的拷贝。
  2. 将对象头的Mark Word拷贝到栈帧的Lock Record
  3. 使用CAS操作尝试将对象的Mark Word 更新为指向 Lock Record 的指针，并将 Lock Record 里的 owner 指针指向对象的 Mark Word
  4. 若设置成功，目前线程就获取到了锁，将对象的 Mark Word 设置为00（轻量级锁）
  5. 若设置失败，检查对象的 Mark Word 的是否指向当前线程的栈帧（若是，说明当前线程已经获取到了锁）。否则说明多个线程在竞争锁，轻量级锁膨胀为重量级锁，锁标志的状态变为"10"，Mark Word 中存储的就变为指向重量级锁的指针，当前线程便尝试使用自旋来获取锁，而后面等待锁的线程要进入阻塞状态。
  6. 解锁，通过 CAS 操作尝试把线程栈帧中复制的Displaced Mark Word 替换到对象当前的 Mark Word
  7. 替换失败，说明由其他线程尝试获取该锁(此时锁已经膨胀为重量级锁)，在释放锁的同时，唤醒被挂起的线程。
- 



