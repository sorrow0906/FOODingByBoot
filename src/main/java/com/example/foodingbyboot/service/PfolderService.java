package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Pfolder;
import com.example.foodingbyboot.repository.MemberRepository;
import com.example.foodingbyboot.repository.PfolderRepository;
import com.example.foodingbyboot.repository.PickRepository;
import com.example.foodingbyboot.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PfolderService {

    @Autowired
    private PickRepository pickRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    public PfolderRepository pfolderRepository;

    public List<Pfolder> getPfoldersByMno(int mno) {
        return pfolderRepository.findByMemberMno(mno);
    }

    public void savePfolder(Pfolder pfolder) {
        pfolderRepository.save(pfolder);
    }

    public void deletePfolderByPfno(int pfno) {
        pfolderRepository.deleteById(pfno);
    }

    public Pfolder findByPfno(int pfno) {
        return pfolderRepository.findByPfno(pfno).orElse(null);
    }

    public List<Pfolder> findPfoldersByPfnos(String pfnos) {
        String[] pfnoArray = pfnos.split(",");
        List<Integer> pfnoList = Arrays.stream(pfnoArray).map(Integer::parseInt).collect(Collectors.toList());
        return pfolderRepository.findByPfno(pfnoList);
    }
}
