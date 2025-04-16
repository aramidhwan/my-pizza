package com.study.mypizza.order.service;

import com.study.mypizza.order.dto.ItemDto;
import com.study.mypizza.order.entity.Item;
import com.study.mypizza.order.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository ;

    @Transactional
    public List<ItemDto> createItems(List<ItemDto> itemDtos) {
        List<Item> items = itemRepository.saveAll(itemDtos.stream().map(ItemDto::toEntity).collect(Collectors.toList()));
        return items.stream().map(ItemDto::of).collect(Collectors.toList());
    }

    public List<ItemDto> getItems() {
        return itemRepository.findAll()
                .stream()
                .map(ItemDto::of)
                .toList();
    }

    public ItemDto getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .map(ItemDto::of)
                .orElseThrow();
    }
}
