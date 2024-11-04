package com.example.foodingbyboot.contoller;

import com.example.foodingbyboot.entity.Store;
import com.example.foodingbyboot.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api") // 기본 경로 설정
public class TestController {
    @Autowired
    private StoreService storeService;


    @GetMapping("/stores-main") // 최종 경로: /api/stores-test
    public ResponseEntity<Map<String, Object>> showStoreListByScate(
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "scates", required = false) String scates) {
        // 테스트용 데이터
        Map<String, Object> response = new HashMap<>();
        System.out.println(scates);
        List<String> allScates = List.of("한식", "일식", "중식", "양식", "세계요리", "빵/디저트", "차/커피", "술집");;
        response.put("allScates", allScates);

        List<Store> storesByScate = new ArrayList<>();
        if (scates != null && !scates.trim().isEmpty()) {
            String[] selectedScates = scates.split(",");

            for (String scate : selectedScates) {
                storesByScate.addAll(storeService.getStoresByCategory(scate));
            }
        }
        else{
            storesByScate = storeService.getAllStores();
        }

        storesByScate.sort(Comparator.comparingDouble(Store::getSno).reversed());
        if ("score".equals(sortBy)) {
            storesByScate.sort(Comparator.comparingDouble(Store::getScoreArg).reversed());
            response.put("sortStandard", "score");
        } else {
            storesByScate.sort(Comparator.comparingInt(Store::getPickNum).reversed());
            response.put("sortStandard", "pick");
        }

        response.put("stores", storesByScate);
        return ResponseEntity.ok(response);
    }

/*    @GetMapping("/store-detail")
    public String storeDetail(@RequestParam("sno") int sno, Model model) {
        Store store = storeService.getStoreAllInfo(sno);
        List<Menu> menus = menuService.getMenuBySno(sno);
        List<StoreTag> storeTags = storeService.getStoreTagsByStoreSno(sno);
        int rCount = reviewService.getReviewsBySno(sno).size();
        System.out.println("rCount = " + rCount);
        System.out.println("<s" + sno + "가게의 태그수>");
        for(StoreTag storeTag : storeTags) {
            System.out.println(storeTag.getTag().getTtag() +"의 수: " + storeTag.getTagCount());
        }
        model.addAttribute("rCount", rCount);
        model.addAttribute("store", store);
        model.addAttribute("menus", menus);
        model.addAttribute("storeTags", storeTags);
        return "storeDetail";
    }*/
}