package com.study.mypizza.mypage.service;

import com.study.mypizza.mypage.dto.MyPageDto;
import com.study.mypizza.mypage.dto.MyPageOrderDetailDto;
import com.study.mypizza.mypage.external.InternalGateway;
import com.study.mypizza.mypage.repository.MyPageOrderDetailRepository;
import com.study.mypizza.mypage.repository.MyPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MyPageService {
    private final MyPageRepository mypageRepository;
    private final MyPageOrderDetailRepository myPageOrderDetailRepository;
    private final InternalGateway internalGateway;

    public List<MyPageDto> getMyPageContent(int customerNo, String startDate, String endDate) {
        // 변환: 하루의 시작부터 끝까지 포함되도록
        LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);

        List<MyPageDto> myPageDtos = mypageRepository.findByCustomerNoAndCreateDtBetweenOrderByStoreIdAscCreateDtDesc(customerNo, startDateTime, endDateTime)
                .stream()
                .map(MyPageDto::of)
                .map(this::setStoreNm)
                .toList() ;

        for (MyPageDto myPageDto:myPageDtos) {
            myPageDto.setMyPageOrderDetailDtos(myPageOrderDetailRepository.findByOrderIdOrderByItemIdAsc(myPageDto.getOrderId())
                    .stream()
                    .map(MyPageOrderDetailDto::of)
                    .map(this::setItemNm)
                    .toList()
            ) ;
        }

        return myPageDtos ;
    }

    @Transactional(readOnly = true) // Public 메소드가 아니므로 readOnly=true 붙여줌
    private MyPageOrderDetailDto setItemNm(MyPageOrderDetailDto myPageOrderDetailDto) {
        if ( myPageOrderDetailDto.getItemId() != null ) {
            myPageOrderDetailDto.setItemNm(internalGateway.getItemNm(myPageOrderDetailDto.getItemId()));
        } else {
            myPageOrderDetailDto.setItemNm("");
        }
        return myPageOrderDetailDto;
    }

    @Transactional(readOnly = true) // Public 메소드가 아니므로 readOnly=true 붙여줌
    private MyPageDto setStoreNm(MyPageDto myPageDto) {
        if ( myPageDto.getStoreId() != null ) {
            myPageDto.setStoreNm(internalGateway.getStoreNm(myPageDto.getStoreId()));
        } else {
            myPageDto.setStoreNm("");
        }
        return myPageDto;
    }
//    private List<MyPageDto> setStoreNms(List<MyPageDto> myPageDtos) {
//
//        for (MyPageDto myPageDto : myPageDtos) {
//            if ( myPageDto.getStoreId() != null ) {
//                myPageDto.setStoreNm(storeGateway.getStoreNm(myPageDto.getStoreId()));
//            } else {
//                myPageDto.setStoreNm("");
//            }
//        }
//
//        return myPageDtos;
//    }
}
