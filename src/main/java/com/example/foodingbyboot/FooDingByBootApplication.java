package com.example.foodingbyboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.example.foodingbyboot.entity")
@EnableJpaRepositories(basePackages = "com.example.foodingbyboot.repository")
public class FooDingByBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(FooDingByBootApplication.class, args);
    }

}
