package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.dto.GroupDTO;
import com.example.foodingbyboot.dto.MemberGroupDTO;
import com.example.foodingbyboot.entity.*;
import com.example.foodingbyboot.service.*;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private MemberGroupService memberGroupService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private ServletContext servletContext;

    @GetMapping("/groupList")
    public String bringGroupList(Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        String nick = member.getMnick();
//        System.out.println("Member의 이름: " + nick);

        Map<Integer, Integer> memberCount = new HashMap<>();
        List<MemberGroupDTO> memberGroups = memberGroupService.getMemberGroupsWithGroup(member);
        for (MemberGroupDTO memberGroup : memberGroups) {
             memberCount.put(memberGroup.getGroup().getGno(), groupService.groupMemberCount(memberGroup.getGroup().getGno()));
//            System.out.println(memberGroup.getJno() + "의 getGroup().getGname() = :" + memberGroup.getGroup().getGname());

            LocalDateTime gdate = memberGroup.getGroup().getGdate();
            String formattedDate = (gdate != null) ? gdate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
            model.addAttribute("formattedDate", formattedDate);
        }

        model.addAttribute("group", new GroupDTO());
        model.addAttribute("memberGroup", new MemberGroup());
        model.addAttribute("memberGroups", memberGroups);

        List<Integer> gnos = new ArrayList<>();
        for (MemberGroupDTO memberGroup : memberGroups) {
            gnos.add(memberGroup.getGroup().getGno());
        }


        List<MemberGroup> allMembers = memberGroupService.getMemberGroupsByGnos(gnos);

        List<MemberGroup> leaderList = new ArrayList<>();
        for (MemberGroup memberGroup : allMembers) {
            if (memberGroup.getJauth() == 1) {
                leaderList.add(memberGroup);
            }
        }

/*        if (memberCount.isEmpty()) {
            System.out.println("memberCount is empty.");
        } else {
            System.out.println("memberCount: " + memberCount);
        }*/
        model.addAttribute("allMembers", allMembers);
        model.addAttribute("leaderList", leaderList);
        model.addAttribute("memberCount", memberCount);

        return "groupList";
    }

    @PostMapping("/groupList")
    public String groupListSubmit(@ModelAttribute GroupDTO groupDTO, Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }
        groupService.createGroup(groupDTO);
        GroupDTO createdGroupDTO = groupService.getGroupById(groupDTO.getGno());
        Group group = new Group();

        group.setGno(createdGroupDTO.getGno());
        group.setGname(createdGroupDTO.getGname());
        memberGroupService.addMemberToGroup(member, group, 1);

