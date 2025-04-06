package com.study.mypizza.store.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.study.mypizza.store.dto.ResponseDto;
import com.study.mypizza.store.dto.StoreDto;
import com.study.mypizza.store.dto.StoreOrderDto;
import com.study.mypizza.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StoreController {
    private final StoreService storeService;

    // 신규 상점 등록
    @PostMapping("/api/createModifyStore")
    public ResponseEntity<ResponseDto> createModifyStore(@RequestBody StoreDto storeDto) throws JsonProcessingException {
        boolean isNew = (storeDto.getStoreId()==null)? true:false;
        storeService.createModifyStore(storeDto);

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .msg("상점이 "+(isNew? "등록":"수정")+"되었습니다!")
                .build();
        return ResponseEntity.ok(responseDto) ;
    }

    // 신규 상점 등록
    @PostMapping("/createStores")
    public ResponseEntity<String> createStores(@RequestBody List<StoreDto> storeDtos) {
        storeService.createModifyStore(storeDtos);
        return ResponseEntity.ok("") ;
    }

    // 기존 상점 업데이트
    @PatchMapping("/udpateStore")
    public ResponseEntity<StoreDto> updateStore(@RequestBody StoreDto storeDto) {
        return ResponseEntity.ok(storeService.udpateStore(storeDto)) ;
    }

    // 기존 상점 삭제
    @DeleteMapping("/deleteStore/{storeId}")
    public ResponseEntity<String> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.ok("삭제되었습니다.") ;
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

    @GetMapping("/favicon.ico")
    public void returnNoFavicon() {}

    @GetMapping("/html/storeMain")
    public String getStoreMainHtml() {
        return "/html/storeMain" ;
    }

    @GetMapping("/html/storeAdminMain")
    public String getStoreAdminMainHtml() {
        return "/html/storeAdminMain" ;
    }

    // Store 메인화면 (등록 상점 리스트 가져오기)
    @GetMapping("/api/getStores")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<ResponseDto> getStores() {
        log.trace("### [getStores] is called. ###");
        List<StoreDto> storeDtos = storeService.getStores() ;

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .data(storeDtos)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }
    
    // StoreAdmin 메인화면 (내 상점에 할당된 주문내역 가져오기)
    @GetMapping("/api/getStoreAdmin")
    @Secured("ROLE_STORE_ADMIN")
    public ResponseEntity<ResponseDto> getStoreAdmin(Authentication authentication) {
        log.trace("### [getStoreAdmin] is called. ###");
        // Authentication 에서 customerNo 가져오기
        int customerNo = (Integer) authentication.getDetails();
        if (customerNo == 0) {
            throw new RuntimeException("### 이런 경우가 있어???") ;
        }
        List<StoreDto> storeDtos = storeService.getStoreAdmin(customerNo) ;

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .data(storeDtos)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }

    // 주문 status 업데이트 (조리완료 처리)
    @PostMapping("/api/updateOrderStatus")
    public ResponseEntity<ResponseDto> updateOrderStatus(@RequestBody StoreOrderDto storeOrderDto) {
        log.trace("### [updateOrderStatus] is called. ###");
        storeOrderDto = storeService.updateOrderStatus(storeOrderDto);

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .msg("주문번호["+storeOrderDto.getOrderId()+"] 조리완료 처리되었습니다.")
                .data(storeOrderDto)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }

}
