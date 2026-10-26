package com.study.mypizza.common.service;

import com.study.mypizza.common.mapper.CommonMapper;
import com.study.mypizza.common.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {         // ADMIN 서비스
    private final CommonMapper commonMapper;

    @Transactional
    public void delAllData() {
        commonMapper.delOrderDetail() ;
        commonMapper.delOrder() ;
        commonMapper.delMyPageOrderDetail() ;
        commonMapper.delMyPage() ;
        commonMapper.delStoreOrderDetail() ;
        commonMapper.delStoreOrder() ;
        commonMapper.delDelivery() ;
        commonMapper.delPendingEvent() ;
    }
}
