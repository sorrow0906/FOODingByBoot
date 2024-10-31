package com.example.foodingbyboot.repository;

import com.example.foodingbyboot.entity.Write;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WriteRepository extends JpaRepository<Write, Integer> {

    List<Write> findByBoardBno(int bno);
    int countByBoardBno(int bno);

    Optional<Object> findByWno(int wno);
}
