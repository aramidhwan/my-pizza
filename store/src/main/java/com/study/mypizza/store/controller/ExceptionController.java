package com.study.mypizza.store.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.study.mypizza.store.dto.ResponseDto;
import com.study.mypizza.store.exception.MyPizzaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionController {
    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(final Exception ex) {
        log.error("### ExceptionController.handleAll : {}", ex.getMessage());
        log.error("error", ex);
        return new ResponseEntity<>("{\"msg\": \"관리자에게 문의하십시요. \\n\\n"+ex.getMessage().replace('"','\'')+"\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDto> handleRuntimeException(final RuntimeException ex) {
        log.error("### ExceptionController.handleRuntimeException : {}", ex.getMessage());
        ex.printStackTrace();
        ResponseDto responseDto = ResponseDto.builder()
                .httpStatus(HttpStatus.OK)
                .BIZ_SUCCESS(2)     // 0: 성공, 1: JWT 토큰 관련
                .msg(ex.getMessage()+"\n관리자에게 문의하십시요.")
                .build() ;
        return ResponseEntity.ok(responseDto) ;
    }

    // 500
    @ExceptionHandler(MyPizzaException.class)
    public ResponseEntity<ResponseDto> handleMyPizzaException(final MyPizzaException ex) throws JsonProcessingException {
        log.debug("### ExceptionController.handleMyPizzaException : {}", ex.getMessage());
        ResponseDto responseDto = ResponseDto.builder()
                .httpStatus(HttpStatus.OK)
                .BIZ_SUCCESS(2)     // 0: 성공, 1: JWT 토큰 관련
                .msg(ex.getMessage())
                .build() ;
        return ResponseEntity.ok(responseDto) ;
    }
}
