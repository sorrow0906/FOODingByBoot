package com.example.foodingbyboot.repository;

import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.entity.Pfolder;
import com.example.foodingbyboot.entity.Pick;
import com.example.foodingbyboot.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickRepository extends JpaRepository<Pick, Integer> {
    Pick findByMemberAndStore(Member member, Store store);

    List<Pick> findByMemberMno(@Param("mno") int mno);

    // pick수 계산을 위해 추가한 부분 (다혜)
    @Query("SELECT COUNT(p) FROM Pick p WHERE p.store.sno = :sno AND p.pfolder.pfno = 1")
    int countBySnoAndPfno(@Param("sno") int sno);

    List<Pick> findByPfolder(Pfolder pfolder);

    List<Pick> findByPfolder_PfnoAndMember_Mno(int pfno, int mno);

    void removeByStore_Sno(int sno);

    List<Pick> findByPfolder_PfnoAndMember_MnoAndStore_Sno(int pfno, int mno, int sno);

    void deleteByPfolder_PfnoAndStore_Sno(Integer pfno, Integer sno);

}