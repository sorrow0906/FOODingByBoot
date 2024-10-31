package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.entity.Business;
import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.service.ApiService;
import com.example.foodingbyboot.service.MemberGroupService;
import com.example.foodingbyboot.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    // 모임 기능을 위해 추가 (희진)
    @Autowired
    private MemberGroupService memberGroupService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ServletContext servletContext;

    @GetMapping("/registerSelect")
    public String selectRegister() {
        return "registerSelect";
    }

    @GetMapping("/register/user")
    public String showUserForm(Model model) {
        Member member = new Member();
        member.setMtype(0); // 손님 (일반회원)으로 설정
        model.addAttribute("member", member);
        model.addAttribute("memberType", "손님");
        return "registerUser";
    }

    @GetMapping("/register/owner")
    public String showOwnerForm(Model model) {
        Member member = new Member();
        member.setMtype(1); // 사장님으로 설정
        model.addAttribute("member", member);
        model.addAttribute("memberType", "사장님");

        return "registerOwner";
    }

    @PostMapping("/register/user")
    public String registerUser(@Valid @ModelAttribute("member") Member member, BindingResult bindingResult, Model model) {
        // 생년월일 형식 검증
        try {
            if (member.getMbirth() != null) {
                LocalDate.parse(member.getMbirth().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (DateTimeParseException e) {
            bindingResult.rejectValue("mbirth", "error.member", "생년월일이 올바른 형식이 아닙니다. (예: YYYYMMDD)");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("memberType", "손님");
            return "registerUser";
        }

        // 아이디 중복 체크
        if (memberService.isMidExists(member.getMid())) {
            bindingResult.rejectValue("mid", "error.member", "이미 사용 중인 아이디입니다.");
            model.addAttribute("memberType", "손님");
            return "registerUser";
        }

        // 닉네임 중복 체크
        if (memberService.isMnickExists(member.getMnick())) {
            bindingResult.rejectValue("mnick", "error.member", "이미 사용 중인 닉네임입니다.");
            model.addAttribute("memberType", "손님");
            return "registerUser";
        }
        // 비밀번호 해시화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(member.getMpass());
        member.setMpass(encodedPassword);

        memberService.saveMember(member);
        model.addAttribute("message", "일반 회원 가입 성공! 환영합니다!");
        return "login";
    }

    @PostMapping("/register/owner")
    public String registerOwner(@Valid @ModelAttribute("member") Member member, BindingResult bindingResult, Model model) {
        // 생년월일 형식 검증
        try {
            if (member.getMbirth() != null) {
                LocalDate.parse(member.getMbirth().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (DateTimeParseException e) {
            bindingResult.rejectValue("mbirth", "error.member", "생년월일이 올바른 형식이 아닙니다. (예: YYYYMMDD)");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("memberType", "사장님");
            return "registerOwner";
        }

        // 아이디 중복 체크
        if (memberService.isMidExists(member.getMid())) {
            bindingResult.rejectValue("mid", "error.member", "이미 사용 중인 아이디입니다.");
            model.addAttribute("memberType", "사장님");
            return "registerOwner";
        }

        // 닉네임 중복 체크
        if (memberService.isMnickExists(member.getMnick())) {
            bindingResult.rejectValue("mnick", "error.member", "이미 사용 중인 닉네임입니다.");
            model.addAttribute("memberType", "사장님");
            return "registerOwner";
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(member.getMpass());
        member.setMpass(encodedPassword);

        memberService.saveMember(member);
        model.addAttribute("message", "사장님 회원 가입 성공! 환영합니다!");
        return "login";
    }

    // 사업자등록조회
    @GetMapping("/checkBusiness")
    public String showForm(Model model) {
        model.addAttribute("business", new Business());
        return "checkOwner";
    }

    @PostMapping("/checkBusiness")
    public String checkBusiness(@ModelAttribute("business") Business business, Model model) {
        if (business.getB_no() == null || business.getB_no().isEmpty() ||
                business.getStart_dt() == null || business.getStart_dt().isEmpty() ||
                business.getP_nm() == null || business.getP_nm().isEmpty()) {
            model.addAttribute("message", "모든 필드를 입력해 주세요.");
            model.addAttribute("messageType", "error");
            return "checkOwner";
        }

        String result = apiService.checkBusiness(business);
        if (result.contains("\"valid\":\"01\"")) {
            model.addAttribute("message", "인증되었습니다.");
            model.addAttribute("messageType", "success");
            model.addAttribute("disabled", true);
        } else {
            model.addAttribute("message", "없는 정보입니다. 다시 입력해 주세요.");
            model.addAttribute("messageType", "error");
            model.addAttribute("disabled", true);
        }

        return "checkOwner";
    }

    // 회원 정보 조회
    @GetMapping("/member/view")
    public String viewMember(HttpSession session, Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if (loggedInMember == null) {
            model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
            return "errorPage";
        } else {
            if (!memberService.isMnoExists(loggedInMember.getMno())) {
                model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
                return "errorPage";
            }else {
                Member dbMember = memberService.findMemberByMno(loggedInMember.getMno());
                model.addAttribute("member", dbMember);
                return "viewMember";
            }
        }
    }

    // 회원 정보 수정 폼 보여주기
    @GetMapping("/member/edit")
    public String showEditForm(Model model, HttpSession session) {

        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if (loggedInMember == null) {
            return "redirect:/member/view";

        } else {

            //프로필 파일을 표시하기 위해 추가&수정(다혜)
            Member viewMember = memberService.findMemberByMno(loggedInMember.getMno());
            viewMember.setMpass("");
            model.addAttribute("member", viewMember);

            return "editMember"; // 수정 폼으로 이동
        }
    }

    @PostMapping("/member/edit")
    public String updateMember(@ModelAttribute("member") @Valid Member updatedMember,
                               BindingResult bindingResult, Model model, /*프로필 업로드를 위해 추가(다혜)*/ @RequestParam("mimageFile") MultipartFile mimageFile){
        if (bindingResult.hasErrors()) {
            return "editMember"; // 입력 폼으로 다시 이동
        }
        Member existingMemberOpt = memberService.findMemberById(updatedMember.getMid());
        if (existingMemberOpt == null) {
            model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
            return "main";
        } else {
            Member existingMember = existingMemberOpt;

            // 기존 정보를 새로 입력된 정보로 업데이트
//            existingMember.setMpass(updatedMember.getMpass());
            existingMember.setMnick(updatedMember.getMnick());
            existingMember.setMbirth(updatedMember.getMbirth());
            existingMember.setMphone(updatedMember.getMphone());
            existingMember.setMemail(updatedMember.getMemail());
            existingMember.setMaddr(updatedMember.getMaddr());


            /*------------ 프로필 파일을 업로드하기 위해 추가(다혜)-----------*/
            if (!mimageFile.isEmpty()) {
                try {
                    String fileName = mimageFile.getOriginalFilename();
                    String uploadDir = servletContext.getRealPath("/resources/images/");
                    System.out.println("File saved at: " + uploadDir);
                    String filePath = Paths.get(uploadDir, fileName).toString();

                    mimageFile.transferTo(new File(filePath));

                    String fileUrl = "/resources/images/" + fileName;
                    existingMember.setMimage(fileUrl);  // String 형태로 파일 경로를 설정
                }catch (IOException e) {
                    e.printStackTrace();
                    return "redirect:/errorPage";
                }
            }
            /*------------ ------------------------------------------------*/


            memberService.updateMember(existingMember); // 회원 정보 업데이트
            model.addAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
            return "redirect:/member/view";
        }
    }

    @GetMapping("/editChangePass")
    public String showEditPassForm(Model model, HttpSession session) {

        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if (loggedInMember == null) {
            return "editMember";

        } else {
            loggedInMember.setMpass(null);
            model.addAttribute("member", loggedInMember);
            return "editChangePass"; // 수정 폼으로 이동
        }
    }
    @PostMapping("/editChangePassSave")
    public String updatePassword(@RequestParam("currentPassword") String currentPassword, @ModelAttribute("member") @Valid Member updatedMember,
                               Model model, HttpSession session) {

        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if (loggedInMember == null){
            model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
            return "editChangePass";
        }

        Member nowMember = memberService.findMemberById(loggedInMember.getMid());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(currentPassword, nowMember.getMpass())) {
            model.addAttribute("error", "현재 비밀번호가 올바르지 않습니다.");

            return "editChangePass";
        }


        loggedInMember.setMpass(passwordEncoder.encode(updatedMember.getMpass()));

        memberService.updateMember(loggedInMember); // 회원 정보 업데이트
        model.addAttribute("message", "비밀번호가 성공적으로 수정되었습니다.");
        return "redirect:/member/edit";

    }

    // 마이페이지
    @GetMapping("/myPage")
    public String showMyPage(HttpSession session, Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            // 로그인되지 않은 상태에서 접근 시 예외 처리 또는 로그인 페이지로 리다이렉트
            return "redirect:/login"; // 예시로 로그인 페이지로 리다이렉트 설정
        }
        model.addAttribute("loggedInMember", loggedInMember);
        return "myPage"; // 마이페이지 JSP 파일명
    }

    // 회원 탈퇴
    @PostMapping("/delete{mno}")
    public String deleteMember(HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember != null) {
            int mno = loggedInMember.getMno();

            // 1. 탈퇴할 회원 찾기 (희진 추가)
            Member leavingMember = memberService.findMemberByMno(mno);
            if (leavingMember != null) {
                // 2. 모임장 권한 위임 (희진 추가)
                memberGroupService.delegateGroupLeadership(leavingMember);
            }
            memberService.deleteMemberByMno(mno);
            session.invalidate(); // 세션 무효화
            return "redirect:/deleteSuccess";
        } else {
            return "redirect:/login"; // 로그인 정보가 없을 경우 로그인 페이지로 리디렉션
        }
    }

    @GetMapping("/deleteSuccess")
    public String showDeleteSuccess(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("loggedInMember");
            session.invalidate(); // 세션 무효화
        }
        return "deleteSuccess";
    }

    // ID 찾기
    @GetMapping("/findID")
    public String showFindId() {
        return "findID";
    }

    @PostMapping("/findID")
    public String findId(@RequestParam("mname") String mname, @RequestParam("memail") String memail, @RequestParam("mphone") String mphone, Model model) {
        String mid = memberService.findIdByMnameEmailAndPhone(mname, memail, mphone);

        if (mid != null) {
            model.addAttribute("mnamemessage", mname + "님의 ID는 ");
            model.addAttribute("IDmessage", mid);
            model.addAttribute("message", "입니다.");
        } else {
            model.addAttribute("message", "가입된 정보가 없습니다.");
        }

        return "findIDResult";
    }

    //비밀번호 찾기
    @GetMapping("/findPass_IdAuth")
    public String showFindPass() {
        return "findPass_IdAuth";
    }

    @PostMapping("/findPass_IdAuth")
    public String findPassIDAuth(@RequestParam("mid") String mid, Model model) {
        if(memberService.isMidExists(mid)){
            model.addAttribute("mid", mid);

            return "findPassAuth";
        }
        else{
            model.addAttribute("message", "존재하지않는 ID 입니다.");
            return "findPass_IdAuth";
        }
    }
    @RequestMapping("/findPassEmail")
    public String findPassEmail(@RequestParam("mid") String mid, @RequestParam("mname") String mname, @RequestParam("memail") String memail, Member member, Model model) {
        Map<String, Object> map = new HashMap<>();

        // 사용자가 작성한 아이디를 기준으로 존재하는 사용자인지 확인한다.
        Optional<Member> isUserOptional = memberService.findByMember(member.getMid(), member.getMname(), member.getMemail());

        if (isUserOptional.isPresent()) { // 회원가입이 되어있는, 존재하는 사용자라면
            Member isUser = isUserOptional.get();
            Random r = new Random();
            int num = r.nextInt(999999); // 랜덤 난수

            if (isUser.getMemail().equals(member.getMemail())) { // 이메일 정보 또한 동일하다면
                String setFrom = "fooding2441@gmail.com"; // 발신자 이메일
                String tomail = isUser.getMemail(); // 수신자 이메일
                String title = "[FOODing] 비밀번호 변경 인증 이메일입니다.";
                String content = String.format("안녕하세요 %s님\nFOODing 비밀번호 찾기(변경) 인증번호는 %d입니다. \n 로그인 후 반드시 비밀번호를 변경하세요!", isUser.getMnick(), num);

                member.setMpass(num+"");
                try {
                    MimeMessage msg = mailSender.createMimeMessage();
                    MimeMessageHelper msgHelper = new MimeMessageHelper(msg, true, "utf-8");

                    msgHelper.setFrom(setFrom);
                    msgHelper.setTo(tomail);
                    msgHelper.setSubject(title);
                    msgHelper.setText(content);

                    // 메일 전송
                    mailSender.send(msg);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                // 성공적으로 메일을 보낸 경우
                map.put("status", true);
                map.put("num", num);
                map.put("mid", mid);
                map.put("mname", mname);
                map.put("memail", memail);
                model.addAllAttributes(map);
                model.addAttribute("num", num);
                model.addAttribute("mno", isUser.getMno());
                model.addAttribute("message", "이메일 전송이 완료되었습니다");

                System.out.println("num값 : " + num);
                return "findPassAuth";
            }
        }

        // 실패한 경우
        map.put("status", false);
        model.addAllAttributes(map);
        model.addAttribute("message", "이메일 전송이 실패하였습니다");
        return "findPassAuth";
    }

    @PostMapping("/findPassAuth")
    public String findPassAuth(@RequestParam("mno") int mno, @RequestParam("auth") String auth, @RequestParam("num") int num, Model model, HttpSession session) {

        if (Integer.parseInt(auth) == num) {
            model.addAttribute("mno", mno);
            model.addAttribute("messageAuth", "인증에 성공했습니다.");
            Member member = memberService.findMemberByMno(mno);
            member.setMpass("");
            model.addAttribute("member", member);
            return "changePass"; // 성공 페이지로 이동
        } else {
            model.addAttribute("messageAuth", "인증번호가 일치하지 않습니다.");
            model.addAttribute("num", num); // 다시 인증번호를 전달
            return "findPassAuth";
        }
    }
    // 비밀번호 변경
    @GetMapping("/changePass")
    public String showChangePass() {
        return "changePass";
    }

    @PostMapping("/changePassSave")
    public String changePass(@RequestParam("mno") int mno, @RequestParam("mpass") String newPass, Model model) {

        Member member = memberService.findMemberByMno(mno);
        if (member == null) {
            model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
            return "changePass";
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPass);
        member.setMpass(encodedPassword);

        memberService.updateMember(member);
        model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
        return "redirect:/login";
    }
}
