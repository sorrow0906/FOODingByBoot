package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Review;
import com.example.foodingbyboot.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    public List<Review> getReviewsBySno(int sno) {
        return reviewRepository.findValidReviewsByStoreSno(sno);
    }
}