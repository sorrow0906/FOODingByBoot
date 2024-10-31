package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.repository.MemberGroupRepository;
import com.example.foodingbyboot.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberGroupRepository memberGroupRepository;

    public Member saveMember(Member member) {
        member.setMdate(LocalDateTime.now()); // 가입 날짜를 현재 날짜로 설정
        return memberRepository.save(member);
    }

    public Member login(String username, String password) {
        Optional<Member> memberOpt = memberRepository.findByMid(username);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            if (member.getMpass().equals(password)) {
                return member; // 로그인 성공
            }
        }
        return null; // 로그인 실패
    }

    public Member findMemberByMno(int mno) {
        return memberRepository.findByMno(mno).orElse(null);
    }

    public Member findMemberById(String mid) {
        return memberRepository.findByMid(mid).orElse(null);
    }

    public boolean isMnoExists(int mno) {return memberRepository.existsByMno(mno);}

    public void updateMember(Member member) {
        if (memberRepository.existsByMid(member.getMid())) {
            memberRepository.save(member);
        }
    }

    public boolean isMidExists(String mid) {
        return memberRepository.existsByMid(mid);
    }
    public boolean isMnickExists(String mnick) {
        return memberRepository.existsByMnick(mnick);
    }


    public void deleteMemberByMno(int mno) {
        memberRepository.deleteByMno(mno);
    }


    public String findIdByMnameEmailAndPhone(String mname, String memail, String mphone) {
        Member memberByEmail = memberRepository.findByMnameAndMemail(mname, memail);
        Member memberByPhone = memberRepository.findByMnameAndMphone(mname, mphone);

        if (memberByEmail != null && memberByPhone != null && memberByEmail.getMno() == memberByPhone.getMno()) {
            return maskId(memberByEmail.getMid());
        } else {
            return null;
        }
    }

    private String maskId(String id) {
        if (id.length() <= 3) {
            return id.charAt(0) + "**";
        }
        StringBuilder maskedId = new StringBuilder();
        maskedId.append(id.charAt(0));
        for (int i = 1; i < id.length() - 2; i++) {
            maskedId.append("*");
        }
        maskedId.append(id.substring(id.length() - 2));
        return maskedId.toString();
    }

    public Optional<Member> findByMember(String mid, String mname, String memail) {
        return memberRepository.findByMidAndMnameAndMemail(mid, mname, memail);
    }


    // 모임방 기능을 위한 추가 (수정자 : 희진)
    public Member getMemberById(String mid) {
        return  memberRepository.findByMid(mid).orElse(null);
    }

}
