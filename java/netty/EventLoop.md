EventLoop
===
 对应一个线程循环，一个Selector，处理IO事件
 
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
 
 select
 ---
 