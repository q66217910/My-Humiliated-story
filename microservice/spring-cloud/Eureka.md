Eureka
===
CAP中的AP

Eureka Client
---
    
    1.向Server注册服务实例 （instance发生改变 40s）
    2.向Eureka Server为租约续期
    3.当服务关闭期间，向Eureka Server取消租约
    4.查询Eureka Server中的服务实例列表
    5.主动下线
    6. 缓存Application的信息 （全量、增量）
    
Eureka Server
---
启动时，记录配置文件中的集群节点

    1.接受Client的注册请求  (ApplicationResource.addInstance())
    2.心跳 （InstanceResource.renewLease()）
    3.下线处理 （InstanceResource.cancelLease()）
    4. 定时清理没有续约的client
    5. 全量获取服务信息 （ApplicationsResource.getContainers()）
    6. 增量获取服务信息 （ApplicationsResource.getContainerDifferential()）
    7. 集群同步  （默认采用批量批量处理,将task放入队列,1分钟从queue中获取一次）
        【PeerAwareInstanceRegistryImpl.replicateInstanceActionsToPeers()】
    
自我保护机制
---
当eureka每分钟续约数小于一定比例。开启保护机制，不会剔除任何一个客户端。
（Eureka-Server初始化，cancle主动下线， 客户端注册 ,定时器）会变更

    numberOfRenewsPerMinThreshold （每分钟最小续约数） = 客户端数*2
    serverConfig.getRenewalPercentThreshold():每分钟续约比例 默认0.85
    
学习文章mark： [https://www.jianshu.com/p/f720d3857830]
