package com.example.foodingbyboot.contoller;

import com.example.foodingbyboot.entity.*;
import com.example.foodingbyboot.repository.ReviewRepository;
import com.example.foodingbyboot.repository.StoreTagRepository;
import com.example.foodingbyboot.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api") // 기본 경로 설정
public class TestController {
    private final StoreService storeService;
    private final ReviewService reviewService;
    private final MenuService menuService;
    private final PickService pickService;
    private final TagService tagService;
    private final MemberService memberService;
    private final StoreTagRepository storeTagRepository;

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

    @GetMapping("/review")
    public ResponseEntity<Map<String, Object>> review(
            @RequestParam("sno") int sno,
            @RequestParam(value = "sortBy", required = false) String sortBy) {

        Map<String, Object> response = new HashMap<>();

        // 리뷰 조회
        List<Review> reviews = reviewService.getReviewsBySno(sno);
        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        // 정렬 로직
        switch (sortBy) {
            case "oldest":
                reviews.sort(Comparator.comparing(Review::getRdate));
                break;
            case "highest":
                reviews.sort(Comparator.comparing(Review::getRstar).reversed());
                break;
            case "lowest":
                reviews.sort(Comparator.comparing(Review::getRstar));
                break;
            default:
                reviews.sort(Comparator.comparing(Review::getRdate).reversed());
        }

        // Store 및 태그 조회
        Store store = storeService.getStoreById(sno);
        if (store == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Store not found for sno: " + sno));
        }

        List<Tag> allTags = tagService.getAllTags();
        reviews.forEach(review -> {
            review.setDateToString(review.getRdate().format(DateTimeFormatter.ofPattern("yy-MM-dd")));
            List<Tag> tags = tagService.getTagsByRno(review.getRno());
            review.setTags(tags);
        });

        // 응답 데이터 설정
        response.put("reviews", reviews);
        response.put("review", new Review());
        response.put("sno", sno);
        response.put("store", store);
        response.put("isEmpty", reviews.isEmpty());
        response.put("tags", allTags);
        response.put("sortBy", sortBy);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        String id = credentials.get("id");
        String password = credentials.get("password");

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Map<String, String> response = new HashMap<>();

        System.out.println("아이디 = " + id);
        System.out.println("비밀번호 = " + password);

        Member member = memberService.findMemberById(id);

        if (member != null && passwordEncoder.matches(password, member.getMpass())) {
            // 로그인 성공
            HttpSession session = (HttpSession) request.getSession();
            session.setAttribute("loggedInMember", member); // 세션에 로그인 정보 저장

            response.put("message", "로그인 성공");
            return ResponseEntity.ok(response); // HTTP 200 OK와 함께 성공 메시지 반환
        } else {
            // 로그인 실패 처리
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // HTTP 401 Unauthorized와 함께 에러 메시지 반환
        }
    }

}