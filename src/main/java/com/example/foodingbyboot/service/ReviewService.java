package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Review;
import com.example.foodingbyboot.repository.ReviewRepository;
import com.example.foodingbyboot.repository.ReviewTagRepository;
import com.example.foodingbyboot.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private  final StoreService storeService;

    private final TagRepository tagRepository;

    private final ReviewTagRepository reviewTagRepository;

    @Transactional
    public Review saveReview(Review review) {
        if (review.getRno() == 0) {
            review.setRdate(LocalDateTime.now());
        }
        // 가게 별점 평균 계산을 위해 추가(다혜)
        storeService.updateStoreInCache(review.getStore().getSno());

        return reviewRepository.save(review);
    }

    public Review getReviewByRno(int rno) {
        return (Review) reviewRepository.findByRno(rno).orElse(null);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Transactional
    public void deleteReviewTags(Review review) {
        reviewTagRepository.deleteTags(review);
    }

    /*--------------------------- 리뷰 미삭제 처리를 위해서 수정한 함수들 (다혜) ------------------------*/

    public List<Review> getReviewsBySno(int sno) {
        return reviewRepository.findValidReviewsByStoreSno(sno);
    }

    @Transactional
    public List<Review> getReviewsByMno(int mno) {
        return reviewRepository.findValidReviewsByMemberMno(mno);
    }

    public void deleteReviewByRno(int rno) {
        reviewRepository.markReviewAsDeleted(rno);
    }

}
