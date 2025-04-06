package com.study.mypizza.store.filter;

import com.study.mypizza.store.jwt.TokenProvider;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;

    // JWT 토큰 검증에서 제외할 url
    private static final List<String> EXCLUDE_URL =
            List.of(
                    "/css/**", "/js/**", "/images/**"   // static content
            );

    // JWT 토큰 검증에서 제외할 url
    private static final List<String> EXCLUDE_PREFIX_URL =
            List.of(
                    "/css/", "/js/", "/images/", "/html/"   // static content
            );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Request Header 에서 JWT 토큰 추출
        String jwtToken = resolveAccessToken(request) ;

        try {
            // 토큰에서 Authentication 객체를 가지고 와서 SecurityContext 에 저장
            Authentication authentication = tokenProvider.getAuthentication(jwtToken);
            // SecurityContextHolder 에 시큐리티용 인증객체(Authentication) 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("### 잘못된 JWT 서명입니다. [{}]", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.trace("### 만료된 JWT 토큰입니다. [{}]", e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.trace("### 지원되지 않는 JWT 토큰입니다. [{}]", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.trace("### JWT 토큰이 잘못되었습니다. [{}]", e.getMessage());
        }

        // 로그인 미수행 시 이 속의 AuthorizationFilter 에서 AccessDeniedException: Access Denied 발생
        // --> 이 경우에는 AuthenticationEntryPoint 를 잘 수행한다.
        filterChain.doFilter(request, response);
    }

    // 요청 헤더에서 JWT AccessToken을 추출해서 리턴
    private String resolveAccessToken(@NotNull HttpServletRequest request) {
        // request에서 Authorization 헤더를 찾음
        String authorizationString = request.getHeader(HttpHeaders.AUTHORIZATION) ;

        if ( authorizationString == null ) {
            // 쿠키에서 JWT 토큰 추출
            Cookie[] cookies = request.getCookies() ;
            if ( cookies != null ) {
                for (Cookie cookie:cookies) {
                    if ("JWTAccessToken".equals(cookie.getName())) {
                        log.trace("### Get AccessToken from COOKIE!!");
                        authorizationString = cookie.getValue() ;
                        break;
                    }
                }
            }

            if ( authorizationString == null ) {
                log.trace("### AccessToken authorizationString is NULL!! Path : {}", request.getRequestURI());
            }
        } else {
            log.trace("### Get AccessToken from HEADER!");
            authorizationString = authorizationString.substring("Bearer".length()+1) ;
        }

        return authorizationString ;
    }

    // EXCLUDE_URL과 동일한 요청이 들어왔을 경우, 본 필터를 진행하지 않고 다음 필터 진행(true로 리턴)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 2-1. 토큰이 필요하지 않은 API URL의 경우 -> 검증 처리없이 다음 필터로 이동한다.
        boolean skipURI = EXCLUDE_URL.stream().anyMatch(exclude -> exclude.equalsIgnoreCase(request.getServletPath()));
        if (skipURI) {
            log.trace("### SKIP authenticate URI. : {}", request.getServletPath());
        } else {
            // 2-2. 토큰이 필요하지 않은 API URL PREFIX의 경우 ->
            // 검증 처리없이 다음 필터로 이동한다.
            skipURI = EXCLUDE_PREFIX_URL.stream().anyMatch(excludePrefix -> request.getServletPath().startsWith(excludePrefix));

            if (skipURI) {
                log.trace("### SKIP authenticate URI Prefix. : {}", request.getServletPath());
            }
        }

        // 토큰이 필요하지 않은 API URL의 경우
        if (skipURI) {
            // 검증 처리없이 다음 필터로 이동한다.
            return skipURI;
        }

        return skipURI;
    }

}
