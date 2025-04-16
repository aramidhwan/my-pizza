package com.study.mypizza.mypage.controller;

import com.study.mypizza.mypage.dto.MyPageDto;
import com.study.mypizza.mypage.dto.ResponseDto;
import com.study.mypizza.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MyPageController {
    private final MyPageService myPageService ;

    @GetMapping("/html/myPageMain")
    public String getMyPageHtml(@RequestParam(required = false, defaultValue="onlyToday", name = "viewType") String viewType, Model model) {
        model.addAttribute("viewType", viewType);
        return "html/myPageMain" ;
    }
    @GetMapping("/api/getMyOrders")
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_VIP_CUSTOMER')") // 여러개 권한 주기
    public ResponseEntity<ResponseDto> getMyPageContent(Authentication authentication, @RequestParam(required = false, defaultValue="onlyToday", name = "viewType") String viewType) {
        log.trace("### [getMyPageContent] is called. ###");
        // Authentication 에서 customerNo 가져오기
        Integer customerNo = (Integer) authentication.getDetails();

        List<MyPageDto> myPageDtos = myPageService.getMyPageContent(customerNo, viewType) ;
        // DTO를 Map으로 묶어서 반환
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("myPageDtos", myPageDtos);
        response.put("viewType", viewType);

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .data(response)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }
}
