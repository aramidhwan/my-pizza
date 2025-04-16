package com.study.mypizza.mypage.repository;

import com.study.mypizza.mypage.entity.MyPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path="myPages", collectionResourceRel = "myPages")
public interface MyPageRepository extends JpaRepository<MyPage, Long> {
    Optional<MyPage> findByOrderId(Long orderId) ;
    List<MyPage> findByCustomerNoOrderByStoreIdAscCreateDtDesc(int customerNo);
    List<MyPage> findByCustomerNoAndCreateDtAfterOrderByStoreIdAscCreateDtDesc(int customerNo, LocalDateTime today);
}
