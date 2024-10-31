package com.example.foodingbyboot.repository;

import com.example.foodingbyboot.entity.Review;
import com.example.foodingbyboot.entity.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.util.List;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, Integer> {
    List<ReviewTag> findByReview(Review review);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReviewTag rt WHERE rt.review = :review")
    void deleteTags(@Param("review") Review review);

    //store 대표태그 기능을 위해 추가한 부분 (다혜)
    @Query("SELECT rt FROM ReviewTag rt WHERE rt.review.store.sno = :sno AND (rt.review.mdelete IS NULL OR rt.review.mdelete != 1) AND (rt.review.adelete IS NULL OR rt.review.adelete != 1)")
    List<ReviewTag> findValidReviewTagsByStoreSno(@Param("sno") int sno);
}