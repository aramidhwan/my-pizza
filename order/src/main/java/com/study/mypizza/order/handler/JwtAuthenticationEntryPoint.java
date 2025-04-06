package com.study.mypizza.order.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    // 인증이 안된 익명의 사용자가 인증이 필요한 엔드포인트로 접근하게 된다면 Spring Security의 기본 설정으로는 HttpStatus 401과 함께 스프링의 기본 오류페이지를 보여준다.
    // 기본 오류 페이지가 아닌 커스텀 오류 페이지를 보여준다거나, 특정 로직을 수행 또는 JSON 데이터 등으로 응답해야 하는 경우,
    // 우리는 AuthenticationEntryPoint 인터페이스를 구현하고 구현체를 시큐리티에 등록하여 사용할 수 있다.
    // AuthenticationEntryPoint 인터페이스는 인증되지 않은 사용자가 인증이 필요한 요청 엔드포인트로 접근하려 할 때,
    // 예외를 핸들링 할 수 있도록 도와준다.
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        log.warn("### msg : {}", authException.getMessage());

        // ExceptionController 에서 처리하기 위한 메소드 호출
        resolver.resolveException(request, response, null, authException);

        // "401 UNAUTHORIZED" Indicates that the client lacks valid authentication credentials
        // Occurs when authentication is required but has failed or hasn't been provided
        // To fix, the user needs to provide valid authentication credentials, such as a username and password or an access token

//        response.setCharacterEncoding("utf-8");
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied : 잘못된 접근입니다.");

//        response.setCharacterEncoding("utf-8");
//        response.setContentType("text/html; charset=utf-8");
//        response.getWriter().write("{\"msg\":\"로그인이 필요합니다.\"}");
    }
}
