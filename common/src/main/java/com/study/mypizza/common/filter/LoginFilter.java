package com.study.mypizza.common.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.dto.LoginDto;
import com.study.mypizza.common.dto.ResponseDto;
import com.study.mypizza.common.handler.LoginFailureHandler;
import com.study.mypizza.common.handler.LoginSuccessHandler;
import com.study.mypizza.common.jwt.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/*
    POST 방식의 Request Parameter (Not BODY-JSON) 방식으로 로그인 요청을 처리하기 위한 필터임.

   formLogin 방식을 disable 안 했으면 UsernamePasswordAuthenticationFilter가 자동으로 실행될텐데
   SecurityConfig에서 formLogin을 disable 시켰으므로 본 Filter를 만들어 등록해 주어야 한다.
 */
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager ;
    private final TokenProvider tokenProvider;

    public LoginFilter(AuthenticationManager authenticationManager, TokenProvider tokenProvider, LoginSuccessHandler loginSuccessHandler, LoginFailureHandler loginFailureHandler) {
        this.authenticationManager = authenticationManager ;
        setAuthenticationManager(authenticationManager) ;
        this.tokenProvider = tokenProvider;
        // 이 UsernamePasswordAuthenticationFilter 필터는 "/login"에 접근할 때만 동작한다.(하드코딩)
        // 그렇기 때문에 내가 원하는 Url에서 필터가 동작하길 원한다면 setFilterProcessesUrl()로 Url를 설정해줘야 작동한다.
        setFilterProcessesUrl("/api/auth/sign-in");
        setUsernameParameter(TokenProvider.JWT_SUBJECT_NAME);
        setAuthenticationSuccessHandler(loginSuccessHandler);
        setAuthenticationFailureHandler(loginFailureHandler);
    }

    // 로그인 인증 수행
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        ObjectMapper objectMapper = new ObjectMapper() ;
//        UserDetails userDetails = null ;
        LoginDto userDetails = null ;

        // 1. Client Request 로 부터 Parameter 방식이 아닌 JSON 방식으로 ID/PASSWORD 얻어오는 경우
        try {
//            userDetails = objectMapper.readValue(request.getInputStream(), CustomerDto.class);
            userDetails = objectMapper.readValue(request.getInputStream(), LoginDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e) ;
        }

        // 3. Authority 얻어오기
        log.trace("### 로그인 인증 시도 attemptAuthentication : Principal : {}, Password : {}", userDetails.getUsername(), userDetails.getPassword());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword());

        // Allow subclasses to set the "details" property
        this.setDetails(request, authenticationToken);

        // 아이디, 패스워드 인증 방식에선 DaoAuthenticationProvider를 사용한다.
        // 이 안에서 DaoAuthenticationProvider 를 통해 UserDetailsService.loadUserByUsername 호출(로그인 액션 시도)
        // Authenticating request with DaoAuthenticationProvider 라고 콘솔에 찍힘
        log.trace("### About To call UserDetailsService.loadUserByUsername().");
        return this.getAuthenticationManager().authenticate(authenticationToken);
//        return authenticationManager.authenticate(authenticationToken);
    }

    // 아래 successfulAuthentication() 메소드를 override 하지 않으면
    // extends AbstractAuthenticationProcessingFilter 의 successfulAuthentication()가 실행되고
    // 거기에서 [SecurityContext.setAuthentication(authResult)] 처리를 한 후, successHandler 를 호출하게 된다.
    // 여기에서 override를 수행한 이유는 "필터에서 response를 직접 처리하지 말고 요청을 계속 진행시켜 Controller를 호출"하기 위함이다.
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
//                                            Authentication authResult) throws IOException, ServletException {
//        super.successfulAuthentication(request, response, chain, authResult) ;
//    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        log.trace("### 로그인 인증 실패!");
        // 아래를 호출하면 LoginFailureHandler로 이동한다.
        super.unsuccessfulAuthentication(request, response, failed) ;
    }
}
