package com.study.mypizza.order.repository;

import com.study.mypizza.order.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path="items", collectionResourceRel = "items")
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByItemIdIn(List<Long> itemIds);
}