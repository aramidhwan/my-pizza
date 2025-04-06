package com.study.mypizza.delivery.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseDto {
    private LocalDateTime createDt ;
    private LocalDateTime updateDt ;
}
