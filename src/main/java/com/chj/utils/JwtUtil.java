package com.chj.utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtil {

    public static String getJwt(Map<String,Object> cl){
        String c = Jwts.builder()
                .addClaims(cl)
                .signWith(SignatureAlgorithm.HS256, "chjchjchj")
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000))
                .compact();
        return c;
    }
    public static Claims passJwt(String s){
        Claims c = Jwts.parser()
                .setSigningKey("chjchjchj")
                .parseClaimsJws(s)
                .getBody();
        return c;
    }

}
