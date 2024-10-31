package com.example.foodingbyboot.repository;

import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.entity.MemberGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberGroupRepository extends JpaRepository<MemberGroup, Integer> {
    @Query("SELECT mg FROM MemberGroup mg WHERE mg.group.gno IN :gnos")
    List<MemberGroup> findByGroupGnoIn(@Param("gnos") List<Integer> gnos);

    // 특정 그룹(gno)과 회원(mid)으로 회원이 그룹에 존재하는지 확인하는 메서드
    @Query("SELECT CASE WHEN COUNT(mg) > 0 THEN true ELSE false END " +
            "FROM MemberGroup mg WHERE mg.group.gno = :gno AND mg.member.mid = :mid")
    boolean existsByGroupGnoAndMemberMid(@Param("gno") int gno, @Param("mid") String mid);

    @Query("SELECT mg " +
            "FROM MemberGroup mg " +
            "WHERE mg.group.gno = :gno AND mg.member.mid = :mid")
    MemberGroup findByGroupGnoAndMemberMid(@Param("gno") int gno, @Param("mid") String mid);

    @Query("SELECT mg FROM MemberGroup mg WHERE mg.member.mid = :mid AND mg.jauth = :jauth")
    List<MemberGroup> findByMemberMidAndJauth(@Param("mid") String memberId, @Param("jauth") int jauth);

    List<MemberGroup> findByMember(Member member);

    List<MemberGroup> findByGroupGno(int gno);

    @Query("SELECT mg FROM MemberGroup mg WHERE mg.group.gno = :gno AND mg.member.mnick = :nick")
    MemberGroup findByGroupGnoAndMemberNick(@Param("gno") int gno, @Param("nick") String nick);

    @Query("SELECT COUNT(mg) FROM MemberGroup mg WHERE mg.group.gno = :gno")
    int countByGroupGno(@Param("gno") int gno);

    @Query("SELECT mg FROM MemberGroup mg WHERE mg.group.gno = :groupId AND mg.jauth = 1")
    MemberGroup findLeaderMemberGroupByGroupId(@Param("groupId") int groupId);

    Optional<MemberGroup> findByJno(int jno);
    /*-------------------------------------- 메인화면에 모임방을 위해 추가한 메서드들 (다혜) ------------------------------------------------*/

    @Query("SELECT mg FROM MemberGroup mg WHERE mg.group.gno = :gno AND mg.jauth = 1")
    List<MemberGroup> findByGroupGnoAndJauthIsOne(@Param("gno") int gno);
 }