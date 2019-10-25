--存储 令牌桶上限 key
local tokens_key = KEYS[1]
--存储  牌桶最后填充令牌时间，单位：秒。   key
local timestamp_key = KEYS[2]

--牌桶填充速率
local rate = tonumber(ARGV[1])
--最大令牌上限
local capacity = tonumber(ARGV[2])
--当前时间
local now = tonumber(ARGV[3])
--请求令牌数
local requested = tonumber(ARGV[4])

--计算令牌桶充满时间
local fill_time = capacity/rate
--保证时间充足
local ttl = math.floor(fill_time*2)

--获取剩余令牌数量
local last_tokens = tonumber(redis.call("get", tokens_key))
if last_tokens == nil then
    --默认令牌桶最大上限
    last_tokens = capacity
end
--redis.log(redis.LOG_WARNING, "last_tokens " .. last_tokens)

--令牌桶最后填充令牌时间，单位：秒。
local last_refreshed = tonumber(redis.call("get", timestamp_key))
if last_refreshed == nil then
    last_refreshed = 0
end
--redis.log(redis.LOG_WARNING, "last_refreshed " .. last_refreshed)

--计算新的令牌桶剩余令牌数量
local delta = math.max(0, now-last_refreshed)
local filled_tokens = math.min(capacity, last_tokens+(delta*rate))

-- 获取令牌
local allowed = filled_tokens >= requested
local new_tokens = filled_tokens
local allowed_num = 0
if allowed then
    new_tokens = filled_tokens - requested
    allowed_num = 1
end

redis.call("setex", tokens_key, ttl, new_tokens)
redis.call("setex", timestamp_key, ttl, now)

return { allowed_num, new_tokens }