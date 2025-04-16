package com.study.mypizza.mypage.controller;

import com.study.mypizza.mypage.exception.MyPizzaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;

@RestControllerAdvice
@Slf4j
public class ExceptionController {
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Object> handleSignatureException(final SignatureException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 변조되었습니다.");
    }
    
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Object> handleSecurityException(final SecurityException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 유효하지 않습니다.");
    }

    // 500
    @ExceptionHandler(MyPizzaException.class)
    public ResponseEntity<Object> handleMyPizzaException(final MyPizzaException ex) {
        log.debug("### ExceptionController.handleMyPizzaException : {}", ex.getMessage());
        return ResponseEntity.ok("{\"msg\": \""+ex.getMessage().replace('"','\'')+"\"}");
    }

    // 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(final RuntimeException ex) {
        log.warn("### ExceptionController.handleRuntimeException : {}", ex.getMessage());
        log.error("error", ex);
        return ResponseEntity.badRequest().body("{\"msg\": \"관리자에게 문의하십시요. : "+ex.getMessage().replace('"','\'')+"\"}");
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(final Exception ex) {
        log.error("### ExceptionController.handleAll : {}", ex.getMessage());
        log.error("error", ex);
        return new ResponseEntity<>("{\"msg\": \"관리자에게 문의하십시요. : "+ex.getMessage().replace('"','\'')+"\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
