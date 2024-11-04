package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Menu;
import com.example.foodingbyboot.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public void saveMenu(Menu menu) {
        menuRepository.save(menu);
    }

    public List<Menu> getMenuBySno(int sno) {
        return menuRepository.findByStoreSno(sno);
    }
}

