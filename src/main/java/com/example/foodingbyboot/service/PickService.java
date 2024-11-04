package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.entity.Pfolder;
import com.example.foodingbyboot.entity.Pick;
import com.example.foodingbyboot.entity.Store;
import com.example.foodingbyboot.repository.MemberRepository;
import com.example.foodingbyboot.repository.PfolderRepository;
import com.example.foodingbyboot.repository.PickRepository;
import com.example.foodingbyboot.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PickService {
    private final PickRepository pickRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final PfolderRepository pfolderRepository;
    private final StoreService storeService;

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
}
