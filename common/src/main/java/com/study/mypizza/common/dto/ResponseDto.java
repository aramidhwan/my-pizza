package com.study.mypizza.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.mypizza.common.enums.JWTAuth;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;


@Data
@Slf4j
@Builder
public class ResponseDto {
    private int BIZ_SUCCESS ;
    private String msg ;
    private JWTAuth jwtAuth ;
    private String jwtAccessToken ;
    private Object data ;

    @JsonProperty("BIZ_SUCCESS")
    public int getBIZ_SUCCESS() {
        return BIZ_SUCCESS ;
    }
}
