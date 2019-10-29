进程通信
===

进程通信的应用场景
---

    数据传输：一个进程需要将它的数据发送给另一个进程，发送的数据量在一个字节到几兆字节之间。
    共享数据：多个进程想要操作共享数据，一个进程对共享数据的修改，别的进程应该立刻看到。
    通知事件：一个进程需要向另一个或一组进程发送消息，通知它（它们）发生了某种事件（如进程终止时要通知父进程）。
    资源共享：多个进程之间共享同样的资源。为了作到这一点，需要内核提供锁和同步机制。
    进程控制：有些进程希望完全控制另一个进程的执行（如Debug进程），此时控制进程希望能够拦截另一个进程的所有陷入和异常，
            并能够及时知道它的状态改变。
            
进程通信的方式
---

1.管道( pipe )：

    当一个管道建立时，它会创建两个文件描述符：fd[0]为读而打开，fd[1]为写而打开。要关闭管道只需将这两个文件描述符关闭即可。

    1.它是半双工的（即数据只能在一个方向上流动），具有固定的读端和写端。
    2.它只能用于具有亲缘关系的进程之间的通信（也是父子进程或者兄弟进程之间）。
    3.它可以看成是一种特殊的文件，对于它的读写也可以使用普通的read、write 等函数。
      但是它不是普通的文件，并不属于其他任何文件系统，并且只存在于内存中。
      
2.FIFO:

    一旦创建了一个 FIFO，就可以用一般的文件I/O函数操作它。

    1.FIFO可以在无关的进程之间交换数据，与无名管道不同。
    2.FIFO有路径名与之相关联，它以一种特殊设备文件形式存在于文件系统中。
    
3、消息队列:

    消息队列，是消息的链接表，存放在内核中。一个消息队列由一个标识符（即队列ID）来标识。
    
    1.消息队列是面向记录的，其中的消息具有特定的格式以及特定的优先级。
    2.消息队列独立于发送与接收进程。进程终止时，消息队列及其内容并不会被删除。
    3.消息队列可以实现消息的随机查询,消息不一定要以先进先出的次序读取,也可以按消息的类型读取。
    
    
4.信号量:

    它是一个计数器。信号量用于实现进程间的互斥与同步，而不是用于存储进程间通信数据。
    
    1.信号量用于进程间同步，若要在进程间传递数据需要结合共享内存。
    2.信号量基于操作系统的 PV 操作，程序对信号量的操作都是原子操作。    
    3.每次对信号量的 PV 操作不仅限于对信号量值加 1 或减 1，而且可以加减任意正整数。   
    4.支持信号量组。
    
    
5.共享内存:

    指两个或多个进程共享一个给定的存储区。
    
    1.共享内存是最快的一种 IPC，因为进程是直接对内存进行存取。
    2.因为多个进程可以同时操作，所以需要进行同步。
    3.信号量+共享内存通常结合在一起使用，信号量用来同步对共享内存的访问。