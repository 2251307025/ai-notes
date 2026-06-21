-- stock_deduct.lua
-- KEYS[1]: 库存Key
-- ARGV[1]: 扣减数量
-- ARGV[2]: 订单唯一ID（用于幂等）

local order_id = ARGV[2]
local deduct_qty = tonumber(ARGV[1])

-- 1. 幂等检查：防止同一订单重复扣减
local exist = redis.call('HEXISTS', KEYS[1] .. ':history', order_id)
if exist == 1 then
    return 0  -- 已扣减过
end

-- 2. 获取当前库存
local current = tonumber(redis.call('GET', KEYS[1]) or 0)

-- 3. 库存不足
if current < deduct_qty then
    return -1
end

-- 4. 原子扣减
local new_stock = redis.call('DECRBY', KEYS[1], deduct_qty)

-- 5. 记录扣减历史（幂等）
redis.call('HSET', KEYS[1] .. ':history', order_id, deduct_qty)

return new_stock  -- 返回剩余库存