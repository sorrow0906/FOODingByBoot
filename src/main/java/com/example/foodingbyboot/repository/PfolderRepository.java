package com.example.foodingbyboot.repository;

import com.example.foodingbyboot.entity.Pfolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PfolderRepository extends JpaRepository<Pfolder, Integer> {
    Optional<Pfolder> findByPfno(int pfno);

    List<Pfolder> findByMemberMno(@Param("mno") int mno);

    List<Pfolder> findByPfno(List<Integer> pfnoList);
}
