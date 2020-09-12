# Netty

- **channel**: 封装了JAVA NIO 中的channel
- **pepeline**: 执行调用链
- **handler**: 处理单元，会装载到channel内部的pepeline中
- **eventloop**: eventloop封装了JAVA NIO 中的selector(用于监听socket)，channel会注册到eventloop中。用于处理io事件与普通任务

## 1.Bootstrap启动过程

1. 配置channel
2. 配置handler
3. 执行bind方法，将channel注册到eventloop上。

```java
EventLoopGroup bossGroup = new NioEventLoopGroup(1);
EventLoopGroup workerGroup = new NioEventLoopGroup();

try {
   ServerBootstrap b = new ServerBootstrap();
   b.group(bossGroup, workerGroup)
    .channel(NioServerSocketChannel.class)
    .childHandler(new ChannelInitializer<SocketChannel>() {
         @Override
         public void initChannel(SocketChannel ch) throws Exception {
        }
     });

   ChannelFuture f = b.bind(8888).sync();

    f.channel().closeFuture().sync();
} finally {
   bossGroup.shutdownGracefully();
   workerGroup.shutdownGracefully();
}
```

## 2.Eventloop工作过程

1. for死循环
2. 调用selector的select方法(有本地任务需要执行会阻塞)
3. 处理selector中的selectedKeys
4. 从key获取相应的就绪状态，调用channel中的pepeline中hadler的相应方法。
5. io事件处理完，会处理本地任务。

```java
public final class NioEventLoop extends SingleThreadEventLoop {
	
    @Override
    protected void run() {
        int selectCnt = 0;
        for (;;) {
            try {
                int strategy;
                try {
                    strategy = selectStrategy
                        .calculateStrategy(selectNowSupplier, hasTasks());
                    switch (strategy) {
                    case SelectStrategy.CONTINUE:
                        continue;

                    case SelectStrategy.BUSY_WAIT:
                    case SelectStrategy.SELECT:
                        long curDeadlineNanos = nextScheduledTaskDeadlineNanos();
                        if (curDeadlineNanos == -1L) {
                            curDeadlineNanos = NONE; 
                        }
                        nextWakeupNanos.set(curDeadlineNanos);
                        try {
                            if (!hasTasks()) {
                                strategy = select(curDeadlineNanos);
                            }
                        } finally {
                            nextWakeupNanos.lazySet(AWAKE);
                        }
                    default:
                    }
                } catch (IOException e) {
                    rebuildSelector0();
                    selectCnt = 0;
                    handleLoopException(e);
                    continue;
                }

                selectCnt++;
                cancelledKeys = 0;
                needsToSelectAgain = false;
                final int ioRatio = this.ioRatio;
                boolean ranTasks;
                if (ioRatio == 100) {
                    try {
                        if (strategy > 0) {
                            processSelectedKeys();
                        }
                    } finally {
                        ranTasks = runAllTasks();
                    }
                } else if (strategy > 0) {
                    final long ioStartTime = System.nanoTime();
                    try {
                        processSelectedKeys();
                    } finally {
                        final long ioTime = System.nanoTime() - ioStartTime;
                        ranTasks = runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                    }
                } else {
                    ranTasks = runAllTasks(0); 
                }

                if (ranTasks || strategy > 0) {
                    if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS 
                        && logger.isDebugEnabled()) {
                    }
                    selectCnt = 0;
                } else if (unexpectedSelectorWakeup(selectCnt)) {
                    selectCnt = 0;
                }
            } catch (CancelledKeyException e) {

            } catch (Throwable t) {
                handleLoopException(t);
            }
            try {
                if (isShuttingDown()) {
                    closeAll();
                    if (confirmShutdown()) {
                        return;
                    }
                }
            } catch (Throwable t) {
                handleLoopException(t);
            }
        }
    }
    
}
```

