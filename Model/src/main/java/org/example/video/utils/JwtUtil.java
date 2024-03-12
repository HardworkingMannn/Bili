package org.example.video.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.video.Model.constant.JwtConst;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;


@Slf4j
public class JwtUtil {
    private static Algorithm algorithm= Algorithm.HMAC256(JwtConst.JWT_SECRET);
    public static String  generateToken(Integer id){
         //用密钥创建算法对象，用于加密
        String token = JWT.create().withIssuer(JwtConst.JWT_ISSUER)
                .withClaim("userId",id)
                .withIssuedAt(new Date())
                .withExpiresAt(getExipireDate())
                .sign(algorithm);
        log.info("生成token:{}",token);
        return token;
    }
    public static Date  getExipireDate(){//用于获取结束时间
        Date date = new Date(new Date().getTime() + JwtConst.JWT_EXIST_HOUR * 60 * 60 * 1000);
        log.info("token过期时间.{}",date);
        return date;
    }
    public static Integer  verifyToken(String token){
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(JwtConst.JWT_ISSUER)
                .build();
        DecodedJWT verify;
        try {
            verify = verifier.verify(token);
        }catch (TokenExpiredException e){
            log.info("token{}过期",token);
            return null;
        }
        Integer userId = verify.getClaim("userId").asInt();
        log.info("检验token，找到userId:{}",userId);
        return userId;
    }
}
