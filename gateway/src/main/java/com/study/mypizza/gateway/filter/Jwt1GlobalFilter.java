package com.study.mypizza.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.gateway.enums.JWTAuth;
import com.study.mypizza.gateway.dto.ResponseDto;
import com.study.mypizza.gateway.jwt.TokenProvider;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
// https://gose-kose.tistory.com/27
// GatewatFilter는 Spring Cloud Gateway에서 Http 요청 및 응답을 필터링하는 역할을 수행합니다.
// 구체적으로 <인증 및 권한 부여, 요청 및 응답 수정, 요청 거부> 등의 역할을 수행하며,
// LoggingFilter로 로그 기록을 남기거나 ReWritePathFilter로 요청 경로를 수정할 수 있습니다.
// GatewayFilter의 인터페이스를 구현한다면 추가적인 로직을 작성할 수 있습니다.
public class Jwt1GlobalFilter implements GlobalFilter, Ordered {

    private final TokenProvider tokenProvider;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE ;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.trace("🔍 Gateway 요청 URI: {}", exchange.getRequest().getURI().toString());  // 로그로 반드시 찍어보세요
        JWTAuth okGo = JWTAuth.NO_JWT ;
        String errorMsg = null ;
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String requestPath = request.getURI().getPath() ;
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDto responseDto = null ;

        if ("/favicon.ico".equals(requestPath)) {
            log.info("🔍 /favicon.ico 요청 감지됨. 응답 코드 204 반환");
            exchange.getResponse().setStatusCode(HttpStatus.NO_CONTENT);
            return exchange.getResponse().setComplete();
        }

        // 2-1. 토큰이 필요하지 않은 API URL의 경우 -> 검증 처리없이 다음 필터로 이동한다.
        boolean skipURI = EXCLUDE_URL.stream().anyMatch(exclude -> exclude.equalsIgnoreCase(requestPath));
        if (skipURI) {
            log.trace("🔍 SKIP authenticate URI. : {}", requestPath);
        } else {
            // 2-2. 토큰이 필요하지 않은 API URL PREFIX의 경우 ->
            // 검증 처리없이 다음 필터로 이동한다.
            skipURI = EXCLUDE_CONTAINS_URL.stream().anyMatch(excludePrefix -> requestPath.contains(excludePrefix));

            if (skipURI) {
                log.trace("🔍 SKIP authenticate URI Contains. : {}", requestPath);
            }
        }

        // 토큰이 필요하지 않은 API URL의 경우
        if (skipURI) {
            // 검증 처리없이 다음 필터로 이동한다.
            okGo = JWTAuth.OK ;
            
        // JWT 토큰 검증 수행
        } else {
            log.debug("🔍 Jwt1GlobalFilter 시작 : {}", requestPath);

            // JWT 토큰 추출
            String jwtToken = resolveJWTToken(request) ;

            try {
                // JWT 토큰 유효성 인증 성공! next filter
                if ( jwtToken == null ) {
                    errorMsg = "🔍 JWT 토큰이 없습니다. ["+requestPath+"]" ;
                    log.debug(errorMsg);
                    okGo = JWTAuth.NO_JWT ;
                } else if ( tokenProvider.validateToken(jwtToken) ) {
                    log.debug("🔍 JWT 토큰 유효성 인증 성공! {}",requestPath);
                    // 다음 필터로 이동한다.
                    okGo = JWTAuth.OK ;
                } else {
                    errorMsg = "🔍 JWT 토큰 Validation 실패! ["+requestPath+"]" ;
                    log.debug(errorMsg);
                    okGo = JWTAuth.WRONG_JWT ;
                }
            } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
                errorMsg = "🔍 잘못된 JWT 토큰입니다. ["+requestPath+"]" ;
                okGo = JWTAuth.WRONG_JWT ;
                log.debug(errorMsg, e.getMessage());
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                errorMsg = "🔍 만료된 JWT 서명입니다. ["+requestPath+"]["+jwtToken+"]" ;
                okGo = JWTAuth.JWT_EXPIRED ;
                log.debug(errorMsg, e.getMessage());
//                handleExpiredToken(exchange, chain) ;
            } catch (io.jsonwebtoken.UnsupportedJwtException e) {
                errorMsg = "🔍 지원되지 않는 JWT 토큰입니다. ["+requestPath+"]" ;
                okGo = JWTAuth.WRONG_JWT ;
                log.debug(errorMsg, e.getMessage());
            } catch (IllegalArgumentException e) {
                errorMsg = "🔍 JWT 토큰이 잘못되었습니다. ["+requestPath+"]" ;
                okGo = JWTAuth.WRONG_JWT ;
                log.debug(errorMsg, e.getMessage());
            }
        }

        // JWT 검증 통과
        if (okGo == JWTAuth.OK) {
            return chain.filter(exchange);
        } else if (okGo == JWTAuth.JWT_EXPIRED) {
            log.debug("🔍 JWT 토큰 재발급 URL Redirect : {}", tokenProvider.REFRESH_TOKEN_URL);
            // 토큰 만료 시 리다이렉트 처리
            exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
            exchange.getResponse().getHeaders().setLocation(URI.create(tokenProvider.REFRESH_TOKEN_URL+"?redirect="+requestPath));
            return exchange.getResponse().setComplete();
        // JWT 토큰이 아예 없을 경우
        } else if (okGo == JWTAuth.NO_JWT) {
            // JWT 토큰 유효성 인증 실패 Response
            responseDto = ResponseDto.builder()
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .BIZ_SUCCESS(1)     // BIZ_SUCCESS : 1      # JWT 인증관련 오류
                    .jwtAuth(okGo)
                    .msg(errorMsg)
                    .build() ;
        // JWT 검증 미통과
        } else {
            log.debug("🔍 JWT 검증 미통과 : " + okGo);
            // JWT 토큰 유효성 인증 실패 Response
            responseDto = ResponseDto.builder()
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .BIZ_SUCCESS(1)     // BIZ_SUCCESS : 1      # JWT 인증관련 오류
                    .jwtAuth(okGo)
                    .msg(errorMsg)
                    .build() ;
        }

        byte[] jsonBody = null;
        try {
            jsonBody = objectMapper.writeValueAsBytes(responseDto);
        } catch (JsonProcessingException e) {
            jsonBody = e.getMessage().getBytes();
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonBody);
        return exchange.getResponse().writeWith(Mono.just(buffer));
