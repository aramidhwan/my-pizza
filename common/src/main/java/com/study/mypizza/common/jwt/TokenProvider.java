package com.study.mypizza.common.jwt;

import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider {
    private final RedisService redisService;
    public static final String ACCESS_TOKEN_COOKIE = "JWTAccessToken";
    public static final String REFRESH_TOKEN_COOKIE = "JWTRefreshToken";
    public static final String BEARER_TYPE = "Bearer";
    public static final String JWT_SUBJECT_NAME = "email" ;
    private final SecretKey secretKey ;
    private final Long accessExpirationMs;
    private final Long refreshExpirationMs;
    private final String issuer;

    public TokenProvider (
            @Value("${jwt.secret}")String secret,
            @Value("${jwt.access-token-expiration-millis}") Long expireMs,
            @Value("${jwt.refresh-token-expiration-millis}") Long refreshExpirationMs,
            @Value("${jwt.issuer}") String issuer,
            RedisService redisService) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS512.key().build().getAlgorithm());
        this.accessExpirationMs = expireMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.issuer = issuer;
        this.redisService = redisService ;
    }

    // 토큰의 유효성 검증 수행
    public boolean validateToken(String accessToken) {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken);
        return true ;
    }

    public Claims getClaims(String jwtToken) {
        // 0.12.3 방식
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwtToken).getPayload() ;

//  0.11.5 방식
//        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("customerId", String.class);
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
        return authentication;
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date()) ;
    }

    public String createAccessToken(Authentication authentication, CustomerDto customerDto) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return createAccessToken(email, authorities, customerDto.getCustomerNo()) ;
    }
    public String createAccessToken(String email, String authorities, int customerNo) {
        // 0.12.3 방식
        String accessToken = Jwts.builder()
                .subject(email)
//                .claim("authorities", authentication.getAuthorities())        // 토큰에 정보기입 최소회
                .claim("authorities", authorities)        // 토큰에 정보기입 최소회
                .claim("customerNo",customerNo)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(secretKey)
                .compact();
        log.trace("### JWT 신규 Access 토큰 생성 완료! : {}", accessToken);
        return accessToken ;
    }

    public String createRefreshToken(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return createRefreshToken(email) ;
    }

    public String createRefreshToken(String email) {
        // 0.12.3 방식
        String refreshToken = Jwts.builder()
                .subject(email)
//                .claim("clientIp",clientIp)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey)
                .compact();
        log.trace("### JWT 신규 RefreshToken 생성 완료! : {}", refreshToken);

        return refreshToken ;
    }

    public long getRefreshTokenExpirationMillis() {
        return this.refreshExpirationMs ;
    }


    public void deleteJWT(HttpServletResponse response, String accessToken, String refreshToken) {

        // 로그아웃 시 Redis에 RefreshToken 삭제 ( key = Email / value = Refresh Token )
        String email = null ;
        try {
            email = getClaims(refreshToken).getSubject() ;
            redisService.setValues("blacklist:" + accessToken, "logout", accessExpirationMs);
            log.trace("### BLACKLIST:accessToken Redis 등록 완료!");
            redisService.deleteValue(email);
            log.trace("### RefreshToken Redis 삭제 완료!");
        } catch (RuntimeException e) {
            log.trace("### [logout] JWT 토큰이 잘못되었습니다. {}", e.getMessage());
        }

        // 쿠키에 AccessToken 삭제
        ResponseCookie accessTokenCookie = ResponseCookie.from(TokenProvider.ACCESS_TOKEN_COOKIE, "")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        log.trace("### AccessToken 쿠키 삭제 완료!");

        // 쿠키에 RefreshToken 저장
        ResponseCookie refreshTokenCookie = ResponseCookie.from(TokenProvider.REFRESH_TOKEN_COOKIE, "")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        log.trace("### RefreshToken 쿠키 삭제 완료!");
    }

    public void addJWT2Cookie(HttpServletResponse response, String accessToken, String encryptedRefreshToken) {

        // 응답 헤더에 JWT 토큰 저장
        response.setHeader(HttpHeaders.AUTHORIZATION, TokenProvider.BEARER_TYPE + " " + URLEncoder.encode(accessToken, StandardCharsets.UTF_8));
        response.setHeader(TokenProvider.REFRESH_TOKEN_COOKIE, URLEncoder.encode(encryptedRefreshToken, StandardCharsets.UTF_8));
        log.trace("### 응답 헤더에 JWT Token 저장 완료!");

        // 로그인 성공시 Redis에 RefreshToken 저장 ( key = Email / value = Refresh Token )
        long refreshTokenExpirationMillis = getRefreshTokenExpirationMillis();
        redisService.setValues(getClaims(encryptedRefreshToken).getSubject(), encryptedRefreshToken, Duration.ofMillis(refreshTokenExpirationMillis));
        log.trace("### RefreshToken Redis 저장 완료!");

        // 쿠키에 AccessToken 저장
        ResponseCookie accessTokenCookie = ResponseCookie.from(TokenProvider.ACCESS_TOKEN_COOKIE, URLEncoder.encode(accessToken, StandardCharsets.UTF_8))
                .httpOnly(true)
                .secure(true)
//                .domain("https://my-pizza.io:90")
                .path("/")  // 모든 경로에서 쿠키에 접근 가능하도록 설정
                .sameSite("Strict") // SameSite 설정
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        log.trace("### AccessToken 쿠키 저장!");

        // 쿠키에 RefreshToken 저장
        ResponseCookie refreshTokenCookie = ResponseCookie.from(TokenProvider.REFRESH_TOKEN_COOKIE, URLEncoder.encode(encryptedRefreshToken, StandardCharsets.UTF_8))
                .httpOnly(true)
                .secure(true)
//                .domain("https://my-pizza.io:90")
                .path("/")  // 모든 경로에서 쿠키에 접근 가능하도록 설정
                .sameSite("Strict") // SameSite 설정
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        log.trace("### RefreshToken 쿠키 저장!");
    }

    public String getTokenFromRedis(String email) {
        return (String)redisService.getValue(email) ;
//        boolean isOk = false ;
//
//        if ( email!=null && refreshToken!=null && refreshToken.equals(redisService.getValue(email)) ) {
//            isOk = true ;
//        }
//
//        return isOk ;
    }
}
