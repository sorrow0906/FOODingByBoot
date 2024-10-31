package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.entity.Pfolder;
import com.example.foodingbyboot.entity.Pick;
import com.example.foodingbyboot.entity.Store;
import com.example.foodingbyboot.repository.MemberRepository;
import com.example.foodingbyboot.repository.PfolderRepository;
import com.example.foodingbyboot.repository.PickRepository;
import com.example.foodingbyboot.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class PickService {

    @Autowired
    private PickRepository pickRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    public PfolderRepository pfolderRepository;

    @Autowired
    private StoreService storeService;


    public boolean togglePick(int mno, int sno, int pfno) {
        Member member = memberRepository.findByMno(mno).orElseThrow(() -> new RuntimeException("로그인 정보를 불러오는 데 실패했습니다."));;
        Store store = storeRepository.findBySno(sno).orElseThrow(() -> new RuntimeException("가게 정보를 불러오는 데 실패했습니다."));;
        Pfolder pfolder = pfolderRepository.findByPfno(pfno).orElseThrow(() -> new RuntimeException("폴더 정보를 불러오는 데 실패했습니다."));;

        Pick existingPick = pickRepository.findByMemberAndStore(member, store);
        if (existingPick != null) {
            pickRepository.delete(existingPick);

            // 가게 Pick수 계산을 위해 추가(다혜)
            storeService.updateStoreInCache(existingPick.getStore().getSno());
            return false;
/*            try {
                pickRepository.updatePfnoToNull(existingPick.getPno());

                pickRepository.delete(existingPick);
            } catch (DataIntegrityViolationException e) {
                throw new RuntimeException("Error while deleting the pick due to foreign key constraint", e);
            } catch (Exception e) {
                throw new RuntimeException("Error while deleting the pick", e);
            }
            return false;*/
        } else {
            Pick newPick = new Pick(member, store, pfolder);
            pickRepository.save(newPick);

            // 가게 Pick수 계산을 위해 추가(다혜)
            storeService.updateStoreInCache(newPick.getStore().getSno());
            return true;
        }

    }

    public boolean isPicked(int mno, int sno) {
        Member member = memberRepository.findByMno(mno).orElseThrow(() -> new RuntimeException("로그인 정보를 불러오는 데 실패했습니다."));;
        Store store = storeRepository.findBySno(sno).orElseThrow(() -> new RuntimeException("가게 정보를 불러오는 데 실패했습니다."));;

        Pick existingPick = pickRepository.findByMemberAndStore(member, store);
        return existingPick != null;
    }

    public List<Pick> getPicksByMno(int mno) {
        return pickRepository.findByMemberMno(mno);
    }

    public List<Pick> getPicksByPfolder(Pfolder pfolder) {
        return pickRepository.findByPfolder(pfolder);
    }

    public Pick findPickByMemberAndStore(Member member, Store store) {
        return pickRepository.findByMemberAndStore(member, store);
    }



    @Transactional
    public void savePick(Pick pick) {
        List<Pick> existingPicks = pickRepository.findByPfolder_PfnoAndMember_MnoAndStore_Sno(
                pick.getPfolder().getPfno(), pick.getMember().getMno(), pick.getStore().getSno());
        if (existingPicks.isEmpty()) {
            pickRepository.save(pick);
        }
    }

    public List<Pick> getPicksByPfnoAndMno(int pfno, int mno) {
        return pickRepository.findByPfolder_PfnoAndMember_Mno(pfno, mno);
    }

    @Transactional
    public void removePicksBySno(int sno) {
        pickRepository.removeByStore_Sno(sno);
    }

    @Transactional
    public void removePicksByPfolderAndSno(Integer pfno, Integer sno) {
        pickRepository.deleteByPfolder_PfnoAndStore_Sno(pfno, sno);
    }
}
