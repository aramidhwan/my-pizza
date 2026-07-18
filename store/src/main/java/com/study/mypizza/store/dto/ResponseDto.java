package com.study.mypizza.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;


@Data
@Slf4j
@Builder
public class ResponseDto {
    @Builder.Default
    private HttpStatus httpStatus = HttpStatus.OK ;
    private int BIZ_SUCCESS ;
    private String msg ;
    private String jwtAccessToken ;
    private Object data ;

    @JsonProperty("BIZ_SUCCESS")    /* camelCase와 외부 API/프론트의 snake_case가 다를 때 사용, Jackson이 Java 객체와 JSON을 변환할 때, JSON 필드명과 Java 필드명(또는 getter/setter)을 연결하는 어노테이션 */
    public int getBIZ_SUCCESS() {
        return BIZ_SUCCESS ;
    }
}
