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

    