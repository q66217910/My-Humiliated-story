redis过期策略
===

    volatile-lru：在设置了过期时间的键空间中，移除最近最少使用的key。
    allkeys-lru ：移除最近最少使用的key
    volatile-random： 在设置了过期时间的键空间中，随机移除一个键
    allkeys-random：直接在键空间中随机移除一个键
    volatile-ttl： 在设置了过期时间的键空间中，有更早过期时间的key优先移除
    noeviction : 不做过键处理，只返回一个写操作错误。