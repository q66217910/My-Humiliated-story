reids频率控制
==== 
 1.令牌桶算法：
 >令牌桶中有初始容量，每来一个请求从桶中获取一个令牌，并且在一定时间间隔中可以生成令牌，多余的令牌被丢弃。可以实现限速功能。
  例子:GUAVA RateLimiter
 
 2.redis实现令牌桶算法：    
    配置:
        
        replenishRate:令牌桶填充平均速率，单位：秒
        burstCapacity:令牌桶上限
        requested:令牌消耗数
        
    存储两个redis key 
    token_key 令牌桶剩余令牌数, timestamp_key 令牌桶最后填充令牌时间，单位：秒。
    math.min(burstCapacity, tokens_key+(math.max(0, now-timestamp_key)*replenishRate)) 计算剩余最大令牌数
    当剩余最大令牌数>requested时,取得令牌。最终存储 token_key,timestamp_key.
    
   与RateLimiter相同都是使用，有访问才更新令牌数。
   
   例子: 
   
   
   [lua脚本地址](https://github.com/q66217910/My-Humiliated-story/blob/master/redis/redisRateLimiter.lua)
    
    
    
    