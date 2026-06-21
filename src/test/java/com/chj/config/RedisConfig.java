package com.chj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
@Configuration
public class RedisConfig {
    @Bean
    public RedisScript<Long> stokeDeductScript(){
        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("lua/stock_deduct.lua")
        ));
        script.setResultType(Long.class);
        return script;
    }
}
