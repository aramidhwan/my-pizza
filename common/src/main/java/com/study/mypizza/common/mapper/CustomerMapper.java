package com.study.mypizza.common.mapper;

import com.study.mypizza.common.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface CustomerMapper {
    //Member 저장
    int insertMember(@Param("member") Customer customer);

    //Member 조회
    Optional<Customer> selectCustomer(@Param("email") String email);
}
