package com.chj;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisScript<Long> redisScript;

    private static final String STOCK_PREFIX="stock:";
    /**
     * 扣减库存
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @param orderId 订单ID（用于幂等）
     * @return 剩余库存，-1表示库存不足，0表示重复扣减
     */
    public Long deductStock(Long productId, Integer quantity, String orderId) {
        String stockKey = STOCK_PREFIX + productId;

        Long result = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(stockKey),  // KEYS
                String.valueOf(quantity),              // ARGV[1]
                orderId                                // ARGV[2]
        );
        if (result == -1){
            System.out.println("库存不足");
        }else if (result == 0){
            System.out.println("重复扣减");
        }else {
            System.out.println("扣减成功，剩余库存：" + result);
        }
        return result;
    }
    @Test
    public  void luaTest() {
        Long productId=1L;
        Integer quantity=2;
        String orderId="order_1";
        System.out.println(deductStock(productId, quantity, orderId));
    }
    @Test
    public void set(){
        stringRedisTemplate.opsForValue().set(STOCK_PREFIX+1,"10",1000,TimeUnit.SECONDS);
        stringRedisTemplate.opsForHash().put("1:history","12","2");
        stringRedisTemplate.opsForHash().delete("1:history","12");
    }
}
