package com.atguigu.gmall0218.passport.utils;

import io.jsonwebtoken.*;

import java.util.Map;

/**
 * @author qiyu
 * @create 2019-08-02 17:03
 * @Description:单点登录使用的jwt的工具类
 */
public class JwtUtil {
    public static String encode(String key, Map<String,Object> param, String salt){
        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }


    public  static Map<String,Object>  decode(String token ,String key,String salt){
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
            return null;
        }
        return  claims;
    }

}
