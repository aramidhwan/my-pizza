package com.study.mypizza.store.repository;

import com.study.mypizza.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/* @RepositoryRestResource : Repository를 REST API로 자동 노출. 예: /myPages */
//@RepositoryRestResource(path="stores", collectionResourceRel = "stores")
public interface StoreRepository extends JpaRepository<Store, Long> {
    public List<Store> findByRegionNmAndOpenYNTrue(String regionNm) ;

    public List<Store> findByOwnerNo(int customerNo) ;

    public List<Store> findByStoreNmLike(String storeNm) ;

    @Query("SELECT DISTINCT s.regionNm FROM Store s WHERE s.openYN = :openYN ORDER BY s.regionNm ASC")
    List<String> findActiveRegionNmList(@Param("openYN") boolean openYN);   // @Param : openYN을 JPQL의 :openYN 바인딩 변수와 명확하게 매핑합니다.

    List<Store> findByOwnerNoOrderByStoreNmAscRegionNmAsc(int ownerNo);
}
