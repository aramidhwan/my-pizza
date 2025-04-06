package com.study.mypizza.order.controller;

import com.study.mypizza.order.dto.ItemDto;
import com.study.mypizza.order.entity.Item;
import com.study.mypizza.order.event.Ordered;
import com.study.mypizza.order.exception.MyPizzaException;
import com.study.mypizza.order.service.ItemService;
import com.study.mypizza.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService ;

    @PostMapping("/createItems")
    public ResponseEntity<List<ItemDto>> order(@RequestBody List<ItemDto> items) {
        itemService.createItems(items);
        return ResponseEntity.ok(items) ;
    }
}
