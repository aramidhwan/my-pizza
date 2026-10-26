package com.study.mypizza.store.service;

import com.study.mypizza.store.dto.StoreDto;
import com.study.mypizza.store.entity.Store;
import com.study.mypizza.store.event.StoreUpdated;
import com.study.mypizza.store.exception.MyPizzaException;
import com.study.mypizza.store.repository.StoreRepository;
import com.study.mypizza.store.util.MyPizzaAssert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final StoreRepository storeRepository ;

    /**
     * 기존 상점 정보 업데이트
     * @param storeDto
     * @return
     */
    @Transactional
    // 안쓰는 메소드임
    public StoreDto updateStore(StoreDto storeDto) {
        return StoreDto.of(storeRepository.save(storeDto.toEntity())) ;
    }

    private List<StoreDto> getStoresByNameLike(String storeNm) {
        return storeRepository.findByStoreNmLike(storeNm)
                .stream()
                .map(StoreDto::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createModifyStore(StoreDto storeDto) {
        createModifyStore(List.of(storeDto)) ;
    }

    @Transactional
    public void createModifyStore(List<StoreDto> storeDtos) {
        Store storeFromDB = null ;
        List<StoreDto> alreadyExistStores = new ArrayList<>();
        for (StoreDto storeDto:storeDtos) {
            alreadyExistStores.addAll(getStoresByNameLike(storeDto.getStoreNm())) ;
        }

        for (StoreDto storeDto:storeDtos) {
            // 기존 Store 수정일 시
            if ( storeDto.getStoreId() != null ) {
                storeFromDB = storeRepository.findById(storeDto.getStoreId())
                        .orElseThrow(() -> new MyPizzaException("⚠️ 해당하는 Store가 없습니다. STORE_ID=["+storeDto.getStoreId()+"]"));

                if ( !alreadyExistStores.isEmpty() && !Objects.equals(alreadyExistStores.get(0).getStoreId(), storeDto.getStoreId())) {
                    throw new MyPizzaException("⚠️ 이미 비슷한 명칭의 Store가 존재합니다. ["+(!alreadyExistStores.isEmpty() ? alreadyExistStores.get(0).getStoreNm():"")+"]");
                }

                storeRepository.save(storeDto.toEntity()) ;
                // 신규 Store 등록
            } else {
                // 기존 Store 명칭이 있으면 반려
                MyPizzaAssert.isTrue(alreadyExistStores.isEmpty(),"⚠️ 이미 비슷한 명칭의 Store가 존재합니다. ["+(alreadyExistStores.size()>0? alreadyExistStores.get(0).getStoreNm():"")+"]");

                storeRepository.save(storeDto.toEntity()) ;
            }
        }

        // kafka 이벤트 발송
        publishEvent();

//        List<Store> stores = storeRepository.saveAll(storeDtos.stream().map(StoreDto::toEntity).toList());
    }

    @Transactional
    public void deleteStore(Long storeId) {
        Store storeFromDB = storeRepository.findById(storeId)
                .orElseThrow(() -> new MyPizzaException("⚠️ STORE ID=["+storeId+"]에 해당하는 상점이 없습니다.")) ;

        if ( storeFromDB.getOpenYN() ) {
            throw new MyPizzaException("⚠️ 영업중인 Store는 삭제할 수 없습니다. ["+storeFromDB.getStoreNm()+"]") ;
        } else {
            storeRepository.delete(storeFromDB);
        }
    }

    // 상점 정보 변경 이벤트 발송
    private void publishEvent() {
        StoreUpdated storeUpdated = new StoreUpdated() ;
        BeanUtils.copyProperties(this, storeUpdated);
        storeUpdated.publishAfterCommit();
        log.info("👉 카프카 이벤트 발송..!! {}", storeUpdated);
    }
}
