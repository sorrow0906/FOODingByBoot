package com.example.foodingbyboot.repository;

import com.example.foodingbyboot.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {
    List<Menu> findByStoreSno(int sno);
}
