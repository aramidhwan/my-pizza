package com.study.mypizza.store.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.study.mypizza.store.dto.ResponseDto;
import com.study.mypizza.store.dto.StoreDto;
import com.study.mypizza.store.dto.StoreOrderDto;
import com.study.mypizza.store.enums.OrderStatus;
import com.study.mypizza.store.service.AdminService;
import com.study.mypizza.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StoreController {
    private final StoreService storeService;
    private final AdminService adminService;

    @GetMapping("/favicon.ico")
    public void returnNoFavicon() {}

    @GetMapping("/html/storeMain")
    public String getStoreMainHtml() {
        return "html/storeMain" ;
    }

    @GetMapping("/html/storeAdminMain")
    public String getStoreAdminMainHtml() {
        return "html/storeAdminMain" ;
    }

    // 신규 상점 등록, 기존 상점 정보 변경
    @PostMapping("/api/createModifyStore")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STORE_ADMIN')")
    public ResponseEntity<ResponseDto> createModifyStore(@RequestBody StoreDto storeDto) throws JsonProcessingException {
        boolean isNew = storeDto.getStoreId() == null;
        adminService.createModifyStore(storeDto);

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .msg("✅ 상점이 "+(isNew? "등록":"수정")+"되었습니다!")
                .build();
        return ResponseEntity.ok(responseDto) ;
    }

    // 신규 상점 등록
    @PostMapping("/createStores")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> createStores(@RequestBody List<StoreDto> storeDtos) {
        adminService.createModifyStore(storeDtos);
        return ResponseEntity.ok("") ;
    }

    // 기존 상점 정보 업데이트
    // 안쓰는 메소드인듯
    @PatchMapping("/updateStore")
    @PreAuthorize("hasRole('ROLE_STORE_ADMIN')")
    public ResponseEntity<StoreDto> updateStore(@RequestBody StoreDto storeDto) {
        return ResponseEntity.ok(adminService.updateStore(storeDto)) ;
    }

    // 기존 상점 삭제
    @DeleteMapping("/deleteStore/{storeId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteStore(@PathVariable Long storeId) {
        adminService.deleteStore(storeId);
        return ResponseEntity.ok("✅ 삭제되었습니다.") ;
    }

    // 주문 접수건 조회 (storeMain.js)
//    @GetMapping("/api/storeOrder")
//    public ResponseEntity<ResponseDto> getStoreOrders(@RequestParam Long storeId) throws JsonProcessingException {
//        log.trace("### [getStoreOrders] is called. ###");
//        List<StoreOrderDto> storeOrderDtos = storeService.getStoreOrders(storeId, "OrderAccepted") ;
//
//        ResponseDto responseDto = ResponseDto.builder()
//                .BIZ_SUCCESS(0)
//                .data(storeOrderDtos)
//                .build();
//
//        return ResponseEntity.ok(responseDto) ;
//    }

    // 주문접수건 조회 (조리완료처리용)
//    @GetMapping("/stores/orderAcceptedStoreOrderId")
//    @ResponseBody
//    public StoreOrder orderAcceptedList() {
//        return storeService.getOrderAcceptedStoreOrderId() ;
//    }

    // 상점 관리자(ROLE_STORE_ADMIN)의 Store 관리화면 (Owner 상점 리스트 가져오기)
    @GetMapping("/api/getStores")
    @Secured({"ROLE_STORE_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ResponseDto> getStores(Authentication authentication) {
        log.trace("### [/api/getStores] is called. ###");
        // Authentication 에서 customerNo 가져오기
        Integer customerNo = (Integer) authentication.getDetails();
        // 👉 1. 현재 사용자가 ROLE_ADMIN 권한을 가지고 있는지 체크
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        List<StoreDto> storeDtos = storeService.getStores(isAdmin, customerNo) ;

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .data(storeDtos)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }
    
    // StoreAdmin 메인화면 (내 상점에 할당된 주문내역 가져오기)
    @GetMapping("/api/getStoreAdmin")
    @Secured("ROLE_STORE_ADMIN")
    public ResponseEntity<ResponseDto> getStoreAdmin(
            Authentication authentication
            ,@RequestParam(required = true, defaultValue="1000-01-01", name = "startDate") String startDate
            ,@RequestParam(required = true, defaultValue="9999-12-31", name = "endDate") String endDate
    ) {
        log.trace("### [/api/getStoreAdmin] is called. ###");
        // Authentication 에서 customerNo 가져오기
        int customerNo = (Integer) authentication.getDetails();
        if (customerNo == 0) {
            throw new RuntimeException("### 이런 경우가 있어???") ;
        }
        List<StoreDto> storeDtos = storeService.getStoreAdmin(customerNo, startDate, endDate) ;

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .data(storeDtos)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }

    // 주문 status 업데이트
    @PostMapping("/api/updateOrderStatus")
    @Secured("ROLE_STORE_ADMIN")
    public ResponseEntity<ResponseDto> updateOrderStatus(@RequestBody StoreOrderDto storeOrderDto) {
        log.trace("### [/api/updateOrderStatus] is called. ###");
        storeOrderDto = storeService.updateOrderStatus(storeOrderDto);
        String msg = null ;
        if (OrderStatus.ORDER_ACCEPTED == storeOrderDto.getStatus()) {
            msg = "접수완료";
        } else if (OrderStatus.COOKED == storeOrderDto.getStatus()) {
            msg = "조리완료" ;
        } else if (OrderStatus.ORDER_CANCELLED == storeOrderDto.getStatus()) {
            msg = "접수취소" ;
        }
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .msg("✅ 주문번호["+storeOrderDto.getOrderId()+"] "+msg+" 처리되었습니다.")
                .data(storeOrderDto)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }

}
