package com.study.mypizza.common.controller;

import com.study.mypizza.common.dto.AuthorityDto;
import com.study.mypizza.common.dto.CustomerDto;
import com.study.mypizza.common.dto.ResponseDto;
import com.study.mypizza.common.exception.MyPizzaException;
import com.study.mypizza.common.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final CustomerService customerService ;

    @GetMapping("/html/roleMain")
    public String roleMain() throws MyPizzaException {
        log.debug("### [/api/roleMain] is called. ###");
        return "html/manageRole";
    }

    @GetMapping("/api/getCustomers")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<ResponseDto> getCustomers() throws MyPizzaException {
        log.debug("### [/api/getCustomers] is called. ###");
        List<CustomerDto> customerDtos = customerService.getCustomers() ;

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .data(customerDtos)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }
    @GetMapping("/api/getRoles")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<ResponseDto> getRoles(@RequestParam String email) throws MyPizzaException {
        log.debug("### [/api/getRoles] is called. ###");
        CustomerDto customerDto = customerService.getCustomer(email) ;
//        CustomerDto customerDto = customerService.getCustomerByMyBatis(email) ;
        List<AuthorityDto> authorityDtos = customerService.getRoles() ;

        // DTO를 Map으로 묶어서 반환
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("customerDto", customerDto);
        response.put("authorityDtos", authorityDtos);

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .data(response)
                .build();

//        // OOM 테스트용 리스트 (참조를 유지하여 GC가 못 가져가게 함)
//        List<byte[]> memoryLeakList = new ArrayList<>();
//        for (int inx = 0 ; inx < 99999999 ; inx++) {
//            // 20MB 생성 후 리스트에 추가 (참조 유지)
//            memoryLeakList.add(new byte[1024 * 1024 * 20]);
//            log.info("Current accumulated memory: {} MB", (inx + 1) * 20);
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }

        return ResponseEntity.ok(responseDto) ;
    }

    @PostMapping("/api/addModifyRole")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<ResponseDto> addModifyRole(@RequestBody CustomerDto customerDto) throws MyPizzaException {
        log.debug("### [addModifyRole] is called. ###");
        customerDto = customerService.updateCustomer(customerDto) ;

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .data(customerDto)
                .msg("성공적으로 권한을 등록하였습니다.")
                .build();

        return ResponseEntity.ok(responseDto) ;
    }
}
