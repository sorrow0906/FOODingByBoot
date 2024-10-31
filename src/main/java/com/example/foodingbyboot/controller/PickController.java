package com.example.foodingbyboot.controller;

import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.entity.Pfolder;
import com.example.foodingbyboot.entity.Pick;
import com.example.foodingbyboot.entity.Store;
import com.example.foodingbyboot.service.PfolderService;
import com.example.foodingbyboot.service.PickService;
import com.example.foodingbyboot.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

@Controller
public class PickController {

    @Autowired
    private PickService pickService;

    @Autowired
    private PfolderService pfolderService;

    @Autowired
    private StoreService storeService;

    @GetMapping("/pickList")
    public String pickList(HttpSession session, Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }

        int pfno = 1;
        List<Pick> pickList = pickService.getPicksByPfnoAndMno(pfno, loggedInMember.getMno());
        List<Pfolder> pfolderList = pfolderService.getPfoldersByMno(loggedInMember.getMno());

        model.addAttribute("pickList", pickList);
        model.addAttribute("pfolderList", pfolderList);

        return "pickPage";
    }


    @PostMapping("/pick")
    @ResponseBody
    public String pickStore(@RequestParam("sno") int sno, @RequestParam(value = "pfno", defaultValue = "1") int pfno, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "error"; // 로그인되지 않은 상태에서 예외 처리
        }

        int mno = loggedInMember.getMno();
        boolean isPicked = pickService.togglePick(mno, sno, pfno);
        return isPicked ? "picked" : "unpicked";
    }

    @PostMapping("/checkPick")
    @ResponseBody
    public String checkPick(@RequestParam("sno") int sno, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "unpicked"; // 로그인되지 않은 상태에서 예외 처리
        }

        int mno = loggedInMember.getMno();
        boolean isPicked = pickService.isPicked(mno, sno);
        return isPicked ? "picked" : "unpicked";
    }

    // 전체 가게 목록에서 찜 삭제 (pfno == 1)
    @PostMapping("/removePick")
    @ResponseBody
    public String removePick(@RequestParam("snos") List<Integer> snos) {
        for (int sno : snos) {
            pickService.removePicksBySno(sno);
        }
        return "success";
    }

    // 찜 폴더 내부에서 가게 삭제
    @PostMapping("deletePickFromFolder")
    @ResponseBody
    public String deletePickFromFolder(@RequestParam("snos") List<Integer> snos, @RequestParam("pfno") Integer pfno) {
        for (int sno : snos) {
            pickService.removePicksByPfolderAndSno(pfno, sno);
        }
        return "success";
    }

    @PostMapping("/createFolder")
    public String createFolder(@RequestParam("pfname") String pfname, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");


        Pfolder pfolder = new Pfolder();
        pfolder.setPfname(pfname);
        pfolder.setMember(loggedInMember);

        pfolderService.savePfolder(pfolder);

        return "redirect:/pickList";
    }

    @PostMapping("/deleteFolder")
    public String deleteFolder(@RequestParam(value = "selectedFolders", required = false) List<Integer> selectedFolders, HttpSession session) {
        if (selectedFolders != null && !selectedFolders.isEmpty()) {
            for (Integer pfno : selectedFolders) {
                pfolderService.deletePfolderByPfno(pfno);
            }
        }
        return "redirect:/pickList";
    }

    @PostMapping("/updateFolderName")
    @ResponseBody
    public String updateFolderName(@RequestParam("pfno") Integer pfno, @RequestParam("pfname") String pfname, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        Pfolder pfolder = pfolderService.findByPfno(pfno);

        pfolder.setPfname(pfname);
        pfolderService.savePfolder(pfolder);

        return "success";
    }

    @PostMapping("/addPickToFolder")
    @ResponseBody
    public String addPickToFolder(@RequestParam("pfnos") String pfnos, @RequestParam("snos") String snos, HttpSession httpSession) {
        Member loggedInMember = (Member) httpSession.getAttribute("loggedInMember");

        try {
            List<Pfolder> pfolders = pfolderService.findPfoldersByPfnos(pfnos);
            List<Store> stores = storeService.findStoresBySnos(snos);

            for (Pfolder pfolder : pfolders) {
                for (Store store : stores) {
                    Pick newPick = new Pick();
                    newPick.setPfolder(pfolder);
                    newPick.setStore(store);
                    newPick.setMember(loggedInMember);

                    pickService.savePick(newPick);
                }
            }
        } catch (Exception e) {
            return "error";
        }
        return "success";
    }

    @GetMapping("/getFolderContent")
    @ResponseBody
    public List<Pick> getFolderContent(@RequestParam("pfno") Integer pfno, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        Pfolder pfolder = pfolderService.findByPfno(pfno);

        List<Pick> picks = pickService.getPicksByPfolder(pfolder);
        if (picks  == null || picks.isEmpty()) {
            return Collections.emptyList();  // 비어있는 리스트 반환
        }
        for (Pick pick : picks) {
            pick.getPfolder().getMember().setMemberGroupList(null);
        }

        return picks;
    }

    @GetMapping("/folder/{pfno}")
    public String viewFolderContent(@PathVariable("pfno") Integer pfno, Model model, HttpSession session) {
        Pfolder pfolder = pfolderService.findByPfno(pfno);

        List<Pick> picks = pickService.getPicksByPfolder(pfolder);
        model.addAttribute("pfolder", pfolder);
        model.addAttribute("picks", picks);

        return "folderContent"; // 새로운 폴더 내용을 보여줄 JSP 페이지 이름
    }
}
