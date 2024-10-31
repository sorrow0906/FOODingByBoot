package com.example.foodingbyboot.repository;

import com.example.foodingbyboot.entity.Alarm;
import com.example.foodingbyboot.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Integer> {
    // 로그인한 회원이 받은 모든 알림 조회
    List<Alarm> findByMember(Member member);

    // 회원 ID로 알림 조회
    List<Alarm> findByMember_Mid(String memberId);

    // 알림 ID로 알림 조회
    Alarm findByAno(int ano);

    @Modifying
    @Query("DELETE FROM Alarm a WHERE a.linkedPk = ?1")
    void deleteByLinkedPk(String linkedPk);
}
