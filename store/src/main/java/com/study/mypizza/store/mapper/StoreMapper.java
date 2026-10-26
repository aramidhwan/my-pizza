package com.study.mypizza.store.mapper;

import com.study.mypizza.store.dto.StoreDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StoreMapper {
    List<StoreDto>  selectStoreAdmin(@Param("customerNo") Integer customerNo, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime) ;
}
