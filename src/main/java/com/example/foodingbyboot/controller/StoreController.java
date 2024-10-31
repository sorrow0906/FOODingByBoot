package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.entity.*;
import com.example.foodingbyboot.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class StoreController {

    @Autowired
    private StoreService storeService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private TagService tagService;
    @Autowired
    private LocationService locationService;

    private static final String DEFAULT_ARRD = "대구광역시 동구 동부로 121";
    private static final double DEFAULT_LAT = 35.8799906; // 대구광역시 동구 동부로 121의 위도
    private static final double DEFAULT_LON = 128.6286206; // 대구광역시 동구 동부로 121의 경도

    @GetMapping("/storeList")
    public String showStoreList(@RequestParam(value = "scate", required = false) String scate, Model model) {
        List<Store> stores;
        if (scate != null && !scate.isEmpty()) {
            stores = storeService.getStoresByCategory(scate);
        } else {
            stores = storeService.getAllStores();
        }
        model.addAttribute("scate", scate);
        model.addAttribute("stores", stores);
        return "storeList";
    }

    @GetMapping("/storeDetail")
    public String storeDetail(@RequestParam("sno") int sno, Model model) {
        Store store = storeService.getStoreAllInfo(sno);
        List<Menu> menus = menuService.getMenuBySno(sno);
        List<StoreTag> storeTags = storeService.getStoreTagsByStoreSno(sno);
        int rCount = reviewService.getReviewsBySno(sno).size();
/*        System.out.println("rCount = " + rCount);
        System.out.println("<s" + sno + "가게의 태그수>");*/
/*        for(StoreTag storeTag : storeTags) {
            System.out.println(storeTag.getTag().getTtag() +"의 수: " + storeTag.getTagCount());
        }*/
        model.addAttribute("rCount", rCount);
        model.addAttribute("store", store);
        model.addAttribute("menus", menus);
        model.addAttribute("storeTags", storeTags);
        return "storeDetail";
    }

    @GetMapping("/storeInfo")
    public String storeInfo(@RequestParam("sno") int sno, Model model) {
        Store store = storeService.getStoreById(sno);
        List<Menu> menus = menuService.getMenuBySno(sno);
        model.addAttribute("store", store);
        model.addAttribute("menus", menus);
        return "storeInfo";
    }

    @GetMapping("/storeListByLocation")
    public String showStoreListByLocation(
            @RequestParam(value = "userLat", required = false) Double userLat,
            @RequestParam(value = "userLon", required = false) Double userLon,
            @RequestParam(value = "inputAddr", required = false) String inputAddr,
            Model model) throws Exception {

        System.out.println("inputAddr = "+ inputAddr);
        if (userLat == null || userLon == null) {
            userLat = DEFAULT_LAT;
            userLon = DEFAULT_LON;
        }
        if (inputAddr != null) {
            double[] coordinates = locationService.getCoordinates(inputAddr);
            userLat = coordinates[0];
            userLon = coordinates[1];
        }

        if(inputAddr == null)
            inputAddr = LocationService.getAddr(userLat, userLon);

        List<Store> stores = storeService.getNearbyStores(userLat, userLon);
        stores.sort(Comparator.comparingDouble(Store::getDistance));

/*        for (Store store : stores) {
            System.out.println(store.getSname() + "의 거리는 " + store.getDistance());
        }*/


        model.addAttribute("stores", stores);
        model.addAttribute("nowAddr", inputAddr);
        model.addAttribute("defaultLat", DEFAULT_LAT);
        model.addAttribute("defaultLon", DEFAULT_LON);
        return "storeListByLocation";
    }


    @GetMapping("/storeListByRank")
    public String showStoreListByPick(@RequestParam(value = "sortBy", required = false) String sortBy, Model model) {
        List<Store> stores = storeService.getAllStores();

        if ("score".equals(sortBy)) {
            stores.sort(Comparator.comparingDouble(Store::getScoreArg).reversed());
            model.addAttribute("sortStandard", "score");
        } else {
            stores.sort(Comparator.comparingInt(Store::getPickNum).reversed());
            model.addAttribute("sortStandard", "pick");
        }

        model.addAttribute("stores", stores);

        return "storeListByRank";
    }
    @GetMapping("/storeListByTag")
    public String showStoreListByTag(@RequestParam(value = "sortBy", required = false) String sortBy, @RequestParam(value = "tnos", required = false) String tnos, Model model/*, Integer tno*/) {

        List<Tag> allTags = tagService.getAllTags();
        model.addAttribute("allTags", allTags);

        List<Store> storesByTag = storeService.getAllStores();
        if (tnos != null && !tnos.isEmpty()) {
            String[] stringTnos = tnos.split(",");
            List<Integer> numTnos = new ArrayList<>();
            // 가져온 태그들을 서버로그에 띄우기 위해서 사용
            for (String tno : stringTnos) {
                System.out.println(tno);
                numTnos.add(Integer.parseInt(tno));
            }

            for (int tno : numTnos) {
                storesByTag = storeService.getStoresByTagCountAndTno(tno, storesByTag);
            }
        }

        if ("score".equals(sortBy)) {
            storesByTag.sort(Comparator.comparingDouble(Store::getScoreArg).reversed());
            model.addAttribute("sortStandard", "score");
        } else {
            storesByTag.sort(Comparator.comparingInt(Store::getPickNum).reversed());
            model.addAttribute("sortStandard", "pick");
        }

        model.addAttribute("stores", storesByTag);

        return "storeListByTag";
    }

    @GetMapping("/storeListByScate")
    public String showStoreListByScate(@RequestParam(value = "sortBy", required = false) String sortBy, @RequestParam(value = "scates", required = false) String scates, Model model) {
        System.out.println(scates);
        List<String> allScates = List.of("한식", "일식", "중식", "양식", "세계요리", "빵/디저트", "차/커피", "술집");;
        model.addAttribute("allScates", allScates);

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
            model.addAttribute("sortStandard", "score");
        } else {
            storesByScate.sort(Comparator.comparingInt(Store::getPickNum).reversed());
            model.addAttribute("sortStandard", "pick");
        }

        model.addAttribute("stores", storesByScate);

        return "storeListByScate";
    }


    @GetMapping("/searchResultView")
    public String searchStoreByKeyword(@RequestParam(value = "sortBy", required = false) String sortBy, @RequestParam(value = "searchKeyword", required = false) String searchKeyword, Model model) {

        model.addAttribute("searchKeyword", searchKeyword);
        List<Store> storesBykeyword = storeService.getAllStores();
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            searchKeyword = searchKeyword.replaceAll("[^a-zA-Z0-9가-힣\\s]", "");
            String[] keywords = searchKeyword.split("\\s+");

            for (String keyword : keywords) {
                System.out.println("검색 키워드: "+ keyword);
                storesBykeyword = storeService.getStoresBykeyword(keyword, storesBykeyword);
            }
        }

        storesBykeyword.sort(Comparator.comparingDouble(Store::getSno).reversed());
        if ("score".equals(sortBy)) {
            storesBykeyword.sort(Comparator.comparingDouble(Store::getScoreArg).reversed());
            model.addAttribute("sortStandard", "score");
        } else {
            storesBykeyword.sort(Comparator.comparingInt(Store::getPickNum).reversed());
            model.addAttribute("sortStandard", "pick");
        }

        model.addAttribute("stores", storesBykeyword);

        return "searchResultView";
    }
}