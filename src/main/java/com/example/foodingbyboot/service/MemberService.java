package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member findMemberById(String mid) {
        return memberRepository.findByMid(mid).orElse(null);
    }
}
