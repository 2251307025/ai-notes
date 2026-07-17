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

    /** JWT 绝对有效期（毫秒），启动时由 WebConfig 根据 auth.token.absolute-expiry-hours 配置写入 */
    private static volatile long tokenExpiryMs = 8 * 3600 * 1000;

    /**
     * 配置 JWT 绝对有效期（会话安全上限）。
     * 该值写入后签发的新 token 将使用此过期时间，签发后不可更改。
     */
    public static void setTokenExpiryMs(long tokenExpiryMs) {
        JwtUtil.tokenExpiryMs = tokenExpiryMs;
    }

    public static String getJwt(Map<String,Object> cl){
        return Jwts.builder()
                .claims(cl)
                .signWith(KEY)
                .expiration(new Date(System.currentTimeMillis() + tokenExpiryMs))
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
