package com.chj;

import com.chj.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;

import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    @Test
    public void testGen(){
        Map<String,Object> map=new HashMap<>();
        map.put("id",2);
        String jwt = JwtUtil.getJwt(map);
        System.out.println(jwt);
        Claims claims = JwtUtil.passJwt(jwt);
        System.out.println(claims);
    }
}
