package com.example.foodingbyboot.repository;

import com.example.foodingbyboot.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Integer> {
    // Invite 엔티티를 ino로 찾는 메소드
    Invite findByIno(int ino);

    // 초대받는 회원의 mno로 초대 목록을 조회하는 메서드
    List<Invite> findByMember_Mno(int mno);

    /*----------------모임장 수락을 위해 추가한 부분(다혜)-----------------------*/

    @Query("SELECT i FROM Invite i WHERE i.leadNum = :leadNumber")
    List<Invite> findByLeadNum(@Param("leadNumber") int leadNumber);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END FROM Invite i " +
            "WHERE i.member.mno = :mno AND i.memberGroup.group.gno = :gno")
    boolean existsByMnoAndGno(@Param("mno") int mno, @Param("gno") int gno);
}
