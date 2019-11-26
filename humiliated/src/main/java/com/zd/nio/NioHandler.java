package com.zd.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * nio 处理
 */
public class NioHandler implements Runnable {

    private Selector selector;

    private ServerSocketChannel servChannel;

    private int port;

    private boolean stop;

    public NioHandler(int port) {
        try {
            selector = Selector.open();
            servChannel = ServerSocketChannel.open();

            //设置成非阻塞
            servChannel.configureBlocking(false);
            //绑定端口
            servChannel.socket().bind(new InetSocketAddress(port), 1024);

            //注册
            servChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (!stop){
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    handleInput(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (key.isConnectable()){
                    if (sc.finishConnect()) {
                        //在通道上注册感兴趣事件为读
                        sc.register(selector, SelectionKey.OP_READ);
                    } else {
                        //连接失败，退出
                        System.exit(1);
                    }
                }
            }
            if (key.isReadable()) {//读取消息
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int read = sc.read(readBuffer);
                if (read > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "utf-8");
                    this.stop = true;
                } else if (read < 0) {
                    //对端链路关闭
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    public void stop() {
        this.stop = true;
    }
}
