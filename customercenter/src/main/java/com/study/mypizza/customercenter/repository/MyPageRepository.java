package com.study.mypizza.customercenter.repository;

import com.study.mypizza.customercenter.entity.MyPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Date;
import java.util.Optional;

@RepositoryRestResource(path="myPages")
public interface MyPageRepository extends JpaRepository<MyPage, Long> {
    Optional<MyPage> findByOrderId(Long orderId);
}
