package com.study.mypizza.order.controller;

import com.study.mypizza.order.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GatewayController {
    private final ItemService itemService;

    // Store/MyPage MSA에서 호출되는 메소드 (아이템 이름 가져오기)
    @GetMapping("/items/getItemNm")
    public String getItemNm(@RequestParam Long itemId) {
        log.debug("### [/items/getItemNm] is called. itemId = {}", itemId);
        return itemService.getItem(itemId).getItemNm();
    }
}
