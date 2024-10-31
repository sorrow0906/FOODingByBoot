package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.entity.Alarm;
import com.example.foodingbyboot.entity.Invite;
import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.service.AlarmService;
import com.example.foodingbyboot.service.InviteService;
import com.example.foodingbyboot.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AlarmService alarmService;
    @Autowired
    private InviteService inviteService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(String id, String password,HttpServletRequest request, Model model) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        Member member = memberService.findMemberById(id);

        if (member != null && passwordEncoder.matches(password, member.getMpass())) {
            // 로그인 성공
            HttpSession session = request.getSession();
            session.setAttribute("loggedInMember", member); // 세션에 로그인 정보 저장


            // 알림이 있는지 여부를 세션에 저장
            boolean hasAlarms = alarmService.hasAlarms(member);
            session.setAttribute("hasAlarms", hasAlarms);  // 세션에 알림 여부 저장

            if (hasAlarms) {
                List<Alarm> alarms = alarmService.getAlarmsByMember(member.getMid());
                session.setAttribute("alarms", alarms);  // 세션에 알림 리스트 저장

                boolean alarmChecked = true;
                for (Alarm alarm : alarms) {
                    if (alarm.getIsChecked() == 0) {
                        alarmChecked = false;
                    }

                    // invite가 null일 경우 처리
                    String inviterName = "";
                    String groupName = "";
                    String inviteeName = "";

                    Invite invite = inviteService.getInviteByIno(Integer.parseInt(alarm.getLinkedPk()));
                    if (invite != null) {
                        inviterName = invite.getMemberGroup().getMember().getMnick();
                        groupName = invite.getMemberGroup().getGroup().getGname();
                        inviteeName = invite.getMember().getMnick();

                        if (alarm.getAtype().equals("일반 회원 초대") || alarm.getAtype().equals("모임장 초대")) {
                            alarm.setMessage(inviterName + "님이 " + groupName + " 모임에<br>회원님을 초대하였습니다.");
                        } else if (alarm.getAtype().equals("초대 거절")) {
                            alarm.setMessage(inviteeName + "님이 초대를 거절하였습니다");
                        } else if (alarm.getAtype().equals("모임장 수락 대기")) {
                            alarm.setMessage(inviteeName + "님이<br>모임장 수락을 요청하였습니다.");
                        }
                    } else {
                        if (alarm.getAtype().equals("모임장 수락 거절1")) {
                            alarm.setMessage("회원님이 받은 초대를 모임장이<br>수락 거절하였습니다.");
                        } else if (alarm.getAtype().equals("모임장 수락 거절2")) {
                            alarm.setMessage("회원님의 초대를 모임장이<br>수락 거절하였습니다.");
                        } else {
                            alarm.setMessage("이미 삭제된 초대에 대한 알림입니다.");
                        }
                    }
                }
                session.setAttribute("alarmChecked", alarmChecked);  // 세션에 알림 확인 여부 저장
            } else {
                session.setAttribute("hasAlarms", false);  // 세션에 알림 없음 저장
            }


            model.addAttribute("member", member);
            model.addAttribute("message", "로그인 성공!!");

            return "redirect:/main";
        } else {
            // 로그인 실패 처리
            model.addAttribute("error", "아이디 또는 비밀번호가 <br> 일치하지 않습니다.");
            return "login"; // 다시 로그인 화면으로
        }
    }

    /*@GetMapping("/dashboard")
    public String showDashboard(HttpServletRequest request, Model model) {
        // 세션에서 로그인된 회원 정보 가져오기
        HttpSession session = request.getSession(false);
        System.out.println("이거 드들어가자 ?" +  session);
        if (session != null && session.getAttribute("loggedInMember") != null) {
            Member loggedInMember = (Member) session.getAttribute("loggedInMember");
            model.addAttribute("member", loggedInMember);
            System.out.println("나오냐고" + loggedInMember);
            return "dashboard"; // 대시보드 페이지로 이동
        } else {
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }
    }*/

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("loggedInMember");
            session.invalidate(); // 세션 무효화
        }

        return "redirect:/main";
    }


}
