package com.chj.interceptors;


import com.chj.utils.JwtUtil;
import com.chj.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate srt;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String Jwt = request.getHeader("Authorization");
        try {
            Claims claims = JwtUtil.passJwt(Jwt);
            ThreadLocalUtil.set(claims);
            ValueOperations<String, String> sso = srt.opsForValue();
            String redisJwt = sso.get(Jwt);
            if (redisJwt == null) {
                throw new RuntimeException();
            }
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空ThreadLocal
        ThreadLocalUtil.remove();
    }
}
