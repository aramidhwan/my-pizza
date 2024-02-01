package com.study.mypizza.store.repository;

import com.study.mypizza.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path="stores", collectionResourceRel = "stores")
public interface StoreRepository extends JpaRepository<Store, Long> {
    public List<Store> findByRegionNmAndOpenYN(String regionNm, boolean openYN) ;
}
