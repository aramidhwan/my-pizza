package com.study.mypizza.mypage.mapper;

import com.study.mypizza.mypage.dto.MyPageDto;
import com.study.mypizza.mypage.dto.MyPageOrderDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MyPageMapper {
    List<MyPageDto> selectMyPage(@Param("customerNo") Integer customerNo, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime) ;

    List<MyPageOrderDetailDto> selectMyPageOrderDetail(@Param("orderId") Long orderId) ;
}
