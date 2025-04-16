package com.study.mypizza.mypage.repository;

import com.study.mypizza.mypage.entity.MyPageOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;


@RepositoryRestResource(path="myPageOrderDetails", collectionResourceRel = "myPageOrderDetails")
public interface MyPageOrderDetailRepository extends JpaRepository<MyPageOrderDetail, Long> {
    List<MyPageOrderDetail> findByOrderIdOrderByItemIdAsc(Long orderId);
}
