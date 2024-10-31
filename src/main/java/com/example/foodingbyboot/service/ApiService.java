package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Business;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "8CxruKocvsJPkdN9mSJfoPJyRhNOPUCbWfodrmGdF5spq9vyBvHV+Z/gW6Gv2hG3xpVAJ/8G0ETV+iLIiv6tdA==";
    private final String apiUrl = "https://api.odcloud.kr/api/nts-businessman/v1/validate?serviceKey=" + apiKey + "&returnType=JSON";

    public String checkBusiness(Business business) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("businesses", Collections.singletonList(business));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return e.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return "API 호출 중 오류가 발생했습니다.";
        }
    }
}
