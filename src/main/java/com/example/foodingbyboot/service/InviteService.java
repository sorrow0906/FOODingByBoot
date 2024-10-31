package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Invite;
import com.example.foodingbyboot.repository.InviteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class InviteService {

    @Autowired
    private InviteRepository inviteRepository;
    @Autowired
    private AlarmService alarmService;

    public Invite findById(int inviteId) {
        return inviteRepository.findByIno(inviteId);
    }

    public void saveInvite(Invite invite) {
        inviteRepository.save(invite);
    }

    // ino로 Invite 엔티티를 조회하는 메소드
    public Invite getInviteByIno(int ino) {
        return inviteRepository.findByIno(ino);
    }

    // 초대받는 회원의 mno로 초대 목록을 조회하는 메서드
    public List<Invite> getInvitesByMemberMno(int mno) {
        return inviteRepository.findByMember_Mno(mno);
    }

    @Transactional
    public void deleteInvite(int ino) {
        alarmService.deleteAlarmsByInviteId(ino);
        inviteRepository.deleteById(ino);
    }

    /*----------------모임장 수락을 위해 추가한 부분(다혜)-----------------------*/
    public List<Invite> getInvitesByLeadNum(int mno) {
        List<Invite> invites = inviteRepository.findByLeadNum(mno);

        invites.removeIf(invite -> invite.getItype() != 1);

        return invites;
    }

    public boolean checkInviteExists(int mno, int gno) {
        return inviteRepository.existsByMnoAndGno(mno, gno);
    }

    public void deleteInviteWithoutAlarm(int ino) {
        inviteRepository.deleteById(ino);
    }

    public void deleteAlarmsByIno(int ino) {
        alarmService.deleteAlarmsByInviteId(ino);
    }
}