package com.study.mypizza.store.repository;

import com.study.mypizza.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/* @RepositoryRestResource : Repository를 REST API로 자동 노출. 예: /myPages */
//@RepositoryRestResource(path="stores", collectionResourceRel = "stores")
public interface StoreRepository extends JpaRepository<Store, Long> {
    public List<Store> findByRegionNmAndOpenYNTrue(String regionNm) ;

    public List<Store> findByOwnerNo(int customerNo) ;

    public List<Store> findByStoreNmLike(String storeNm) ;
}
