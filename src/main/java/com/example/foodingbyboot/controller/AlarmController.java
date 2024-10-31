package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

//    @GetMapping("/top")
//    public String getTopPage(HttpSession session, Model model) {
//        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
//        System.out.println("get Controller 들어왔음");
//        if (loggedInMember != null) {
//            boolean hasAlarms = alarmService.hasAlarms(loggedInMember);
//            System.out.println("hasAlarms  = " + hasAlarms);
//            model.addAttribute("hasAlarms", hasAlarms);
//        }
//
//        return "top"; // 반환할 JSP 페이지 이름
//    }
}
