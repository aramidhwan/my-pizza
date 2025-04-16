package com.study.mypizza.mypage.service;

import com.study.mypizza.mypage.dto.MyPageDto;
import com.study.mypizza.mypage.dto.MyPageOrderDetailDto;
import com.study.mypizza.mypage.exception.MyPizzaException;
import com.study.mypizza.mypage.external.OrderGateway;
import com.study.mypizza.mypage.external.StoreGateway;
import com.study.mypizza.mypage.repository.MyPageOrderDetailRepository;
import com.study.mypizza.mypage.repository.MyPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService {
    private final MyPageRepository mypageRepository;
    private final MyPageOrderDetailRepository myPageOrderDetailRepository;
    private final StoreGateway storeGateway;
    private final OrderGateway orderGateway;

    public List<MyPageDto> getMyPageContent(int customerNo, String viewType) {
        List<MyPageDto> myPageDtos = null ;

        if ("onlyToday".equals(viewType)) {
            LocalDateTime now = LocalDateTime.now() ;
            LocalDateTime today = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0, 0) ;

            myPageDtos = mypageRepository.findByCustomerNoAndCreateDtAfterOrderByStoreIdAscCreateDtDesc(customerNo, today)
                    .stream()
                    .map(MyPageDto::of)
                    .map(this::setStoreNm)
                    .toList() ;

        } else if ("all".equals(viewType)) {
            myPageDtos = mypageRepository.findByCustomerNoOrderByStoreIdAscCreateDtDesc(customerNo)
                    .stream()
                    .map(MyPageDto::of)
                    .map(this::setStoreNm)
                    .toList() ;

        } else {
            throw new MyPizzaException("알 수 없는 viewType=["+viewType+"]") ;
        }

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

    private MyPageOrderDetailDto setItemNm(MyPageOrderDetailDto myPageOrderDetailDto) {
        if ( myPageOrderDetailDto.getItemId() != null ) {
            myPageOrderDetailDto.setItemNm(orderGateway.getItemNm(myPageOrderDetailDto.getItemId()));
        } else {
            myPageOrderDetailDto.setItemNm("");
        }
        return myPageOrderDetailDto;
    }

    private MyPageDto setStoreNm(MyPageDto myPageDto) {
        if ( myPageDto.getStoreId() != null ) {
            myPageDto.setStoreNm(storeGateway.getStoreNm(myPageDto.getStoreId()));
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
