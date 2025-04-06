package com.study.mypizza.common.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.common.dto.ResponseDto;
import com.study.mypizza.common.exception.DuplicateMemberException;
import com.study.mypizza.common.exception.MyPizzaException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;
import java.util.Arrays;

@RestControllerAdvice
@Slf4j
public class ExceptionController {
    @ExceptionHandler(DuplicateMemberException.class)
    public ResponseEntity<ResponseDto> handleDuplicateMemberException(final DuplicateMemberException ex) {
        log.warn("### ExceptionController.handleDuplicateMemberException : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(2)     // 0: 성공, 1: JWT 토큰 관련
                .msg(ex.getMessage())
                .build() ;
        return ResponseEntity.ok(responseDto) ;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseDto> handleBadCredentialsException(final BadCredentialsException ex) throws JsonProcessingException {
        log.debug("### ExceptionController.handleBadCredentialsException : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(2)
                .msg(ex.getMessage()+"\n아이디 또는 패스워드를 확인하세요.")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto) ;
    }

    // 401 Unauthorized JWT 토큰이 만료 or 잘못되었을 경우, ROLE이 맞지 않을 경우
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDto> handleAccessDeniedException(final AccessDeniedException ex) {
        log.debug("### handleAccessDeniedException : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(2)
                .msg(ex.getMessage()+"\n접근 권한이 없습니다. 관리자에게 문의하세요.")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto) ;
    }

    // 401 UNAUTHORIZED 사용자가 인증되지 않았거나 유효한 인증 정보가 부족하여 요청이 거부된 것을 말한다.
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ResponseDto> InsufficientAuthenticationException(final InsufficientAuthenticationException ex) {
        log.debug("### handleInsufficientAuthenticationException : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(2)
                .msg(ex.getMessage()+"\n인증이 필요합니다. 로그인 후 이용하세요.")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto) ;
    }

    // 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDto> handleRuntimeException(final RuntimeException ex) throws JsonProcessingException {
        log.warn("### ExceptionController.handleRuntimeException : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(9)
                .msg(ex.getMessage())
                .build();
        return ResponseEntity.ok(responseDto) ;
    }

    // MyPizzaException
    @ExceptionHandler(MyPizzaException.class)
    public ResponseEntity<ResponseDto> handleMyPizzaException(final MyPizzaException ex) {
        log.warn("### ExceptionController.handleMyPizzaException : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(2)
                .msg(ex.getMessage())
                .build();
        return ResponseEntity.ok(responseDto) ;
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleAll(final Exception ex) {
        log.error("### ExceptionController.handleAll : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(9)
                .msg(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto) ;
    }

}
