package com.study.mypizza.common.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.common.config.AES128Config;
import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.dto.LoginDto;
import com.study.mypizza.common.dto.ResponseDto;
import com.study.mypizza.common.jwt.TokenProvider;
import com.study.mypizza.common.service.CustomerService;
import com.study.mypizza.common.service.LoginService;
import com.study.mypizza.common.service.RedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
// JWT AccessToken 생성한다.
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
//public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final CustomerService customerService;
    private final TokenProvider tokenProvider ;
    private final AES128Config aes128Config;

    @Override
    // 이 부분은 LoginFilter extends AbstractAuthenticationProcessingFilter 의 successfulAuthentication() 에서
    // [SecurityContext.setAuthentication(authentication)] 처리를 한 후, successHandler 를 호출하게 된다.
    //
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // SecurityContext.setAuthentication() 처리
        // ★★★ --> SecurityContext는 인증 성공 후에 기본적으로는 저장하지 않는다. ★★★
        // UsernamePasswordAuthenticationFilter는 form-login 기반으로, SSO 처리시에는 호출 되지 않기 때문에
        // SessionManagementFilter에 인증정보를 감지할 수 없다.
        // 그렇기 때문에 UsernamePasswordAuthenticationFilter는 인증 성공 후에 인정된 객체를 SecurityContext에 저장해야 한다.
        // SecurityContext에 저장하기 위하여는 AbstractAuthenticationProcessingFilter 의 successfulAuthentication를 재정의하여야 한다.
        // 출처: https://kamsi76.tistory.com/entry/Spring-Security-60-SSO-로그인-시-Anonymous-user로-처리-될-때 [Kamsi's Blog:티스토리]
        log.trace("### 이 라인 앞에서 SecurityContext.setAuthentication(authentication) 처리 완료! --> AbstractAuthenticationProcessingFilter.successfulAuthentication() 에서 처리됨");

        // 로그인 인증 성공 시 호출될 URL
//        String redirectUrl = ((LoginDto) authentication.getPrincipal()).getRedirectUrl();
//        log.trace("### redirectUrl :: {}", redirectUrl);

        // extends SimpleUrlAuthenticationSuccessHandler 필요함
//        setDefaultTargetUrl("/index") ;
//        handle(request, response, authentication);
//        clearAuthenticationAttributes(request);     // session.removeAttribute() 수행
        
        // JWT AccessToken, RefreshToken 생성
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        CustomerDto customerDto = customerService.getCustomer(email) ;
        log.trace("### 로그인 인증 성공! 사용자 : {}", customerDto.getEmail());

        String accessToken  = tokenProvider.createAccessToken(authentication, customerDto);
        String refreshToken  = tokenProvider.createRefreshToken(authentication);
//        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        String encryptedRefreshToken = refreshToken ;

        // 쿠키에 JWT 토큰 저장 (Redis 저장 포함)
        try {
            tokenProvider.addJWT2Cookie(response, accessToken, encryptedRefreshToken);
        } catch (RedisSystemException e) {
            throw new RuntimeException(e);
        }
        // 로그인 성공 응답 Body Json 작성
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
//                .msg("로그인 성공!!")  // msg 셋팅 시 화면에서 alert(msg) 창이 뜬다.
//                .data(redirectUrl)
                .build() ;

        // JSON 형태로 변환하기
        try {
            response.getWriter().write(new ObjectMapper().writeValueAsString(responseDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
