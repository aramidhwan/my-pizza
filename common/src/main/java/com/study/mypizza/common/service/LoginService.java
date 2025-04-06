package com.study.mypizza.common.service;

import com.study.mypizza.common.dto.AuthorityDto;
import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.entity.Authority;
import com.study.mypizza.common.entity.Customer;
import com.study.mypizza.common.exception.DuplicateMemberException;
import com.study.mypizza.common.exception.MyPizzaException;
import com.study.mypizza.common.jwt.TokenProvider;
import com.study.mypizza.common.repository.CustomerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
// UserDetailsService는 DaoAuthenticationProvider에 의해서 사용되어진다.
// 이는 스프링 시큐리티에서 유저정보를 가져오는데 활용된다.
public class LoginService implements UserDetailsService {         // 회원가입, 로그인 서비스
    private final CustomerRepository customerRepository;
    private final RedisService redisService;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 로그아웃
    @Transactional(rollbackFor = MyPizzaException.class)
    public void logout(HttpServletResponse response, String accessToken, String refreshToken) {
        tokenProvider.deleteJWT(response, accessToken, refreshToken) ;
    }

    // 회원가입
    @Transactional(rollbackFor = MyPizzaException.class)
    public CustomerDto signUp(CustomerDto customerDto) {
        if (customerRepository.existsByCustomerId(customerDto.getCustomerId())) {
            throw new DuplicateMemberException("이미 사용중인 ID 입니다. ["+customerDto.getCustomerId()+"]");
        }
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new DuplicateMemberException("이미 사용중인 email 입니다. ["+customerDto.getEmail()+"]");
        }

        Set<Authority> authorities = new HashSet<>();
        authorities.add(Authority.builder().authorityName("ROLE_CUSTOMER").build()) ;

        Customer customer = Customer.builder()
                .customerId(customerDto.getCustomerId())
                .customerName(customerDto.getCustomerId())
                .email(customerDto.getEmail())
                .password(passwordEncoder.encode(customerDto.getPassword()))
                .authorities(authorities)
                .activated(true)
                .build();

        return CustomerDto.of(customerRepository.save(customer)) ;
    }

    // 로그인 액션
    // LoginFilter/UsernamePasswordAuthenticationFilter.attemptAuthentication 에서 여기로 호출됨
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("### LoginService.loadUserByUsername() 로그인 시도 email : {}", email);

        Customer customer = customerRepository.findOneByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 e-mail 입니다. [" + email + "]"));

        CustomerDto customerDto = new CustomerDto(customer.getEmail(), customer.getPassword(), customer.getAuthorities().stream().map(AuthorityDto::of).toList()) ;
        Hibernate.initialize(customerDto.getAuthorities());

        return customerDto ;
    }
}
