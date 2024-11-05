package com.example.foodingbyboot.service;
import com.example.foodingbyboot.entity.Tag;
import com.example.foodingbyboot.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> getTagsByRno(int rno) {
        return tagRepository.findTagsByRno(rno);
    }
}
