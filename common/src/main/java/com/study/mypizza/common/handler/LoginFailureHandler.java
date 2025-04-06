package com.study.mypizza.common.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.net.URLEncoder;

@Component
@NoArgsConstructor
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.trace("### onAuthenticationFailure() : {}", exception.getMessage());
//        String errorMessage = null;
//
//        if(exception instanceof BadCredentialsException) {
//            errorMessage = "아이디 또는 비밀번호가 맞지 않습니다. 다시 확인해주세요.";
//        } else if (exception instanceof InternalAuthenticationServiceException) {
//            errorMessage = "내부 시스템 문제로 로그인 요청을 처리할 수 없습니다. 관리자에게 문의하세요. ";
//        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
//            errorMessage = "인증 요청이 거부되었습니다. 관리자에게 문의하세요.";
//        } else {
//            errorMessage = "알 수 없는 오류로 로그인 요청을 처리할 수 없습니다. 관리자에게 문의하세요.";
//        }
//
//        errorMessage = URLEncoder.encode(errorMessage, "UTF-8"); /* 한글 인코딩 깨진 문제 방지 */

        // ExceptionController 에서 처리하기 위한 메소드 호출
        resolver.resolveException(request, response, null, exception);

//        setDefaultFailureUrl("/auth/loginPage?error=true&errorMessage=" + errorMessage);
//        super.onAuthenticationFailure(request, response, exception) ;
    }

}
