package com.zd.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettySimpe {

    public static void main(String[] args) {

        //boss线程组负责io        推荐个数1
        EventLoopGroup boss = new NioEventLoopGroup(1);
        //work线程组 负责处理Handler任务       推荐个数 cpu核心数*2 Runtime.getRuntime().availableProcessors() * 2
        EventLoopGroup workers = new NioEventLoopGroup();

        //服务启动
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 设置EventLoopGroup
        bootstrap.group(boss, workers)
                //
                .channel(NioServerSocketChannel.class)
                .bind();
    }

}
