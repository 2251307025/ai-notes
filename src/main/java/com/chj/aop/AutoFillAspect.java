package com.chj.aop;

import com.chj.anno.AutoFill;
import com.chj.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Before("@annotation(com.chj.anno.AutoFill)")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始公共字段填充");
        MethodSignature signature=(MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        AutoFill.OperationType operationType = autoFill.value();
        Object[] args = joinPoint.getArgs();
        if (args==null||args.length==0)return;
        Object entity=args[0];
        try {
            if (operationType==AutoFill.OperationType.INSERT){
                setFieldValue(entity,"createTime", LocalDateTime.now());
                setFieldValue(entity,"updateTime",LocalDateTime.now());
                Map<String,Object> map = ThreadLocalUtil.get();
                Integer userId=(Integer)map.get("id");
                setFieldValue(entity,"createUser",userId);
            }else if (operationType==AutoFill.OperationType.UPDATE){
                setFieldValue(entity,"updateTime",LocalDateTime.now());
            }
        }catch (Exception e){
            log.error("自动填充失败: {}",e.getMessage(),e);
        }
    }
    private void setFieldValue(Object obj,String fieldName,Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj,value);
    }
}
