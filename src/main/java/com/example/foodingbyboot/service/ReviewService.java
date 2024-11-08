package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Review;
import com.example.foodingbyboot.repository.ReviewRepository;
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
    private final StoreService storeService;

    public List<Review> getReviewsBySno(int sno) {
        return reviewRepository.findValidReviewsByStoreSno(sno);
    }

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

    @Transactional
    public void deleteReviewByRno(int rno) {
        reviewRepository.markReviewAsDeleted(rno);
    }

}