package com.study.mypizza.store.controller;

import com.study.mypizza.store.entity.StoreOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.mypizza.store.service.StoreService;

import java.util.Optional;

@RestController
@Slf4j
public class StoreController {
    @Autowired
    private StoreService storeService;

    @GetMapping("/stores/checkOpenYn")
    public boolean checkOpenYn(@RequestParam("regionNm") String regionNm) throws Exception {
        return storeService.checkOpenYn(regionNm);
    }

    @GetMapping("/stores/checkOrderCancel/{orderId}")
    public boolean checkOrderCancel(@PathVariable("orderId") Long orderId) {
        return storeService.checkOrderCancel(orderId) ;
    }
}
