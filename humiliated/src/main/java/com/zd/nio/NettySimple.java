package com.zd.nio;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import sun.misc.Unsafe;
import sun.nio.ch.ThreadPool;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NettySimple {

    public static void main(String[] args) throws InterruptedException {

        //boss线程组负责io        推荐个数1
        EventLoopGroup boss = new NioEventLoopGroup(1);
        //work线程组 负责处理Handler任务       推荐个数 cpu核心数*2 Runtime.getRuntime().availableProcessors() * 2
        EventLoopGroup workers = new NioEventLoopGroup();

        //服务启动
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 设置EventLoopGroup
        bootstrap.group(boss, workers)
                //  设置channel类型
                .channel(NioServerSocketChannel.class)
                //设置pipeline，消息处理链，read执行inbound，write执行outbound
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new JdkZlibEncoder());
                        pipeline.addLast(new JdkZlibDecoder());
                    }
                })
                .bind(8080)
                .addListener(future -> {
                    if (future.isSuccess()) {
                        System.out.println("bind success");
                    }
                })
                .channel()
                .closeFuture()
                //阻塞主线程
                .sync();
    }

}
