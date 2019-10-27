Linux I/O多路复用  （epoll）
===

   IO请求的流程:
        
        1.准备数据,这时数据可能还没有到达,如还没有收到一个完成的包, kernel需要等待.
        也就是 数据被拷贝到操作系统内核的缓冲区是需要一个过程的
        2. 拷贝数据到用户内存,对于synchronous IO 这一步需要用户进程去请求read操作,
        阻塞.对于asynchronous IO,这一步由kernel主动完成,非阻塞.
        

一.阻塞I/O
---
   1 2 阶段都是阻塞的:
   
        也就是说当你调用read时，如果没有数据收到，那么线程或者进程就会被挂起，直到收到数据。
        
二、非阻塞I/O
---
   synchronous非阻塞IO,这里2是阻塞的
   
        1.用户进程轮询请求数据,没有数据时kernel返回错误状态,用户进程收到后会重试.
        2.某次请求后如果数据到达,kernel返回数据到达状态,阶段1结束,用户进程调用read,将数据从kernel拷贝到用户内存
        
        这时，当你调用read时，如果有数据收到，就返回数据，如果没有数据收到，就立刻返回一个错误，如EWOULDBLOCK。
        这样是不会阻塞线程了，但是你还是要不断的轮询来读取或写入。
        
三、I/O多路复用
---

   和阻塞IO一样是synchronous阻塞IO,这里的 1 2是阻塞的 ,唯一的区别是一个用户进程负责多个socket,也是IO多路复用的优势
   
        1.select poll epoll请求数据,在阶段1被阻塞,当某个socket有数据到达了就通知用户进程
        2.用户进程调用read操作,将数据从kernel拷贝到用户内存,在阶段2被阻塞
        阻塞io只需要一次系统调用,IO多路复用需要两次,如果连接数不是很高时 select/epoll不一定比multi-threading+blocking IO更快
        
   select():
   
        【1】每次调用select()都需要把fd(文件描述符)从用户态拷贝到内核态，开销比较大
        
        【2】每次都需要在内核遍历传入的fd（文件描述符）
        
        【3】select支持文件数量比较小，默认是1024
   poll():
   
        poll的实现和select非常相似，只是描述fd集合的方式不同，poll使用pollfd结构而不是select的fd_set结构，支持的文件数量比较多，不仅仅是1024
        
   epoll():
        
        select/poll只提供了一个函数，selct/poll函数，但是epoll一下子就提供了3个函数，真是人多力量大，难怪这么强，如下3个函数：
        
        epoll_create,epoll_ctl和epoll_wait，epoll_create是创建一个epoll句 柄；epoll_ctl是注册要监听的事件类型；epoll_wait则是等待事件的产生。
        
        epoll既然是对select和poll的改进，就应该能避免上述的三个缺点。那epoll都是怎么解决的呢？在此之前，
        我们先看一下epoll和select和poll的调用接口上的不同，select和poll都只提供了一个函数——select或者poll函数。
        而epoll提供了三个函数，epoll_create,epoll_ctl和epoll_wait，epoll_create是创建一个epoll句柄；
        epoll_ctl是注册要监听的事件类型；epoll_wait则是等待事件的产生。
        
四、异步IO
---

asynchronous非阻塞 IO,完全的非阻塞
        
        1.用户进程发起read操作后立即返回去做其他事,kernel收到asynchronous read后也立刻返回
        2.在数据准备完成后,kernel将数据拷贝到用户内存,并发送给用户signa
