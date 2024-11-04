package com.example.foodingbyboot.contoller;

import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.entity.Menu;
import com.example.foodingbyboot.entity.Store;
import com.example.foodingbyboot.entity.StoreTag;
import com.example.foodingbyboot.repository.ReviewRepository;
import com.example.foodingbyboot.service.MenuService;
import com.example.foodingbyboot.service.PickService;
import com.example.foodingbyboot.service.ReviewService;
import com.example.foodingbyboot.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api") // 기본 경로 설정
public class TestController {
    private final StoreService storeService;
    private final ReviewService reviewService;
    private final MenuService menuService;
    private final PickService pickService;

    @GetMapping("/stores-main") // 최종 경로: /api/stores-test
    public ResponseEntity<Map<String, Object>> showStoreListByScate(
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "scates", required = false) String scates) {
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

    @GetMapping("/store-detail")
    public ResponseEntity<Map<String, Object>> storeDetail(@RequestParam("sno") int sno) {
        Map<String, Object> response = new HashMap<>();
        Store store = storeService.getStoreAllInfo(sno);
        List<Menu> menus = menuService.getMenuBySno(sno);
        List<StoreTag> storeTags = storeService.getStoreTagsByStoreSno(sno);
        int rCount = reviewService.getReviewsBySno(sno).size();
        System.out.println("rCount = " + rCount);
        System.out.println("<s" + sno + "가게의 태그수>");
        for(StoreTag storeTag : storeTags) {
            System.out.println(storeTag.getTag().getTtag() +"의 수: " + storeTag.getTagCount());
        }
        response.put("rCount", rCount);
        response.put("store", store);
        response.put("menus", menus);
        response.put("storeTags", storeTags);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pick")
    @ResponseBody
    public ResponseEntity<Map<String, String>> pickStore(
            @RequestParam("sno") int sno,
            @RequestParam(value = "pfno", defaultValue = "1") int pfno,
            HttpSession session) {

        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Unauthorized
        }

        int mno = loggedInMember.getMno();
        boolean isPicked = pickService.togglePick(mno, sno, pfno);
        Map<String, String> response = new HashMap<>();
        response.put("status", isPicked ? "picked" : "unpicked");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkPick")
    @ResponseBody
    public ResponseEntity<Map<String, String>> checkPick(
            @RequestParam("sno") int sno,
            HttpSession session) {

        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "unpicked");
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Unauthorized
        }

        int mno = loggedInMember.getMno();
        boolean isPicked = pickService.isPicked(mno, sno);
        Map<String, String> response = new HashMap<>();
        response.put("status", isPicked ? "picked" : "unpicked");
        return ResponseEntity.ok(response);
    }

}