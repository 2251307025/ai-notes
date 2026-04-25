package com.chj.utils;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal 工具类
 */
@SuppressWarnings("all")
public class ThreadLocalUtil {
    //提供ThreadLocal对象,
    private static final TransmittableThreadLocal<Object> TTL = new TransmittableThreadLocal<>();

    //根据键获取值
    public static <T> T get(){
        return (T) TTL.get();
    }
	
    //存储键值对
    public static void set(Object value){
        TTL.set(value);
    }


    //清除ThreadLocal 防止内存泄漏
    public static void remove(){
        TTL.remove();
    }
}
