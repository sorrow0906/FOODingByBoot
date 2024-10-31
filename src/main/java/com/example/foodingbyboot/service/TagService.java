package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.ReviewTag;
import com.example.foodingbyboot.entity.Tag;
import com.example.foodingbyboot.repository.ReviewTagRepository;
import com.example.foodingbyboot.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ReviewTagRepository reviewTagRepository;

    public Tag getTagByTno(int tno) {
        return tagRepository.findByTno(tno);
    }

    public void saveReviewTag(ReviewTag reviewTag) {
        reviewTagRepository.save(reviewTag);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> getTagsByRno(int rno) {
        return tagRepository.findTagsByRno(rno);
    }

}
