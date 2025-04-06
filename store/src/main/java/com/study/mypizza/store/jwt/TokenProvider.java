package com.study.mypizza.store.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider {
    public static final String BEARER_TYPE = "Bearer";
    public static final String ACCESS_TOKEN_COOKIE = "JWTAccessToken";
    private final SecretKey secretKey ;

    public TokenProvider(
            @Value("${jwt.secret}")String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS512.key().build().getAlgorithm());
    }

    private Claims getClaims(String jwtToken) {
        // 0.12.3 방식
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwtToken).getPayload() ;
    }

    // JWT 토큰에서 인증 객체(Authentication 획득)
    public Authentication getAuthentication(String jwtToken) {

        // 토큰에서 username과 권한(authorities) 획득
        // 토큰에서 claims body 얻기 (username과 authorities이 들어있음)
        Claims claims = getClaims(jwtToken) ;
        // 토큰에서 권한(authorities) 획득
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("authorities").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        log.trace("### 사용자 [{}] 권한(authorities) : {}", claims.getSubject(), authorities);

        // 스프링 시큐리티용 신규 인증 토큰 생성 (JWT 토큰을 이용해 AuthenticationToken 토큰 생성)
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
        authentication.setDetails(getClaims(jwtToken).get("customerNo", Integer.class));
        return authentication ;
    }
}
