package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.entity.Store;
import com.example.foodingbyboot.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final StoreService storeService;

    @PostMapping("api/stores-test")
    public ResponseEntity<Map<String,Object>> showStoreListByScate(@RequestParam(value = "sortBy", required = false) String sortBy, @RequestParam(value = "scates", required = false) String scates) {
        System.out.println(scates);
        List<String> allScates = List.of("한식", "일식", "중식", "양식", "세계요리", "빵/디저트", "차/커피", "술집");
        ;

        Map<String,Object> map = new HashMap<>();

        map.put("allScates", allScates);

        List<Store> storesByScate = new ArrayList<>();
        if (scates != null && !scates.trim().isEmpty()) {
            String[] selectedScates = scates.split(",");

            for (String scate : selectedScates) {
                storesByScate.addAll(storeService.getStoresByCategory(scate));
            }
        } else {
            storesByScate = storeService.getAllStores();
        }

        storesByScate.sort(Comparator.comparingDouble(Store::getSno).reversed());
        if ("score".equals(sortBy)) {
            storesByScate.sort(Comparator.comparingDouble(Store::getScoreArg).reversed());
            map.put("sortStandard", "score");
        } else {
            storesByScate.sort(Comparator.comparingInt(Store::getPickNum).reversed());
            map.put("sortStandard", "pick");
        }

        return ResponseEntity.ok(map);
    }
}