//-----------------------------------------모임 생성 시 게시판 같이 생성(정희)------------------------------
        int gno = group.getGno();
        boardService.createBoard(gno);

        return "redirect:/groupList";
    }

    @PostMapping("/inviteMember")
    public String inviteMemberSubmit(@ModelAttribute MemberGroup memberGroup, Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        Member newMember = memberService.getMemberById(memberGroup.getMember().getMid());
        if (newMember == null) {
            model.addAttribute("error", "해당 ID의 회원은 존재하지 않습니다.");
            return bringGroupList(model, session);
        }

        GroupDTO groupDTO = groupService.getGroupById(memberGroup.getGroup().getGno());
        Group group = new Group();
        group.setGno(groupDTO.getGno());
        group.setGname(groupDTO.getGname());

        // 초대하려는 회원이 이미 모임에 존재하는지 확인
        if (memberGroupService.isMemberInGroup(newMember.getMid(), group.getGno())) {
            model.addAttribute("error", "이미 모임에 참여하고 있는 회원입니다.");
            return bringGroupList(model, session);
        }

        // 현재 로그인한 회원의 모임에서의 권한 조회
        int currentMemberJauth = memberGroupService.getMemberJauth(member.getMid(), group.getGno());

        // 초대하는 회원의 MemberGroup 객체를 데이터베이스에서 조회
        MemberGroup inviterMemberGroup = memberGroupService.getMemberGroupByGroupGnoAndMemberMid(group.getGno(), member.getMid());
        if (inviterMemberGroup == null) {
            model.addAttribute("error", "초대하는 회원의 모임 정보가 존재하지 않습니다.");
            return "groupList";
        }

        if(inviteService.checkInviteExists(newMember.getMno(), group.getGno())){
            model.addAttribute("error", "이미 초대된 회원입니다.");
            return bringGroupList(model, session);
        }

        // 초대 유형 설정
        int inviteType = (currentMemberJauth == 1) ? 6 : 0;

        // Invite 엔티티 생성 및 설정
        Invite invite = new Invite();
        invite.setMemberGroup(inviterMemberGroup); // 초대하는 회원의 정보를 jno로 설정
        invite.setMember(newMember); // 초대받는 회원의 정보를 mno로 설정
        invite.setItype(inviteType);

        // 모임장의 MemberGroup 객체를 가져와서 leaderNum을 설정
        MemberGroup groupLeaderMemberGroup = memberGroupService.getGroupLeaderMemberGroup(groupDTO.getGno());
        if (groupLeaderMemberGroup != null) {
            invite.setLeadNum(groupLeaderMemberGroup.getMember().getMno()); // 모임장의 mno를 설정
        } else {
            model.addAttribute("error", "모임장 정보를 찾을 수 없습니다.");
            return "redirect:/groupList";
        }

        // 초대 정보 저장
        inviteService.saveInvite(invite);

        // 알림 엔티티 생성 및 설정
        Alarm alarm = new Alarm();
        alarm.setLinkedPk(String.valueOf(invite.getIno())); // 초대 엔티티의 ino 값을 문자열로 설정
        alarm.setAtype(inviteType == 6 ? "모임장 초대" : "일반 회원 초대"); // 초대 유형에 따라 알림 유형 설정
        alarm.setMember(newMember); // 알림을 받을 회원
        alarm.setIsChecked(0); // 확인 여부는 0 (미확인 상태)

        // 알림 정보 저장
        alarmService.saveAlarm(alarm);

        return "redirect:/groupList";
    }

    @GetMapping("/groupManage")
    public String groupManage(Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        // 회원이 모임장인 모임이 있는지 확인
        List<Group> leaderGroups = memberGroupService.findGroupsWhereMemberIsLeader(member.getMid());
        if (leaderGroups.isEmpty()) {
            // 모임장이 아닌 경우, 오류 메시지와 함께 메인 화면으로 이동
            model.addAttribute("error", "모임장 권한이 없으므로 메인 화면으로 이동합니다.");
            return "main";
        }

        List<MemberGroup> allMemberGroups = new ArrayList<>();

        for (Group group : leaderGroups) {
            List<MemberGroup> memberGroupsForGroup = memberGroupService.findMembersByGroupGno(group.getGno());
            allMemberGroups.addAll(memberGroupsForGroup);
        }

        for(MemberGroup memberGroup : allMemberGroups){
            System.out.println(memberGroup.getMember().getMnick());
        }

        // 모임장 수락을 위해 추가한 부분(다혜)
        List<Invite> inviteList = inviteService.getInvitesByLeadNum(member.getMno());

        // 모임장 수락을 위해 추가한 부분(다혜)
        model.addAttribute("invites", inviteList);

        model.addAttribute("leaderGroups", leaderGroups);
        model.addAttribute("allMemberGroups", allMemberGroups);

        // 모임장인 경우 groupManage.jsp 페이지로 이동
        return "groupManage";
    }

    /*--------------------희진씨의 원래 모임방 이름 수정 코드(다혜)----------*/
