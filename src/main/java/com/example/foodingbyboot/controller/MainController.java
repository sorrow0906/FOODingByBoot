package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.dto.MemberGroupDTO;
import com.example.foodingbyboot.entity.*;
import com.example.foodingbyboot.service.AlarmService;
import com.example.foodingbyboot.service.InviteService;
import com.example.foodingbyboot.service.MemberGroupService;
import com.example.foodingbyboot.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import jakarta.servlet.http.HttpSession;
import java.util.*;

@Controller
public class MainController {
    @Autowired
    private MemberGroupService memberGroupService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private AlarmService alarmService;
    @Autowired
    private InviteService inviteService;

    @GetMapping("/main")
    public String showMainPage(Model model, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        List<MemberGroupDTO> myMemberGroups = new ArrayList<>();
        Map<Integer, String> leaderList = new HashMap<>();
        Map<Integer, String> allMemberList = new HashMap<>();


        if (loggedInMember != null) {
            /*----------알림처리를 위한 부분-----------*/
            Boolean hasAlarms = (Boolean) session.getAttribute("hasAlarms");
            if (hasAlarms == null) {
                hasAlarms = false; // null이면 false로 처리
            }
            model.addAttribute("hasAlarms", hasAlarms);

            if (hasAlarms) {
                List<Alarm> alarms = (List<Alarm>) session.getAttribute("alarms");
                boolean alarmChecked = (Boolean) session.getAttribute("alarmChecked");

                model.addAttribute("alarms", alarms);
                model.addAttribute("alarmChecked", alarmChecked);
            }
            /*---------------------------------------*/

            myMemberGroups = memberGroupService.getMemberGroupsWithGroup(loggedInMember);

            if (myMemberGroups.isEmpty())
                model.addAttribute("myMemberGroups", "모임방x");
            else {
                for (MemberGroupDTO memberGroup : myMemberGroups) {
/*                    System.out.println(memberGroup.getGroup().getGimage());*/
                    int thisGno = memberGroup.getGroup().getGno();
                    // 해당 gno 그룹의 모든 맴버 닉네임을 한줄의 String으로 만들어서 gno와 함께 Map화 (key= gno, value= 모임방의 모든 맴버 닉네임)
                    allMemberList.put(thisGno, memberGroupService.findMnicksByGroupGno(thisGno));
                    // 해당 gno 그룹의 모임장을 찾아서 모임장의 닉네임을 gno와 함께 Map화 (key= gno, value= 모임장 닉네임)
                    if (memberGroupService.getLeaderByGno(thisGno) != null) {
                        leaderList.put(thisGno, memberGroupService.getLeaderByGno(thisGno).getMember().getMnick());
                    }
                }
                model.addAttribute("myMemberGroups", myMemberGroups);
                model.addAttribute("leaderList", leaderList);
                model.addAttribute("allMemberList", allMemberList);
            }
        }

        List<Store> stores1 = storeService.getAllStores();
        List<Store> stores2 = storeService.getAllStores();
        stores1.sort(Comparator.comparingDouble(Store::getScoreArg).reversed());
        List<Store> rankedByScoreStores = stores1.subList(0, 5);
        stores2.sort(Comparator.comparingDouble(Store::getPickNum).reversed());
        List<Store> rankedByPickStores = stores2.subList(0, 5);

        model.addAttribute("rankByScore", rankedByScoreStores);
        model.addAttribute("rankByPick", rankedByPickStores);


        return "main";
    }
	
	// 확인 버튼 클릭 시 알림의 isChecked 상태를 1로 변경 (희진 추가)
    @PostMapping("/alarmChecked")
    public String alarmChecked(@RequestParam("alarmId") int alarmId,@RequestParam("returnUrl") String returnUrl, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if (loggedInMember != null) {
            Alarm alarm = alarmService.findById(alarmId);
            if (alarm != null) {
                alarm.setIsChecked(1); // 확인된 상태로 설정
                alarmService.saveAlarm(alarm);
            }

            List<Alarm> alarms = alarmService.getAlarmsByMember(loggedInMember.getMid());
            session.setAttribute("alarms", alarms);

            boolean alarmChecked = true;
            for (Alarm a : alarms) {
                if (a.getIsChecked() == 0) {
                    alarmChecked = false;
                    break;
                }
            }
            session.setAttribute("alarmChecked", alarmChecked);
        }
        return "redirect:/main";
    }
	
	/* 알림 기능 추가 (희진) */
    @PostMapping("/alarmDelete")
    public String alarmDelete(@RequestParam("alarmId") int alarmId,  @RequestParam("returnUrl") String returnUrl, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if (loggedInMember != null) {
            alarmService.deleteAlarm(alarmId);

            List<Alarm> alarms = alarmService.getAlarmsByMember(loggedInMember.getMid());
            session.setAttribute("alarms", alarms);

            boolean alarmChecked = true;
            for (Alarm a : alarms) {
                if (a.getIsChecked() == 0) {
                    alarmChecked = false;
                    break;
                }
            }
            session.setAttribute("alarmChecked", alarmChecked);

            // 알림이 더 이상 없으면 hasAlarms를 false로 설정
            session.setAttribute("hasAlarms", !alarms.isEmpty());
        }
        return "redirect:" + returnUrl;
    }
}
