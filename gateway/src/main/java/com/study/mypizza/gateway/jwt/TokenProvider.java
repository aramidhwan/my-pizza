package com.study.mypizza.gateway.jwt;

import com.study.mypizza.gateway.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class TokenProvider {
    public static final String ACCESS_TOKEN_COOKIE = "JWTAccessToken";
    public static final String REFRESH_TOKEN_COOKIE = "JWTRefreshToken";
    private final RedisService redisService ;

    @Value("${jwt.login_page}")
    public String LOGIN_PAGE ;
    @Value("${jwt.refresh_token_url}")
    public String REFRESH_TOKEN_URL ;
    public static final String BEARER_TYPE = "Bearer";
    private final SecretKey secretKey ;

    public TokenProvider(
            @Value("${jwt.secret}")String secret,
            RedisService redisService
        ) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS512.key().build().getAlgorithm());
        this.redisService = redisService ;
    }

    // 토큰의 유효성 검증 수행
    public boolean validateToken(String token) {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        String blackList = (String)redisService.getValue("blacklist:" + token) ;
        if ("logout".equals(blackList)) {
            log.debug("### Redis BlackList found!");
            return false ;
        }
        return true;
    }

    public Claims getClaims(String jwtToken) {
        // 0.12.3 방식
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwtToken).getPayload() ;
    }

}
