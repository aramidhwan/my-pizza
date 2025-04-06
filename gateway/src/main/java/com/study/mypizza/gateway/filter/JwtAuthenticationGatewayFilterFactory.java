package com.study.mypizza.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.gateway.dto.ResponseDto;
import com.study.mypizza.gateway.jwt.TokenProvider;
import io.jsonwebtoken.MalformedJwtException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

//@Component
@Slf4j
// https://gose-kose.tistory.com/27
// AbstractGatewatFilterFactory는 Spring Cloud Gateway에서 GatewayFilter를 생성하는 팩토리 클래스입니다.
// 이 팩토리를 활용하면, 필터 인스턴스를 생성 및 초기화, 구성을 할 수 있습니다.
// 구체적으로 , 팩토리로 생성된 인스턴스는 SpringBean으로 등록되며, Spring ApplicationContext에서 사용할 수 있습니다.
// 또한 인스턴스의 설정 값을 지정할 수 있고, 인스턴스의 종속성을 주입하거나 필터 인스턴스 간의 관계를 설정할 수 있습니다.
//
// SecurityWebFilterChain는 Spring WebFlux에서 사용하는 SecurityFitlerChain입니다.
// Spring Security 최신 버전은 SecurityFilterChain을 활용하여 SecurityConfig를 작성하도록 하고 있습니다.
// 이때, 주의할 점은 SecurityFilterChain은 Spring MVC에서 적용하는 보안 필터 체인이기 때문에
// 현재 사용하고 있는 프레임워크에 따라 다른 필터를 적용하여야 합니다.
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private final TokenProvider tokenProvider;

    public JwtAuthenticationGatewayFilterFactory(TokenProvider tokenProvider) {
        super(Config.class);
        this.tokenProvider = tokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        // "pre" filter 방식임
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            String requestPath = request.getPath().toString() ;
            String requestPath2 = request.getURI().getPath() ;
            log.trace("### requestPath. : {}", requestPath);
            log.trace("### requestPath2. : {}", requestPath2);

            // 2-1. 토큰이 필요하지 않은 API URL의 경우 -> 검증 처리없이 다음 필터로 이동한다.
            boolean skipURI = EXCLUDE_URL.stream().anyMatch(exclude -> exclude.equalsIgnoreCase(requestPath));
            if (skipURI) {
                log.trace("### SKIP authenticate URI. : {}", requestPath);
            } else {
                // 2-2. 토큰이 필요하지 않은 API URL PREFIX의 경우 ->
                // 검증 처리없이 다음 필터로 이동한다.
                skipURI = EXCLUDE_PREFIX_URL.stream().anyMatch(excludePrefix -> requestPath.startsWith(excludePrefix));

                if (skipURI) {
                    log.trace("### SKIP authenticate URI Prefix. : {}", requestPath);
                }
            }

            // 토큰이 필요하지 않은 API URL의 경우
            if (skipURI) {
                // 검증 처리없이 다음 필터로 이동한다.
                return chain.filter(exchange);
            }

            // JWT 토큰 추출
            String jwtToken = resolveJWTToken(request) ;
            String errorMsg = null ;

            try {
                // JWT 토큰 유효성 인증 성공! next filter
                if (jwtToken!=null && tokenProvider.validateToken(jwtToken)) {
                    log.trace("### JWT 토큰 유효성 인증 성공! {}",requestPath);
                    // 다음 필터로 이동한다.
                    return chain.filter(exchange);
                } else {
                    errorMsg = "### JWT 토큰이 없습니다." ;
                }
            } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
                errorMsg = "### 잘못된 JWT 토큰입니다. ["+requestPath+"]" ;
                log.trace(errorMsg, e.getMessage());
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                errorMsg = "### 만료된 JWT 서명입니다. ["+requestPath+"]" ;
                log.trace(errorMsg, e.getMessage());
            } catch (io.jsonwebtoken.UnsupportedJwtException e) {
                errorMsg = "### 지원되지 않는 JWT 토큰입니다. ["+requestPath+"]" ;
                log.trace(errorMsg, e.getMessage());
            } catch (IllegalArgumentException e) {
                errorMsg = "### JWT 토큰이 잘못되었습니다. ["+requestPath+"]" ;
                log.trace(errorMsg, e.getMessage());
            }

            // JWT 토큰 유효성 인증 실패 Response
            ObjectMapper objectMapper = new ObjectMapper();
            ResponseDto responseDto = ResponseDto.builder()
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .BIZ_SUCCESS(1)
                    .msg(errorMsg)
                    .build() ;

            byte[] jsonBody = null;
            String jsonString = null ;
            try {
                jsonBody = objectMapper.writeValueAsBytes(responseDto);
                jsonString = objectMapper.writeValueAsString(responseDto);
            } catch (JsonProcessingException e) {
                jsonBody = e.getMessage().getBytes();
            }
            log.trace("### jsonBody : {}",jsonBody);
            log.trace("### jsonBody : {}",jsonString);

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonBody);
            exchange.getResponse().writeWith(Flux.just(buffer));
            return exchange.getResponse().setComplete();
        };
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
//                log.trace("### Cookie {}:{}", key, value);
//            });
            List<HttpCookie> httpCookies = request.getCookies().get("JWTAccessToken") ;
            if (httpCookies!=null && !httpCookies.isEmpty()) {
                authorizationString = httpCookies.get(0).getValue() ;
                log.trace("### Get AccessToken from COOKIE! [{}]",authorizationString);
            }

            if ( authorizationString == null ) {
                log.trace("### authorizationString is NULL!! Path : {}", request.getPath().toString());
            }
        } else {
            log.trace("### Get AccessToken from HEADER! [{}]",authorizationString);
            authorizationString = authorizationString.substring(TokenProvider.BEARER_TYPE.length()+1) ;
        }

        return authorizationString;
    }

    // 설정파일에 있는 args
    @Getter
    @Setter
    public static class Config{
        private String granted; // Bearer
    }

    // JWT 토큰 검증에서 제외할 url
    private static final List<String> EXCLUDE_URL =
            List.of(
                    "/css/**", "/js/**", "/images/**"   // static content
                    ,"/auth/loginPage.html"  // 로그인 페이지의 URL을 추가합니다.
                    ,"/auth/sign-up"        // 회원가입 URL을 추가합니다.
                    ,"/auth/sign-in"        // 로그인 액션 URL을 추가합니다.
            );

    // JWT 토큰 검증에서 제외할 url
    private static final List<String> EXCLUDE_PREFIX_URL =
            List.of(
                    "/css/", "/js/", "/images/"   // static content
            );
}
