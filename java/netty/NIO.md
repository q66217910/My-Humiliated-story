NIO:
===

Channel:

    表示 IO 源与目标打开的连接，是双向的，但不能直接访问数据，只能与Buffer 进行交互。
    
Buffer:
 
    与Channel进行交互，数据是从Channel读入缓冲区，从缓冲区写入Channel中的
        
        flip：反转此缓冲区，将position给limit，然后将position置为0，其实就是切换读写模式
        clear：清除此缓冲区，将position置为0，把capacity的值给limit。
        rewind: 重绕此缓冲区，将position置为0
        

DirectByteBuffer:

    可减少一次系统空间到用户空间的拷贝
    
    
Selector:

    可使一个单独的线程管理多个Channel，open方法可创建Selector，
    register方法向多路复用器器注册通道，可以监听的事件类型：读、写、连接、accept。
    注册事件后会产生一个SelectionKey
    
SelectionKey：

    它表示SelectableChannel 和Selector 之间的注册关系
    
