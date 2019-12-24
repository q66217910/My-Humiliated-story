ChannelPipeline
===
ChannelPipeline是ChannelHandler的编排管理容器，它内部维护了一个ChannelHandler的链表和迭代器

Inbound(读): 
---
head-> context-> tail

    fireChannelRegistered: channel注册到EventLoop
    fireChannelActive：  channel出于活跃状态，连接已就绪
    fireChannelRead： 接受数据
    fireChannelReadComplete： 触发ChannelPipeline下一个ChannelHandler
    fireExceptionCaught： 输入操作时发生异常
    fireUserEventTriggered： 接受用户自定义事件
    fireChannelWritabilityChanged： 直接触发ChannelPipeline下一个ChannelHandler
    fireChannelInactive： channel关闭
    fireChannelUnregistered： channel从EventLoop中注销
    
Oubound(写) (HeadContext):
---
tail->context -> head

    bind: 绑定端口地址
    connect： 请求地址
    write： 写数据
    flush： 请求刷新所有未处理的数据
    read： 从channel读取数据到缓冲区
    disconnect： 断开连接
    close： 关闭