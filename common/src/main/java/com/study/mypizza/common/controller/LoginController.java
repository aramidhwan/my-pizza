package com.study.mypizza.common.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.dto.ResponseDto;
import com.study.mypizza.common.enums.JWTAuth;
import com.study.mypizza.common.handler.LoginSuccessHandler;
import com.study.mypizza.common.jwt.TokenProvider;
import com.study.mypizza.common.service.CustomerService;
import com.study.mypizza.common.service.LoginService;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static com.study.mypizza.common.enums.JWTAuth.REFRESH_EXPIRED;

@Controller
@Slf4j
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService ;
    private final CustomerService customerService ;
    private final TokenProvider tokenProvider;
    private final LoginSuccessHandler loginSuccessHandler;

    // 로그인 체크
    @GetMapping("/api/auth/checkSignIn")
    public ResponseEntity<ResponseDto> checkSignIn(Authentication authentication) {
        log.trace("### [checkSignIn] is called. ###");
        // Authentication 에서 customerNo 가져오기
        int customerNo = (int) authentication.getDetails();
        if (customerNo == 0) {
            throw new RuntimeException("### 이런 경우가 있어???") ;
        }

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 로그인 안 된 사용자는 gateway의 Jwt1GlobalFilter에서 이미 리턴된다.
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)     // 0: 성공, 1: JWT 토큰 관련
                .msg("로그인 된 사용자")
                .data(authorities)
                .build() ;

        return ResponseEntity.ok(responseDto) ;
    }

    // 로그아웃
    @GetMapping("/api/auth/logout")
    public ResponseEntity<ResponseDto> logout(HttpServletResponse response, @CookieValue(name = TokenProvider.ACCESS_TOKEN_COOKIE, required = false) String accessToken, @CookieValue(value = TokenProvider.REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        log.trace("### [logout] is called. ###");

        loginService.logout(response, accessToken, refreshToken);

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)     // 0: 성공, 1: JWT 토큰 관련
                .msg("로그아웃 하였습니다.")
                .build() ;

        return ResponseEntity.ok(responseDto) ;
    }

    // 로그인페이지
    @GetMapping("/html/auth/loginPage")
    public String loginPage(@RequestParam(name="redirectUrl", required = false) String redirectUrl, Model model) {
        log.trace("### [loginPage] is called. redirectUrl = {}",redirectUrl);
        // 여기 타면 top.html에서 "로그아웃"/"로그인"을 구별해 줄 수 있다.
        log.trace("###################### 여기 안탄다. #############################");
        log.trace("###################### 여기 안탄다. #############################");
        log.trace("###################### 여기 안탄다. #############################");

        model.addAttribute("redirectUrl", redirectUrl);

        return "html/auth/loginPage" ;
    }

    // 회원가입
    @PostMapping("/api/auth/sign-up")
    @ResponseBody
    public ResponseEntity<ResponseDto> signUp(@RequestBody CustomerDto customerDto) {
        log.trace("### [signUp] is called.");
        customerDto = loginService.signUp(customerDto) ;

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)     // 0: 성공, 1: JWT 토큰 관련
                .data(customerDto)
                .msg("회원가입을 성공하였습니다.")
                .build() ;
        return ResponseEntity.ok(responseDto) ;
    }

    // 토큰 재발급
    @GetMapping("/api/auth/refresh-token")
    public ResponseEntity<ResponseDto> refreshToken(HttpServletRequest request, HttpServletResponse response, @CookieValue(value = TokenProvider.REFRESH_TOKEN_COOKIE, defaultValue = "") String refreshToken) {
        log.trace("### [refreshToken] is called.");
        // redirectUrl은 gateway의 Jwt1GlobalFilter에서 셋팅해줌
        String redirectUrl = request.getQueryString().split("=")[1] ;
        log.trace("### redirectUrl : {}", redirectUrl);
        ResponseDto responseDto = null ;
        String email = null, errorMsg, msg = null ;
        JWTAuth okGo = JWTAuth.NO_JWT;
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED ;
        String requestPath = request.getRequestURI() ;

        try {
            email = tokenProvider.getClaims(refreshToken).getSubject() ;
            okGo = JWTAuth.OK ;
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException e) {
            errorMsg = "### 잘못된 JWT RefreshToken 입니다. ["+requestPath+"]" ;
            okGo = JWTAuth.WRONG_JWT ;
            log.trace(errorMsg, e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            errorMsg = "### 만료된 JWT RefreshToken 입니다. ["+requestPath+"]" ;
            okGo = REFRESH_EXPIRED ;
            log.trace(errorMsg, e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            errorMsg = "### 지원되지 않는 JWT RefreshToken 입니다. ["+requestPath+"]" ;
            okGo = JWTAuth.WRONG_JWT ;
            log.trace(errorMsg, e.getMessage());
        }

        // refreshToken이 정상인 경우
        if ( okGo == JWTAuth.OK ) {
            String refreshTokenFromRedis = tokenProvider.getTokenFromRedis(email);

            if (StringUtils.hasText(refreshTokenFromRedis)) {
                // Refresh 토큰이 Redis와 일치하는 경우 : 정상 재발급
                if (refreshTokenFromRedis.equals(refreshToken)) {
                    CustomerDto customerDto = customerService.getCustomer(email);

                    String authorities = customerDto.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));

                    String newAccessToken = tokenProvider.createAccessToken(email, authorities, customerDto.getCustomerNo());
                    String newRefreshToken = tokenProvider.createRefreshToken(email);
                    tokenProvider.addJWT2Cookie(response, newAccessToken, newRefreshToken);

                    httpStatus = HttpStatus.OK;
                    msg = "JWT 재발급 성공";
                    okGo = JWTAuth.JWT_REGENERATED;

                } else {
                    httpStatus = HttpStatus.UNAUTHORIZED;
                    msg = "JWT 재발급 실패(Refresh 토큰 Redis와 불일치)";
                    okGo = JWTAuth.WRONG_REFRESH;
                }

                // Refresh 토큰이 Redis에 아예 없는 경우
            } else {
                httpStatus = HttpStatus.UNAUTHORIZED;
                msg = "JWT 재발급 실패(Refresh 토큰 만료)";
                okGo = REFRESH_EXPIRED;
            }
        // refreshToken이 만료된 경우 : okGo는 이미 셋팅완료
        } else if ( okGo == REFRESH_EXPIRED ) {
            httpStatus = HttpStatus.UNAUTHORIZED ;
            msg = "JWT 재발급 실패(만료된 RefreshToken)" ;
        // refreshToken이 비정상인 경우 : okGo는 이미 셋팅완료
        } else {
            httpStatus = HttpStatus.UNAUTHORIZED ;
            msg = "JWT 재발급 실패(잘못된 RefreshToken)" ;
        }

        responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(1)     // JWT 토큰 관련
                .jwtAuth(okGo)
                .msg(msg)
                .build() ;

        return ResponseEntity.ok(responseDto) ;
    }

}
