package com.study.mypizza.mypage.config;

import com.study.mypizza.mypage.entity.MyPage;
import com.study.mypizza.mypage.event.Ordered;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // 기본 설정
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                // setSkipNullEnabled(true) : 소스(Source) 객체의 필드 값이 null일 경우, 대상(Destination) 객체의 기존 필드 값을 덮어쓰지 않고 건너뛰도록(Skip) 처리하는 옵션.
                //                              주로 DB의 기존 데이터를 유지하면서 일부 변경된 데이터만 업데이트하는 PATCH(수정) 기능을 구현할 때 핵심적으로 사용.
                .setSkipNullEnabled(false)
                .setFieldMatchingEnabled(true)  /* target class에 @Setter 설정이 없을때 사용(field에 직접 복사) */
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);  /* 직접 복사할 field의 access level */

        // 필드명이 다른 클래스 간 매핑 규칙 사전 등록
//        modelMapper.typeMap(Ordered.class, MyPage.class)
//                .addMappings(mapper -> mapper.map(Ordered::getOrderDetailDtoList, MyPage::assignMyPageOrderDetail));
//        modelMapper.typeMap(MyPageOrderDetailDto.class, MyPage.class)
//                .addMappings(mapper -> mapper.map(Ordered::getOrderDetailDtoList, MyPage::assignMyPageOrderDetail))
//                .addMappings(mapper -> {
//                    mapper.map(src -> src.getItemDto().getItemId(), MyPageOrderDetailDto::setItemId);
//                    mapper.map(src -> src.getItemDto().getItemNm(), MyPageOrderDetailDto::setItemNm);
//                });        ;

        return modelMapper;
    }
}