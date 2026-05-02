package com.chj.utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            "chjchjchjchjchjchjchjchjchjchjchjchj".getBytes(StandardCharsets.UTF_8));

    public static String getJwt(Map<String,Object> cl){
        return Jwts.builder()
                .claims(cl)
                .signWith(KEY)
                .expiration(new Date(System.currentTimeMillis() + 3600 * 1000))
                .compact();
    }
    public static Claims passJwt(String s){
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(s)
                .getPayload();
    }

}
