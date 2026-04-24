package com.chj;

import org.junit.jupiter.api.Test;

public class ThreadLocalTest {
    @Test
    public void testThreadLocalSetAndGet(){
        ThreadLocal tl=new ThreadLocal();
        new Thread(()->{
            tl.set("1");
            System.out.println(""+Thread.currentThread().getName()+tl.get());
            System.out.println(""+Thread.currentThread().getName()+tl.get());
            System.out.println(""+Thread.currentThread().getName()+tl.get());
        },"A").start();
        new Thread(()->{
            tl.set("2");
            System.out.println(""+Thread.currentThread().getName()+tl.get());
            System.out.println(""+Thread.currentThread().getName()+tl.get());
            System.out.println(""+Thread.currentThread().getName()+tl.get());
        },"B").start();
    }
}
