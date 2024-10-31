package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Write;
import com.example.foodingbyboot.repository.WriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class WriteService {
    @Autowired
    private WriteRepository writeRepository;


    public Write saveWrite(Write write) {
        write.setWdate(LocalDateTime.now());
        return writeRepository.save(write);
    }

    public List<Write> getWritesByBoardBnoWithPagination(int bno, int page, int size) {

        // 시간순으로 정렬 가능하도록 레파지토리 단계에서는 전체 게시글을 가져오도록 수정(다혜)
        List<Write> writeList = writeRepository.findByBoardBno(bno);
        writeList.sort(Comparator.comparing(Write::getWdate).reversed());
        int start = (page - 1) * size;
        int end = Math.min(start + size, writeList.size());

        // 페이지 범위 내의 리스트만 추출
        List<Write> paginatedWriteList = writeList.subList(start, end);

        // 날짜 포맷팅
        for (Write write : paginatedWriteList) {
            write.setDateToString(write.getWdate().format(DateTimeFormatter.ofPattern("yy-MM-dd")));
        }

        return paginatedWriteList;
    }

    public int countWritesByBoardBno(int bno) {
        return writeRepository.countByBoardBno(bno);
    }

    public Write findByWno(int wno) {
        Write write = (Write) writeRepository.findByWno(wno).orElse(null);
        if (write != null) {
            write.setDateToString(write.getWdate().format(DateTimeFormatter.ofPattern("yy-MM-dd")));
        }
        return write;
    }

    public Write updateWrite(Write write) {

        return writeRepository.save(write);
    }

    public void deleteWrite(int wno) {
        writeRepository.deleteById(wno);
    }
}


