package com.example.foodingbyboot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.foodingbyboot.entity.Menu;
import com.example.foodingbyboot.entity.Store;
import com.example.foodingbyboot.service.MenuService;
import com.example.foodingbyboot.service.StoreService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StoreDataLoader {

    @Autowired
    private StoreService storeService;

    @Autowired
    private MenuService menuService;

    private static final String URL = "https://www.daegufood.go.kr/kor/api/tasty.html?mode=json&addr=%EC%A4%91%EA%B5%AC";

    @PostConstruct
    public void init() throws Exception {
        // DB에 데이터가 있는지 확인~
        if (storeService.getAllStores().isEmpty()) {
            List<JsonNode> dataList = fetchDataFromApi();
            for (JsonNode node : dataList) {
                Store store = new Store();
                store.setSno(node.get("cnt").asInt()); // cnt 필드를 사용하여 sno 설정
                store.setSname(node.get("BZ_NM").asText());
                store.setSaddr(node.get("GNG_CS").asText());
                store.setStel(node.get("TLNO").asText());
                store.setSeg(node.get("SMPL_DESC").asText());
                // FD_CS 값을 변환!
                String fdCs = node.get("FD_CS").asText();
                switch (fdCs) {
                    case "디저트/베이커리":
                        fdCs = "빵/디저트";
                        break;
                    case "전통차/커피전문점":
                        fdCs = "차/커피";
                        break;
                    case "특별한 술집":
                        fdCs = "술집";
                        break;
                    default:
                        // 변환이 필요 없는 경우 그대로 사용!!
                        break;
                }
                store.setScate(fdCs);

                store.setStime(node.get("MBZ_HR").asText());

                // spark 필드의 길이 검사 및 잘라내기 (설명이 너무 길면 공간이 안 되어서 자름)
                String spark = node.get("PKPL").asText();
                if (spark.length() > 20) {
                    spark = spark.substring(0, 20);
                }
                store.setSpark(spark);

                storeService.saveStore(store);

                // 메뉴 저장
                String menuString = node.get("MNU").asText();
                if (menuString != null && !menuString.isEmpty()) {
                    List<Menu> menus = parseMenuString(menuString, store);
                    for (Menu menu : menus) {
                        try {
                            menuService.saveMenu(menu);
                        } catch (Exception e) {
                            System.err.println("Error saving menu: " + menu.getMnname() + " - " + menu.getMnprice());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private List<JsonNode> fetchDataFromApi() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(URL);

        // Content-Type 및 Accept-Charset 헤더를 추가하여 UTF-8 인코딩을 지정
        request.setHeader("Content-Type", "application/json; charset=UTF-8");
        request.setHeader("Accept-Charset", "UTF-8");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // UTF-8 인코딩으로 응답을 변환
                String result = EntityUtils.toString(entity, "UTF-8");

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(result);

                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

                // "data" 노드가 존재하는지 확인하고 리스트로 변환
                JsonNode dataNode = jsonNode.get("data");
                if (dataNode != null && dataNode.isArray()) {
                    return objectMapper.convertValue(dataNode, new TypeReference<List<JsonNode>>() {});
                } else {
                    System.out.println("No data found in API response.");
                }
            }
        }
        return null;
    }

    private List<Menu> parseMenuString(String menuString, Store store) {
        List<Menu> menus = new ArrayList<>();
        if (menuString != null && !menuString.isEmpty()) {
            // 메뉴 항목을 <br /> 태그 기준으로 분리
            String[] menuItems = menuString.split("\\u003Cbr /\\u003E");
            for (String item : menuItems) {
                // '[채식'으로 시작하는 항목 넘김!
                if (item.startsWith("[채식")) {
                    continue;
                }

                // 가격 부분을 정규 표현식으로 추출 (수정된 정규 표현식)
                String priceRegex = "(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?(?:원)?(?:\\s*~\\s*|~)\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?(?:원)?|\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?원)";
                Pattern pattern = Pattern.compile(priceRegex);
                Matcher matcher = pattern.matcher(item);

                String menuPrice = "";
                String menuName = item;

                // 가격을 먼저 추출 -> 메뉴 이름에서 제거
                if (matcher.find()) {
                    menuPrice = matcher.group(0);
                    menuName = item.replace(menuPrice, "").trim();
                }

                // 메뉴 이름의 길이를 50자로 제한 (너무 긴 이름이 있음...)
                if (menuName.length() > 50) {
                    menuName = menuName.substring(0, 50);
                }

                Menu menu = new Menu();
                menu.setMnname(menuName);
                menu.setMnprice(menuPrice);
                menu.setStore(store);
                menus.add(menu);
            }
        }
        return menus;
    }
}