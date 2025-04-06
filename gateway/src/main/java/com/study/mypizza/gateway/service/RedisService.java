package com.study.mypizza.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public void setValues(String key, String value, long refreshExpirationMs) {
        setValues(key, value, Duration.ofMillis(refreshExpirationMs)) ;
    }

    public void setValues(String key, String value, Duration duration) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(key, value, duration);
        // List 형태 (list 말고도 Set, Hash 등등 다양한 타입들이 존재한다.)
//        ListOperations<String, String> valueOperations = redisTemplate.opsForList();
//        valueOperations.leftPush(key, value, duration);
    }

    // 값 가져오기
    public Object getValue(String key) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }

    // 키 삭제
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
}
