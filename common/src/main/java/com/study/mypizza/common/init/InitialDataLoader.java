package com.study.mypizza.common.init;

import com.study.mypizza.common.dto.AuthorityDto;
import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.repository.CustomerRepository;
import com.study.mypizza.common.service.LoginService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
public class InitialDataLoader implements CommandLineRunner {

    private final Environment env;
    private final LoginService loginService; // 예시 Repository

    public InitialDataLoader(Environment env, LoginService loginService) {
        this.env = env;
        this.loginService = loginService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 프로퍼티 값 읽기 (기본값 설정 가능)
        String ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto", "none");

        // create 또는 create-drop 일 때만 실행
        if ("create".equalsIgnoreCase(ddlAuto) || "create-drop".equalsIgnoreCase(ddlAuto)) {
            insertInitialData();
        } else {
            System.out.println("✅ ddl-auto가 '" + ddlAuto + "'이므로 초기 데이터 적재를 스킵했습니다");
        }
    }

    private void insertInitialData() {
        System.out.println("✅ t_customer 테이블 초기 데이터 적재를 시작합니다... t_authority 초기 적재는 import.sql로 대체합니다.");

        // ROLE_ADMIN 등록
        CustomerDto customerDto = CustomerDto.builder()
                .customerId("aramidhwan")
                .customerName("신승환")
                .email("aramidhwan@naver.com")
                .password("shin22")
                .activated(true)
                .authorities(Set.of(
                        AuthorityDto.builder().role("ROLE_ADMIN").build(),
                        AuthorityDto.builder().role("ROLE_CUSTOMER").build(),
                        AuthorityDto.builder().role("ROLE_VIP_CUSTOMER").build(),
                        AuthorityDto.builder().role("ROLE_STORE_ADMIN").build(),
                        AuthorityDto.builder().role("ROLE_DELIVERY_ADMIN").build()
                ))
                .build();
        // 데이터 적재 로직 작성
        loginService.signUp(customerDto) ;

        for (int inx=1 ; inx <= 10 ; inx++) {
            customerDto = CustomerDto.builder()
                    .customerId("customerId_"+String.format("%02d", inx))
                    .customerName("홍길동"+String.format("%02d", inx))
                    .email("email_"+String.format("%02d", inx)+"@naver.com")
                    .password("shin22")
                    .activated(true)
                    .authorities(Set.of(AuthorityDto.builder().role("ROLE_CUSTOMER").build()))
                    .build();
            // 데이터 적재 로직 작성
            loginService.signUp(customerDto) ;
        }
    }
}