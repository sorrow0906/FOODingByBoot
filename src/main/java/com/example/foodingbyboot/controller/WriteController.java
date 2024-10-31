package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.entity.Write;
import com.example.foodingbyboot.service.BoardService;
import com.example.foodingbyboot.service.WriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;

@Controller
public class WriteController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private WriteService writeService;

    @GetMapping("/write")
    public String showWriteForm(@RequestParam("bno") int bno, HttpSession session, Model model){
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if(loggedInMember == null){
            return "redirect:/login";
        }else{

            model.addAttribute("bno", bno);
            model.addAttribute("member", loggedInMember.getMnick());
            model.addAttribute("write", new Write());
            return "write";
        }
    }

    @PostMapping("/submitWrite")
    public String submitWrite(@ModelAttribute Write write,HttpSession session, BindingResult result, Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if (result.hasErrors()) {
            return "write";
        }

        write.setMember(loggedInMember);
        writeService.saveWrite(write);
        model.addAttribute("message", "글쓰기 성공!");

        return "redirect:/board?gno="+boardService.getGnoByBno(write.getBoard().getBno());
    }

    @GetMapping("/read")
    public String showReadWrite(@RequestParam("wno") int wno, HttpSession session, Model model){
        Write write = writeService.findByWno(wno);
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        Integer gno = (Integer) session.getAttribute("gno");
        if (write != null) {
            model.addAttribute("gno", gno);

            model.addAttribute("write", write);
            boolean canEdit = loggedInMember != null && loggedInMember.getMno() == write.getMember().getMno();
            model.addAttribute("canEdit", canEdit);
         } else {
             model.addAttribute("error", "없는 정보입니다.");
         }
         return "read";

    }
    @GetMapping("/writeUpdate")
    public String showUpdateWrite(@RequestParam("wno") int wno, HttpSession session, Model model){
        Write write = writeService.findByWno(wno);
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        Integer gno = (Integer) session.getAttribute("gno");
        if (write != null) {
            model.addAttribute("gno", gno);
            model.addAttribute("member", loggedInMember.getMnick());
            model.addAttribute("write", write);
            return "writeUpdate";
        }else{
            return "redirect:/read";
        }

    }

    @PostMapping("/writeUpdate")
    public String writeUpdate(@RequestParam("wno") int wno, @ModelAttribute Write write, HttpSession session, BindingResult result, Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (result.hasErrors()) {
            return "writeUpdate";
        }
        try {
            Write originalWrite = writeService.findByWno(wno);
            originalWrite.setWtitle(write.getWtitle());
            originalWrite.setWcontent(write.getWcontent());
            model.addAttribute("member", loggedInMember.getMnick());
            writeService.updateWrite(originalWrite);
            return "redirect:/read?wno=" + wno;
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "해당 글을 찾을 수 없습니다.");
            return "errorPage";
        }
    }

    @PostMapping("/writeDelete")
    public String writeDelete(@RequestParam("wno") int wno, HttpSession session, Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

         if (loggedInMember != null) {
             Write write = writeService.findByWno(wno);
             if (write != null) {
                 if (write.getMember().getMno() == loggedInMember.getMno()) {
                     writeService.deleteWrite(wno);
                     return "redirect:/board?gno=" + boardService.getGnoByBno(write.getBoard().getBno());
                 } else {
                     model.addAttribute("error", "글쓴이 정보가 일치하지 않습니다.");
                     return "redirect:/read?wno=" + wno;
                 }
             } else {
                 model.addAttribute("error", "작성된 글 정보가 없습니다.");
                 return "redirect:/read?wno=" + wno;
             }
         }else{
             model.addAttribute("error", "로그인된 정보가 업습니다.");
             return "redirect:/read?wno=" + wno;
         }
    }
}
