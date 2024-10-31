package com.example.foodingbyboot.repository;

import com.example.foodingbyboot.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findByRno(int rno);
    List<Review> findByStore_Sno(int sno); // 수정된 부분
    List<Review> findByMember_Mno(int mno);


    //별점 평균을 구하기 위해서 추가한 부분 (다혜)
    @Query("SELECT AVG(r.rstar) FROM Review r WHERE r.store.sno = :sno AND (r.mdelete IS NULL OR r.mdelete != 1) AND (r.adelete IS NULL OR r.adelete != 1)")
    Double findAverageRstarBySno(@Param("sno") int sno);



    /* --------------------------- 리뷰 미삭제 처리를 하기 위해 추가한 부분-------------------------------*/
    @Query("SELECT r FROM Review r WHERE r.store.sno = :sno AND (r.mdelete IS NULL OR r.mdelete != 1) AND (r.adelete IS NULL OR r.adelete != 1)")
    List<Review> findValidReviewsByStoreSno(@Param("sno") int sno);

    @Query("SELECT r FROM Review r WHERE r.member.mno = :mno AND (r.mdelete IS NULL OR r.mdelete != 1) AND (r.adelete IS NULL OR r.adelete != 1)")
    List<Review> findValidReviewsByMemberMno(@Param("mno") int mno);

    //리뷰 신고를 위해 추가한 부분
    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.mdelete = 1 WHERE r.rno = :rno")
    void markReviewAsDeleted(@Param("rno") Integer rno);

}