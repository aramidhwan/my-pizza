package com.study.mypizza.customercenter.repository;

import com.study.mypizza.customercenter.dto.MyPageDto;
import com.study.mypizza.customercenter.entity.MyPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path="myPages", collectionResourceRel = "myPages")
public interface MyPageRepository extends JpaRepository<MyPage, Long> {
    Optional<MyPage> findByOrderId(Long orderId) ;
    List<MyPage> findByCustomerNoOrderByStoreIdAscCreateDtDesc(int customerNo);
    List<MyPage> findByCustomerNoAndCreateDtAfterOrderByStoreIdAscCreateDtDesc(int customerNo, LocalDateTime today);
}
