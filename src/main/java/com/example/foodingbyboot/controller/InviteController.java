package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.entity.Alarm;
import com.example.foodingbyboot.entity.Group;
import com.example.foodingbyboot.entity.Invite;
import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.service.AlarmService;
import com.example.foodingbyboot.service.InviteService;
import com.example.foodingbyboot.service.MemberGroupService;
import com.example.foodingbyboot.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class InviteController {
    @Autowired
    private InviteService inviteService;
    @Autowired
    private AlarmService alarmService;
    @Autowired
    private MemberGroupService memberGroupService;
    @Autowired
    private MemberService memberService;

    @GetMapping("/inviteManage")
    public String inviteManage(Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        // 로그인한 회원의 mno를 가져옵니다.
        int mno = member.getMno();

        // 사용자의 mno로 초대 목록을 조회합니다.
        List<Invite> invites = inviteService.getInvitesByMemberMno(mno);

        // 초대 목록을 모델에 추가합니다.
        model.addAttribute("invites", invites);

        return "inviteManage";
    }

    @PostMapping("/acceptInvite")
    public String acceptInvite(@RequestParam("inviteId") int inviteId, HttpSession session) {

        Invite invite = inviteService.findById(inviteId);
        if (invite != null) {
            if (invite.getItype() == 0) {
                invite.setItype(1); // itype을 1로 변경

                // 수락했으므로 이전에 있던 초대 알람 삭제
                inviteService.deleteAlarmsByIno(inviteId);

                // 새로운 알림 엔티티 생성 및 설정
                Alarm alarm = new Alarm();
                alarm.setLinkedPk(String.valueOf(invite.getIno())); // 초대 엔티티의 ino 값을 문자열로 설정
                alarm.setAtype("모임장 수락 대기");
                // 알림을 받을 회원 (초대한 회원의 mno를 통해 찾아야 함)
                Member inviter = memberService.findMemberByMno(invite.getLeadNum());
                alarm.setMember(inviter); // 초대한 회원을 알림의 대상자로 설정
                alarm.setIsChecked(0); // 알림 확인 여부를 미확인 상태로 설정 (0)

                // 알림 정보 저장
                alarmService.saveAlarm(alarm);

                // 바뀐 초대정보 저장
                inviteService.saveInvite(invite);

            } else if (invite.getItype() == 6) {
                invite.setItype(7); // itype을 7로 변경

                // 초대받은 회원과 모임 정보 가져오기
                Member invitedMember = invite.getMember();
                Group group = invite.getMemberGroup().getGroup();

                // 초대받은 회원을 모임에 일반회원으로 추가
                memberGroupService.addMemberToGroup(invitedMember, group, 0); // 0은 일반회원 권한

                //모임장이 승인했으니 초대 삭제(다혜)
                inviteService.deleteInvite(invite.getIno());
            }
        }

        return "redirect:/inviteManage";
    }

    @PostMapping("/rejectInvite")
    public String rejecttInvite(@RequestParam("inviteId") int inviteId, HttpSession session) {

        Invite invite = inviteService.findById(inviteId);
        if (invite != null) {
            if (invite.getItype() == 0) {
                invite.setItype(2); // itype을 2로 변경
            } else if (invite.getItype() == 6) {
                invite.setItype(8); // itype을 8로 변경
            }
            inviteService.saveInvite(invite); // 업데이트된 엔티티를 저장

            // 알림 엔티티 생성 및 설정
            Alarm alarm = new Alarm();
            alarm.setLinkedPk(String.valueOf(invite.getIno())); // 초대 엔티티의 ino 값을 문자열로 설정
            alarm.setAtype("초대 거절");
            // 초대한 회원의 mno로 알림을 받을 회원 설정
            Member invitingMember = invite.getMemberGroup().getMember(); // 초대한 회원
            alarm.setMember(invitingMember); // 알림을 받을 회원
            alarm.setIsChecked(0); // 확인 여부는 0 (미확인 상태)

            // 알림 정보 저장
            alarmService.saveAlarm(alarm);
        }

        return "redirect:/inviteManage";
    }

    @PostMapping("/deleteInvite")
    public String deleteInvite(@RequestParam("inviteId") int inviteId) {
        inviteService.deleteInvite(inviteId);
        return "redirect:/inviteManage";
    }



    /*------------------모임장 초대 수락을 위해 추가한 부분(다혜)-------------*/

    @PostMapping("/leaderAcceptInvite")
    public String leaderAcceptInvite(@RequestParam("inviteId") int inviteId, HttpSession session) {

        Invite invite = inviteService.findById(inviteId);
        if (invite != null) {
            if (invite.getItype() == 1) {
                invite.setItype(3); // itype을 1로 변경

                // 모임장이 승인했다는 알림 생성
                Alarm alarm = new Alarm();
                alarm.setLinkedPk(String.valueOf(invite.getIno())); // 초대 엔티티의 ino 값을 문자열로 설정
                alarm.setAtype("모임장 수락");
                // 알림을 받을 회원 설정
                Member invitee = invite.getMember();
                alarm.setMember(invitee); // 초대받은 회원을 알림의 대상자로 설정
                alarm.setIsChecked(0);

                // 알림 정보 저장
                alarmService.saveAlarm(alarm);

                // 회원을 모임에 추가
                Member invitedMember = invite.getMember();
                Group group = invite.getMemberGroup().getGroup();
                memberGroupService.addMemberToGroup(invitedMember, group, 0);

                inviteService.deleteInvite(invite.getIno()); // 초대 삭제
            }

        }

        return "redirect:/groupManage";
    }

    @PostMapping("leaderRejectInvite")
    public String leaderRejectInvite(@RequestParam("inviteId") int inviteId, HttpSession session) {

        Invite invite = inviteService.findById(inviteId);
        if (invite != null) {
            invite.setItype(4); // itype을 1로 변경

            // 이전에 생성되었던 해당 초대에 대한 알람 모두 삭제
            inviteService.deleteAlarmsByIno(inviteId);

            // 초대받은 사람에게 모임장이 승인 거절했다는 알림 생성
            Alarm alarmToInvitee = new Alarm();
            alarmToInvitee.setLinkedPk(String.valueOf(invite.getIno())); // 초대 엔티티의 ino 값을 문자열로 설정
            alarmToInvitee.setAtype("모임장 수락 거절1");
            // 알림을 받을 회원 설정
            Member invitee = invite.getMember();
            alarmToInvitee.setMember(invitee); // 초대받은 회원을 알림의 대상자로 설정
            alarmToInvitee.setIsChecked(0);

            // 알림 정보 저장
            alarmService.saveAlarm(alarmToInvitee);


            // 초대한 사람에게 모임장이 승인 거절했다는 알림 생성
            Alarm alarmToInviter = new Alarm();
            alarmToInviter.setLinkedPk(String.valueOf(invite.getIno())); // 초대 엔티티의 ino 값을 문자열로 설정
            alarmToInviter.setAtype("모임장 수락 거절2");
            // 알림을 받을 회원 설정
            Member inviter = invite.getMemberGroup().getMember();
            alarmToInviter.setMember(inviter); // 초대한 회원을 알림의 대상자로 설정
            alarmToInviter.setIsChecked(0);

            // 알림 정보 저장
            alarmService.saveAlarm(alarmToInviter);

            // 거절 받았다는 알림을 삭제하지 않고 invite만 삭제
            inviteService.deleteInviteWithoutAlarm(invite.getIno()); // 초대 삭제
        }

        return "redirect:/groupManage";
    }

}