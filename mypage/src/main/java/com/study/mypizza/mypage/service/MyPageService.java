package com.study.mypizza.mypage.service;

import com.study.mypizza.mypage.dto.MyPageDto;
import com.study.mypizza.mypage.dto.MyPageOrderDetailDto;
import com.study.mypizza.mypage.entity.MyPage;
import com.study.mypizza.mypage.enums.OrderStatus;
import com.study.mypizza.mypage.event.AbstractEvent;
import com.study.mypizza.mypage.event.OrderCancelled;
import com.study.mypizza.mypage.exception.MyPizzaException;
import com.study.mypizza.mypage.mapper.MyPageMapper;
import com.study.mypizza.mypage.repository.MyPageOrderDetailRepository;
import com.study.mypizza.mypage.repository.MyPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MyPageService {
    private final MyPageRepository myPageRepository;
    private final MyPageOrderDetailRepository myPageOrderDetailRepository;
    private final CacheService cacheService;
    private final MyPageMapper myPageMapper;

    @Transactional(readOnly = true)
    public List<MyPageDto> getMyPageByMyBatis(Integer customerNo, String startDate, String endDate) {
        // 변환: 하루의 시작부터 끝까지 포함되도록
        LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        // storeNm 셋팅
        List<MyPageDto> myPageDtos = myPageMapper.selectMyPage(customerNo, startDateTime, endDateTime)
                .stream()
                .peek(detailDto -> {
                    if (detailDto.getStoreId() != null) {
                        detailDto.setStoreNm(cacheService.getStoreNm(detailDto.getStoreId()));
                    } else {
                        detailDto.setStoreNm("");
                    }
                })
                .toList() ;
        // itemNm 셋팅
        myPageDtos.forEach(myPageDto -> {
            List<MyPageOrderDetailDto> detailDtos = myPageDto.getMyPageOrderDetailDtoList() ;
            if (detailDtos!=null) {
                detailDtos.forEach(detailDto -> {
                    if (detailDto.getItemId() != null) {
                        detailDto.setItemNm(cacheService.getItemNm(detailDto.getItemId()));
                    } else {
                        detailDto.setItemNm("");
                    }
                });
            }
        });

        return myPageDtos ;
    }

    @Transactional(readOnly = true)
    public List<MyPageDto> getMyPageByJPA(int customerNo, String startDate, String endDate) {
        // 변환: 하루의 시작부터 끝까지 포함되도록
        LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);

        List<MyPageDto> myPageDtos = myPageRepository.findByCustomerNoAndCreateDtBetweenOrderByStoreIdAscCreateDtDesc(customerNo, startDateTime, endDateTime)
                .stream()
                .map(MyPageDto::of)
                .peek(detailDto -> {
                    if (detailDto.getStoreId() != null) {
                        detailDto.setStoreNm(cacheService.getStoreNm(detailDto.getStoreId()));
                    } else {
                        detailDto.setStoreNm("");
                    }
                })
                .toList() ;

        for (MyPageDto myPageDto:myPageDtos) {
            myPageDto.setMyPageOrderDetailDtoList(myPageOrderDetailRepository.findByMypage_OrderIdOrderByItemIdAsc(myPageDto.getOrderId())
                    .stream()
                    .map(MyPageOrderDetailDto::of)
                    .peek(detailDto -> {
                        if (detailDto.getItemId() != null) {
                            detailDto.setItemNm(cacheService.getItemNm(detailDto.getItemId()));
                        } else {
                            detailDto.setItemNm("");
                        }
                    })
                    .toList()
            ) ;
        }

        return myPageDtos ;
    }


    @Transactional
    public MyPageDto updateOrderStatus(Integer customerNo, Long orderId, String toBeStatus) {
        MyPage myPage = myPageRepository.findByOrderId(orderId)
                .orElseThrow(() -> new MyPizzaException("⚠️ 마이페이지 테이블(t_mypage)에 해당 하는 주문이 없습니다. orderId="+orderId ) ) ;

        boolean isSucceeded = myPage.updateStatus(OrderStatus.valueOf(toBeStatus), null);
        if (!isSucceeded) {
            throw new MyPizzaException("⚠️ 주문번호:[" + orderId + "]가 "+toBeStatus+" 처리할 상태가 아닙니다.\n현재 상태(Status) : " + myPage.getStatus());
        }

        // kafka 이벤트 발송
        publishEvent(myPage) ;

        // 반드시 flush()를 하고 보내야 updateDt 컬럼이 반영되어 client로 내려간다.
        myPageRepository.flush();
        return MyPageDto.of(myPage) ;
    }

    // kafka 이벤트(Ordered) 발행
    private void publishEvent(MyPage myPage) {
        AbstractEvent event = null ;

        // 주문취소 처리
        if (OrderStatus.ORDER_CANCELLED == myPage.getStatus()) {
            OrderCancelled orderCancelled = new OrderCancelled() ;
            BeanUtils.copyProperties(myPage, orderCancelled);
            event = orderCancelled ;
        } else {
            String msg = "⚠️ 카프카 이벤트 발송에서 알 수 없는 상태입니다. orderId=[" + myPage.getOrderId() + "], " + myPage.getStatus() ;
            log.error(msg) ;
            throw new MyPizzaException(msg) ;
        }

        event.publishAfterCommit();
        log.info("👉 카프카 이벤트 발송..!! : {}", event);
    }
}
