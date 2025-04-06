package com.study.mypizza.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.mypizza.gateway.enums.JWTAuth;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;


@Data
@Slf4j
@Builder
public class ResponseDto {
    private HttpStatus httpStatus ;
    private JWTAuth jwtAuth ;
    private int BIZ_SUCCESS ;
    private String msg ;
    private String jwtAccessToken ;
    private Object data ;
    @JsonProperty("BIZ_SUCCESS")
    public int getBIZ_SUCCESS() {
        return BIZ_SUCCESS ;
    }

}
