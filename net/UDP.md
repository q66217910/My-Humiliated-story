UDP
===

一、UDP报文头
---

![11](https://img-blog.csdn.net/20171228174603943?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY2hpbmFfamVmZmVyeQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

    1.源端口：源端口号。在需要对方回信时选用。不需要时可全为０
    2.目的端口：目的端口号。这在终端交付报文时必须要使用到
    3.长度：UDP 数据包的长度 (包括首部和数据)，其最小值是 8 (仅有首部)
    4.校验和：检测 UDP 数据报在传输中是否有错。有错就丢弃。该字段是可选的，当源主机不想计算校验和，则直接令该字段全为 0

UDP 优点:
---

    1.UDP 无需建立连接
    2.无连接状态
    3.分组头部开销小。TCP 有 20 字节的首部开销，而 UDP 仅有 8 字节的开销