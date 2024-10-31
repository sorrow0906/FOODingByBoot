package com.example.foodingbyboot;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api") // 기본 경로 설정
public class TestController {

    @GetMapping("/stores-test") // 최종 경로: /api/stores-test
    public ResponseEntity<Map<String, Object>> showStoreListByScate(
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "scates", required = false) String scates) {
        // 테스트용 데이터
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Success");
        response.put("sortBy", sortBy);
        response.put("scates", scates);
        return ResponseEntity.ok(response);
    }
}