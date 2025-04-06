package com.study.mypizza.common.controller;

import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.service.CustomerService;
import com.study.mypizza.common.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    private final CustomerService customerService ;

    @GetMapping("/")
    public String redirectToIndex() {
        return "/html/index";
    }

//    @GetMapping("/")
//    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
//    public ResponseEntity<String> first() {
//        return ResponseEntity.ok().body("Welcome to MY-PIZZA") ;
//    }

    @GetMapping("/html/index")
    public String mainPage() {
        return "/html/index";
    }

    @GetMapping("/common/layout")
    public String layout() {
        return "/common/layout" ;
    }

//    @GetMapping("/mainPage")
//    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
//    public ResponseEntity<String> mainPage() {
//        log.trace("### 메소드 [{}]", Thread.currentThread().getStackTrace()[1].getMethodName());
//
//        return ResponseEntity.ok().body("This is Main Page for [ROLE_CUSTOMER] authorized Customer.") ;
//    }

    @GetMapping("/vip-order")
    @Secured("ROLE_VIP")
    public ResponseEntity<String> vipOrder() {
        return ResponseEntity.ok().body("This is Vip-Order Page only for [ROLE_VIP] Role Customer.") ;
    }

    @GetMapping("/order")
    @Secured("ROLE_CUSTOMER")
    public ResponseEntity<String> order() {
        return ResponseEntity.ok().body("This is Order Page only for [ROLE_CUSTOMER] Role Customer.") ;
    }
}
