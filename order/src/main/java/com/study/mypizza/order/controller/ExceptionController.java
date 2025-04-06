package com.study.mypizza.order.controller;

import com.study.mypizza.order.dto.ResponseDto;
import com.study.mypizza.order.exception.MyPizzaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;
import java.util.Arrays;

@RestControllerAdvice
@Slf4j
public class ExceptionController {
    // 401 Unauthorized JWT 토큰이 만료 or 잘못되었을 경우, ROLE이 맞지 않을 경우
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDto> handleAccessDeniedException(final AccessDeniedException ex) {
        log.warn("### handleAccessDeniedException : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .BIZ_SUCCESS(2)     // 0: 성공, 1: JWT 토큰 관련
                .msg("⚠️ 접근 권한이 없습니다. 관리자에게 문의하세요. "+ex.getMessage())
                .build() ;
        return ResponseEntity.ok(responseDto) ;
    }

    // 500
    @ExceptionHandler(MyPizzaException.class)
    public ResponseEntity<ResponseDto> handleMyPizzaException(final MyPizzaException ex) {
        log.debug("### ExceptionController.handleMyPizzaException : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .httpStatus(HttpStatus.OK)
                .BIZ_SUCCESS(2)     // 0: 성공, 1: JWT 토큰 관련
                .msg(ex.getMessage())
                .build() ;
        return ResponseEntity.ok(responseDto) ;
    }

    // 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDto> handleRuntimeException(final RuntimeException ex) {
        log.warn("### ExceptionController.handleRuntimeException : {}", ex.getMessage());
        ex.printStackTrace();
        ResponseDto responseDto = ResponseDto.builder()
                .httpStatus(HttpStatus.OK)
                .BIZ_SUCCESS(2)     // 0: 성공, 1: JWT 토큰 관련
                .msg("⚠️ 관리자에게 문의하십시요. : "+ex.getMessage())
                .build() ;
        return ResponseEntity.ok(responseDto) ;
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(final Exception ex) {
        log.error("### ExceptionController.handleAll : {}", ex.getMessage());
        log.error("error", ex);
        return new ResponseEntity<>("{\"msg\": \"관리자에게 문의하십시요. : "+ex.getMessage().replace('"','\'')+"\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
