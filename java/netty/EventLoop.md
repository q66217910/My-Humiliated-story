EventLoop
===
 对应一个线程循环，一个Selector，处理IO事件
 
NioEventLoop.run()
---

    等待io准备就绪：
    
        taskQueue不为空：
            调用NIO的 selectNow：检查当前是否有就绪的 IO 事件, 
                                如果有, 则返回就绪 IO 事件的个数; 如果没有, 则返回0
            因为select()是不阻塞的,会调用selector.wakeup()唤醒
            
        taskQueue为空： 调用NIO的select(long) 会阻塞
        （在有任务时希望不阻塞迅速处理,没有任务时阻塞等待IO）
    
    IO 事件的处理：
        ioRatio： 线程分配给 IO 操作所占的时间比
                (即运行 processSelectedKeys 耗时在整个循环中所占用的时间)
        processSelectedKeys():查询就绪的io事件
        
        OP_READ(读事件):
            分配 ByteBuf->从 SocketChannel 中读取数据->调用 pipeline.fireChannelRead 发送一个 inbound 事件.
        OP_WRITE(写事件):
            flush
        OP_CONNECT(连接事件):
             OP_CONNECT 从就绪事件集中清除, 不然会一直有 OP_CONNECT 事件->调用 unsafe.finishConnect() 通知上层连接已建立
    
     
 
 
 NioEventLoopGroup
 ---
 包含多个NioEventLoop
 
 
 EventExecutor
 ---
 特殊的EventLoopGroup(继承)
 
    GlobalEventExecutor：单线程单例
 
 Selector
 ---
 多路复用路由器
 
ServerSocketChannel
 ---
 EventLoop注册过程：
    
    Bootstrap.initAndRegister -> 
        AbstractBootstrap.initAndRegister -> 
            MultithreadEventLoopGroup.register -> 
                SingleThreadEventLoop.register -> 
                    AbstractUnsafe.register ->
                        AbstractUnsafe.register0 ->
                            AbstractNioChannel.doRegister
 
 