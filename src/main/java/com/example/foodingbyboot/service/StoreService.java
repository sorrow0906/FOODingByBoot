package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.ReviewTag;
import com.example.foodingbyboot.entity.Store;
import com.example.foodingbyboot.entity.StoreTag;
import com.example.foodingbyboot.entity.Tag;
import com.example.foodingbyboot.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    private final ReviewTagRepository reviewTagRepository;

    private final StoreTagRepository storeTagRepository;

    private final TagRepository tagRepository;

    private final PickRepository pickRepository;

    private final ReviewRepository reviewRepository;

    private Map<Integer, Store> storeCache = new HashMap<>();


    @PostConstruct
    public void initializeStoreScores() {
        List<Store> stores = storeRepository.findAll();
        for (Store store : stores) {
            updateStoreTags(store);
            calculateAndCacheStoreScores(store);
        }
    }

    @Transactional
    public void updateStoreInCache(int sno) {
        Store store = storeRepository.findBySno(sno).orElse(null);
        if (store != null) {
            // 별점 평균과 Pick 수를 다시 계산
            calculateAndCacheStoreScores(store);

            // 태그 수 갱신
            updateStoreTags(store);
        }
    }

    @Transactional
    public void updateStoreTags(Store store) {
        // review의 mdelete나 adelete 값이 1이 아닌 경우로 예외처리 완료
        List<ReviewTag> reviewTags = reviewTagRepository.findValidReviewTagsByStoreSno(store.getSno());

        if (reviewTags == null || reviewTags.isEmpty()) {
            return;
        }

        Map<Integer, Long> tagCountMap = new HashMap<>();
        for (ReviewTag reviewTag : reviewTags) {
            int tno = reviewTag.getTag().getTno();
            tagCountMap.put(tno, tagCountMap.getOrDefault(tno, 0L) + 1);
        }

        for (Map.Entry<Integer, Long> entry : tagCountMap.entrySet()) {
            int tno = entry.getKey();
            long count = entry.getValue();

            StoreTag storeTag = storeTagRepository.findStoreTagByStoreSnoAndTagTno(store.getSno(), tno);

            if (storeTag == null) {
                storeTag = new StoreTag();
                storeTag.setStore(store);
                Tag tag = tagRepository.findByTno(tno);
                storeTag.setTag(tag);
                storeTag.setTagCount((int) count);
            } else {
                storeTag.setTagCount((int) count);
            }

            storeTagRepository.save(storeTag);
        }
        /*System.out.println("updateStoreTags 동작 완료");*/
    }


    @Transactional
    public void calculateAndCacheStoreScores(Store store) {
        // review의 mdelete나 adelete 값이 1이 아닌 경우로 예외처리 완료
        Double averageScore = reviewRepository.findAverageRstarBySno(store.getSno());
        store.setScoreArg(averageScore != null ? averageScore : 0);

        // pick 폴더가 기본폴더(pfno =1)인 경우만 count 되도록 예외처리 완료
        int pickCount = pickRepository.countBySnoAndPfno(store.getSno());
        store.setPickNum(pickCount);

        // 캐시에 저장!
        storeCache.put(store.getSno(), store);
    }


    public List<Store> getStoresByCategory(String scate) {
        if (storeCache.isEmpty()) {
            initializeStoreScores();
        }
        List<Store> resultStores = new ArrayList<>();

        for (Store store : storeCache.values()) {

            // 카테고리가 일치하는 가게만 리스트에 추가
            if (store.getScate().equals(scate)) {
                resultStores.add(store);
            }
        }
        return resultStores;
    }

    public List<Store> getAllStores() {
        //        System.out.println("getAllStores에 진입");
        if (storeCache.isEmpty()) {
            initializeStoreScores();
        }

        // 그냥 list로 return하면 불변 list라서 정렬이 안 되기 때문에 복사해서 return함
        return new ArrayList<>(storeCache.values());
    }

    public Store getStoreAllInfo(int sno) {
        if (storeCache.isEmpty()) {
            initializeStoreScores();
        }
        return storeCache.get(sno);
    }

    public List<StoreTag> getStoreTagsByStoreSno(int sno) {
        return storeTagRepository.findByStore_Sno(sno);
    }
}
