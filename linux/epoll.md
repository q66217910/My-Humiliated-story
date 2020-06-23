epoll
===

epoll 是 Linux 内核为处理大批量文件描述符而作了改进的 poll，是 Linux 下多路复用 IO接口 select/poll 的增强版本


文件描述符(fd)
---
    
    


epoll 接口
----
int epoll_create(int size);
    
    epoll_create 函数就是用来获取内核事件表的特殊文件描述符，该函数返回的文件描述符将用作其他 epoll 系统调用的第一个参数，
    以指定要访问的内核事件表。
    
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);

    epoll 的事件注册函数，用来操作内核事件表。它不同与 select() 是在监听事件时告诉内核要监听什么类型的事件，
    而是在这里先注册要监听的事件类型。
    
    1. epfd： 要操作的内核事件表的文件描述符，即 epoll_create 的返回值
    2. op：指定操作类型，操作类型有三种：
        -> EPOLL_CTL_ADD：往内核事件表中注册指定fd 相关的事件
        -> EPOLL_CTL_MOD：修改指定 fd 上的注册事件
        -> EPOLL_CTL_DEL：删除指定 fd 的注册事件
    3. fd：所要操作的文件描述符，也就是要内核事件表中监听的 fd
    4. event：指定所要监听的事件类型，epoll_event 结构指针类型。
        EPOLLIN：表示对应的文件描述符可以读（包括对端SOCKET正常关闭）；
        EPOLLOUT：表示对应的文件描述符可以写；
        EPOLLPRI：表示对应的文件描述符有紧急的数据可读（这里应该表示有带外数据到来）；
        EPOLLERR：表示对应的文件描述符发生错误；
        EPOLLHUP：表示对应的文件描述符被挂断；
        EPOLLET： 将EPOLL设为边缘触发(Edge Triggered)模式，这是相对于水平触发(Level Triggered)来说的。
        EPOLLONESHOT：只监听一次事件，当监听完这次事件之后，如果还需要继续监听这个socket的话，需要再次把这个socket加入到EPOLL队列里
    
int epoll_wait(int epfd, struct epoll_event* events, int maxevents, int timeout);

    等待事件的发生，它在一段超时时间之内等待一组文件描述符上的事件，epoll_wait 函数如果检测到事件，
    就将所有就绪的事件从内核事件表(epfd 参数决定)中复制到第二个参数 events 指向的数组中。
    
    (1) epfd
    要操作的内核事件表的文件描述符，即 epoll_create 的返回值
    
    (2) events
    内核事件表中得到的检测事件集合
    
    (3) maxevents & timeout
    maxevents 告诉内核 events 的最大 size，timeout 指定超时时间
    
epoll 工作模式
---

epoll 对文件描述符的操作有两种模式：LT（level trigger）和 ET（edge trigger）。LT 模式是默认模式，LT 模式与 ET 模式的区别如下：

    　LT模式：电平触发，当 epoll_wait 检测到描述符事件发生并将此事件通知应用程序，应用程序可以不立即处理该事件。
            下次调用 epoll_wait 时，会再次响应应用程序并通知此事件。
    
    　　ET模式：边沿触发，当 epoll_wait 检测到描述符事件发生并将此事件通知应用程序，应用程序必须立即处理该事件。
            如果不处理，下次调用 epoll_wait 时，不会再次响应应用程序并通知此事件。
    
    　　ET 模式在很大程度上减少了 epoll 事件被重复触发的次数，因此效率要比 LT 模式高。epoll 工作在 ET 模式的时候，
            必须使用非阻塞套接口，以避免由于一个文件句柄的阻塞读/阻塞写操作把处理多个文件描述符的任务饿死。

    