//            return exchange.getResponse().setComplete() ;
    }


    // 요청 헤더에서 JWT 토큰을 추출해서 리턴
    private String resolveJWTToken(ServerHttpRequest request) {
        // request에서 Authorization 헤더를 찾음
//        HttpHeaders httpHeaders = request.getHeaders() ;
//        for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
//            System.out.println("key: " + entry.getKey() +
//                    ", value: " + entry.getValue());
//        }
        String authorizationString = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//      String authorizationString = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);

        if ( authorizationString == null ) {
            // 쿠키에서 JWT 토큰 추출
//            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
//            cookies.forEach((key,value)->{
//                log.debug("🔍 Cookie {}:{}", key, value);
//            });
            List<HttpCookie> httpCookies = request.getCookies().get(TokenProvider.ACCESS_TOKEN_COOKIE) ;
            if (httpCookies!=null && !httpCookies.isEmpty()) {
                authorizationString = httpCookies.get(0).getValue() ;
                log.debug("🔍 Get AccessToken from COOKIE! [{}]",authorizationString);
            }

            if ( authorizationString == null ) {
                log.debug("🔍 authorizationString is NULL!! Path : {}", request.getPath().toString());
            }
        } else {
            log.debug("🔍 Get AccessToken from HEADER! [{}]",authorizationString);
            authorizationString = authorizationString.substring(TokenProvider.BEARER_TYPE.length()+1) ;
        }

        return authorizationString;
    }

    // JWT 토큰 검증에서 제외할 url
    private static final List<String> EXCLUDE_URL =
            List.of(
                    "/", "/**/css/**", "/**/js/**", "/**/images/**"   // static content
                    ,"/common-service/auth/loginPage"       // 로그인 페이지의 URL을 추가합니다.
                    ,"/common-service/auth/loginPage.html"  // 로그인 페이지의 URL을 추가합니다.
                    ,"/common-service/api/auth/logout"          // 로그아웃 URL을 추가합니다.
                    ,"/common-service/api/auth/sign-up"         // 회원가입 URL을 추가합니다.
                    ,"/common-service/api/auth/sign-in"         // 로그인 액션 URL을 추가합니다.
                    ,"/common-service/api/auth/refresh-token"   // 토큰 재발급 URL을 추가합니다.
                    ,"/common-service/index.html"           // (비로그인)메인페이지를 추가합니다.
                    ,"/common-service/common/layout"        // (비로그인)메인페이지를 추가합니다.
                    ,"/order-service/orderMain"             // 로그인 액션 URL을 추가합니다.
            );

    // JWT 토큰 검증에서 제외할 url
    private static final List<String> EXCLUDE_CONTAINS_URL =
            List.of(
                    "-service/css/", "-service/js/", "-service/images/", "-service/html/"   // static content
            );
}
