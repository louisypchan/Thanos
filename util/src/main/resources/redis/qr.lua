--
-- Created by IntelliJ IDEA.
-- User: Louis
-- Date: 2018-5-22
-- Time: 15:10
-- To change this template use File | Settings | File Templates.
--
--local Random = {};
--
--function Random:Awake()
--    self.m_min = 1;
--    self.m_max = 1;
--    self.m_end = 1;
--    self.m_rangeMap = {};
--end
--
--function Random:setRange( min,max )
--    if min > max then
--        min,max = max ,min;
--    end
--    self.m_min = min;
--    self.m_max = max;
--    self.m_end = max;
--    self.m_rangeMap[self.m_max] = self.m_max;
--end
--
--function Random:getRandom()
--    math.randomseed(tostring(os.time()):reverse():sub(1,6));--避免时差太小
--    math.random(self.m_min,self.m_max);--过滤掉前几个劣质随机数;
--    math.random(self.m_min,self.m_max);
--    math.random(self.m_min,self.m_max);
--    local tmp = math.random(self.m_min,self.m_max);
--    local ret = self.m_rangeMap[tmp];
--    if ret == nil then
--        ret = tmp;
--    end
--    self.m_rangeMap[tmp] = self.m_max;
--
--    self.m_max = self.m_max - 1;
--    return ret;
--end
local key = KEYS[1]
local cacheKey = KEYS[2]
local value = ARGV[1]
local ttl = ARGV[2]
local sid = ARGV[3]

local exists = redis.call('SISMEMBER', key, value)

if exists == 1 then
    return 0
else
    redis.call('SADD', key, value)
    redis.call('SET', value, sid)
    redis.call('SET', value..cacheKey, sid)
    redis.call('EXPIRE', value, ttl)
end
return 1