/*    @PostMapping("/updateGroupName")
    public String updateGroupName(HttpSession session, Model model, String newGname, int gno) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }
        System.out.println("newGname = " + newGname);
        // GroupDTO에서 gno를 통해 그룹 엔티티를 조회
        GroupDTO gDTO = groupService.getGroupById(gno);
        if (gDTO != null) {
            String originalGname = gDTO.getGname();
            System.out.println("originalGname = " + originalGname);

            // 새로운 이름이 원래의 이름과 같은지 비교
            if (newGname.equals(originalGname)) {
                model.addAttribute("error", "입력한 모임명이 기존 모임명과 동일합니다.");
                return groupManage(model, session); // 수정 페이지로 이동
            }

            // GroupDTO의 newGname으로 그룹명 업데이트
            Group group = new Group();
            group.setGno(gDTO.getGno());
            group.setGname(newGname);
            group.setGdate(gDTO.getGdate());
            // 그룹 엔티티를 저장하여 업데이트 수행
            groupService.save(group);
        }

        return "redirect:/groupManage";
    }*/

    @PostMapping("/deleteMemberToGroup")
    public String deleteMemberToGroup(@ModelAttribute("gno") int gno,
                               @ModelAttribute("mid") String memberId,
                               HttpSession session,
                               Model model) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        // 모임장만 특정 그룹에서 회원을 삭제할 수 있습니다.
        if (!memberGroupService.isMemberInGroup(memberId, gno)) {
            model.addAttribute("errorMessage", "입력한 회원은 해당 모임에 없습니다.");
            return groupManage(model, session);
        }

        MemberGroup deleteMg = memberGroupService.getMemberGroupByGroupGnoAndMemberMid(gno, memberId);
        memberGroupService.removeMemberGroup(deleteMg);

        return "redirect:/groupManage";
    }

    @PostMapping("/addMemberToGroup")
    public String addMemberToGroup(int gno,
                                   String mid,
                                   HttpSession session,
                                   Model model) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        // 입력된 회원 ID와 그룹 번호를 이용해 추가하려는 회원과 그룹을 조회
        Member newMember = memberService.getMemberById(mid);
        if (newMember == null) {
            model.addAttribute("errorMessage2", "해당 ID의 회원은 존재하지 않습니다.");
            return groupManage(model, session);
        }

        // 추가하려는 회원이 이미 모임에 존재하는지 확인
        if (memberGroupService.isMemberInGroup(mid, gno)) {
            model.addAttribute("errorMessage2", "이미 모임에 참여하고 있는 회원입니다.");
            return groupManage(model, session);
        }

        // GroupDTO를 통해 그룹 정보를 가져오고, Group 객체를 생성
        GroupDTO groupDTO = groupService.getGroupById(gno);
        Group group = new Group();
        group.setGno(groupDTO.getGno());
        group.setGname(groupDTO.getGname());

        // 회원을 그룹에 추가
        memberGroupService.addMemberToGroup(newMember, group, 0);

        return "redirect:/groupManage";
    }

    @PostMapping("/leaveGroup")
    public String leaveGroup(int gno, HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        System.out.println("Attempting to leave group: " + gno);
        System.out.println("Member ID: " + member.getMid());

        MemberGroup memberGroup = memberGroupService.getMemberGroupByGroupGnoAndMemberMid(gno, member.getMid());

        // 현재 모임장 권한을 가진 사용자가 모임을 탈퇴하려는 경우
        if (memberGroup.getJauth() == 1) {
            // 모임에 참여하는 회원 수를 가져옴
            List<MemberGroup> membersInGroup = memberGroupService.findMembersByGroupGno(gno);

            // 모임에 참여하는 회원이 모임장 한 명만 있는 경우
            if (membersInGroup.size() == 1) {
                // 모임을 삭제
                groupService.deleteGroupByGno(gno);
            } else {
                // 일반 회원의 경우 모임에서 삭제
                memberGroupService.removeMemberGroup(memberGroup);
            }
        } else {
            // 일반 회원의 경우 모임에서 삭제
            memberGroupService.removeMemberGroup(memberGroup);
        }

        return "redirect:/groupList";
    }


    @GetMapping("/transferJauth")
    public String showTransferJauth(@RequestParam("gno") int gno, Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        // gno와 현재 로그인된 멤버의 ID를 사용하여 해당 MemberGroup 객체를 가져옴
        MemberGroup memberGroup = memberGroupService.getMemberGroupByGroupGnoAndMemberMid(gno, member.getMid());
        if (memberGroup == null) {
            model.addAttribute("error", "해당 그룹을 찾을 수 없습니다.");
            return "redirect:/groupList";
        }

        // 현재 모임장의 ID를 제외한 일반 회원 목록을 가져옴
        List<MemberGroupDTO> memberGroups = memberGroupService.findMembersByGroupGnoWithDTO(memberGroup.getGroup().getGno());
        List<MemberGroupDTO> regularMembers = new ArrayList<>();
        for (MemberGroupDTO mg : memberGroups) {
            if (mg.getJauth() == 0 ) {
                regularMembers.add(mg);
                System.out.println(mg.getMemberNick());
            }
        }

        model.addAttribute("memberGroup", memberGroup);
        model.addAttribute("regularMembers", regularMembers);

        return "transferJauth";
    }

    @PostMapping("/transferJauth")
    @ResponseBody
    public Map<String, String> transferJauth(@RequestParam("gno") int gno,
                                @RequestParam("memberNick") String newLeaderNick,
                                HttpSession session, Model model) {
        Map<String, String> response = new HashMap<>();
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            response.put("status", "error");
            response.put("message", "로그인된 회원 정보가 없습니다.");
            return response;
        }

        // 현재 모임장 확인
        MemberGroup currentLeaderGroup = memberGroupService.getMemberGroupByGroupGnoAndMemberMid(gno, member.getMid());

        // 새로운 모임장으로 설정할 회원 찾기
        MemberGroup newLeaderGroup = memberGroupService.findMemberGroupByGroupGnoAndNick(gno, newLeaderNick);

        // 권한 위임 및 기존 모임장 권한 변경
        memberGroupService.updateMemberGroupJauth(gno, newLeaderGroup.getMember().getMid(), 1);
        memberGroupService.updateMemberGroupJauth(gno, member.getMid(), 0);

        // 기존 모임장 그룹에서 제거
        memberGroupService.removeMemberGroup(currentLeaderGroup);

        response.put("status", "success");
        response.put("message", "모임장 권한 위임이 성공했습니다.");
        return response;
    }

    @PostMapping("/deleteGroup")
    public String deleteGroup(@RequestParam("gno") int gno, HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        // 그룹 삭제
        groupService.deleteGroupByGno(gno);

        return "redirect:/groupManage";
    }



   /* -------------그룹 프로필을 위해 추가한 함수(다혜)----------*/

    @GetMapping("/editGroup")
    public String showEditForm(Model model, @RequestParam("gno") int gno) {
        Group group = groupService.findGroupByGno(gno);
        model.addAttribute("group", group);
        return "editGroup";
    }

    @PostMapping("/editGroup")
    @ResponseBody
    public String updateGroup(@RequestParam("gno") int gno, @RequestParam("gname") String gname, @RequestParam("gimageFile") MultipartFile gimageFile) throws IOException {
        Group originalGroup = groupService.findGroupByGno(gno);
        originalGroup.setGname(gname);

        if (!gimageFile.isEmpty()) {
            String fileName = gimageFile.getOriginalFilename();
            String uploadDir = servletContext.getRealPath("/resources/images/");
            String filePath = Paths.get(uploadDir, fileName).toString();

            gimageFile.transferTo(new File(filePath));

            String fileUrl = "/resources/images/" + fileName;
            originalGroup.setGimage(fileUrl);
        }

        groupService.save(originalGroup);

        return "redirect:/groupManage";
    }
}