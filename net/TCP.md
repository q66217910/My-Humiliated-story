TCP
===

    TCP是面向连接的协议，在两端通信时，必须先建立链接，然后才能传输数据。而建立连接采用的就是三次握手法，断开连接采用的是四次挥手法

一.TCP首部结构详解
---
TCP数据封装在一个IP数据报中：
> IP数据报{IP首部,TCP报文段{TCP首部,TCP数据}}
>


图片来自《TCP/IP详解卷1》
TCP报文段:
>![RUNOOB 图标](https://upload-images.jianshu.io/upload_images/5679451-80b421b7c501a6ff.png)

**1.源端口和目的端口：**
     
     各占2个字节，这两个值加上IP首部中的源端IP地址和目的端IP地址唯一确定一个TCP连接。有时一个IP地址和一个端口号也称为socket（插口）。
     
**2.序号:**

    占4个字节，是本报文段所发送的数据项目组第一个字节的序号。在TCP传送的数据流中，每一个字节都有一个序号。
    
**3.确认序号：**

    占4字节，是期望收到对方下次发送的数据的第一个字节的序号，也就是期望收到的下一个报文段的首部中的序号；
    确认序号应该是上次已成功收到数据字节序号+1。只有ACK标志为1时，确认序号才有效。
    
**4.数据偏移：**

    占4比特，表示数据开始的地方离TCP段的起始处有多远。实际上就是TCP段首部的长度。由于首部长度不固定，因此数据偏移字段是必要的。
    数据偏移以32位为长度单位，也就是4个字节，因此TCP首部的最大长度是60个字节。即偏移最大为15个长度单位=1532位=154字节。
    
**5.6个标志位比特：**

    ①URG：当URG=1时，注解此报文应尽快传送，而不要按本来的列队次序来传送。与“紧急指针”字段共同应用，紧急指针指出在本报文段中的紧急数据的
          最后一个字节的序号，使接管方可以知道紧急数据共有多长；
    ②ACK：只有当ACK=1时，确认序号字段才有效；
    ③PSH：当PSH=1时，接收方应该尽快将本报文段立即传送给其应用层。
    ④RST：当RST=1时，表示出现连接错误，必须释放连接，然后再重建传输连接。复位比特还用来拒绝一个不法的报文段或拒绝打开一个连接；
    ⑤SYN：SYN=1,ACK=0时表示请求建立一个连接，携带SYN标志的TCP报文段为同步报文段；
    ⑥FIN：发端完成发送任务。
    
**6.窗口:**

    TCP通过滑动窗口的概念来进行流量控制。设想在发送端发送数据的速度很快而接收端接收速度却很慢的情况下，为了保证数据不丢失，
    显然需要进行流量控制， 协调好通信双方的工作节奏。所谓滑动窗口，可以理解成接收端所能提供的缓冲区大小。
    TCP利用一个滑动的窗口来告诉发送端对它所发送的数据能提供多大的缓 冲区。窗口大小为字节数，
    起始于确认序号字段指明的值（这个值是接收端正期望接收的字节）。窗口大小是一个16bit字段，因而窗口大小最大为65535字节。

**7。检验和：**

    检验和覆盖了整个TCP报文段：TCP首部和数据。这是一个强制性的字段，一定是由发端计算和存储，并由收端进行验证。
    
**8.紧急指针：**

    只有当URG标志置1时紧急指针才有效。紧急指针是一个正的偏移量，和序号字段中的值相加表示紧急数据最后一个字节的序号。
    
    

二、TCP三次握手过程
---

所谓三次握手（Three-Way Handshake）即建立TCP连接，就是指建立一个TCP连接时，需要客户端和服务端总共发送3个包以确认连接的建立。
在socket编程中，这一过程由客户端执行connect来触发

    1、建立连接时，客户端发送SYN包（SYN=i）到服务器，并进入到SYN-SEND状态，等待服务器确认
    2、服务器收到SYN包，必须确认客户的SYN（ack=i+1）,同时自己也发送一个SYN包（SYN=k）,即SYN+ACK包，此时服务器进入SYN-RECV状态
    3、客户端收到服务器的SYN+ACK包，向服务器发送确认报ACK（ack=k+1）,此包发送完毕，客户端和服务器进入ESTABLISHED状态，
      完成三次握手，客户端与服务器开始传送数据。
      
三、TCP四次挥手
---

所谓四次挥手（Four-Way Wavehand）即终止TCP连接，就是指断开一个TCP连接时，需要客户端和服务端总共发送4个包以确认连接的断开。

    1.Client发送一个FIN，用来关闭Client到Server的数据传送，Client进入FIN_WAIT_1状态。
    2.Server收到FIN后，发送一个ACK给Client，确认序号为收到序号+1（与SYN相同，一个FIN占用一个序号），Server进入CLOSE_WAIT状态。
    3.Server发送一个FIN，用来关闭Server到Client的数据传送，Server进入LAST_ACK状态。
    4.Client收到FIN后，Client进入TIME_WAIT状态，接着发送一个ACK给Server，确认序号为收到序号+1，Server进入CLOSED状态，完成四次挥手

    
[原文地址](https://www.jianshu.com/p/8c5ccbe51f5b)

    
