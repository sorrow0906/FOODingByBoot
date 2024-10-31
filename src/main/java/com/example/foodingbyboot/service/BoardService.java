package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Board;
import com.example.foodingbyboot.entity.Group;
import com.example.foodingbyboot.repository.BoardRepository;
import com.example.foodingbyboot.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private GroupRepository groupRepository;

    public List<Board> getBoardByGroupGno(int gno) {return boardRepository.findByGroupGno(gno); }


    public void createBoard(int gno) {
        Board board = new Board();

        Optional<Group> groupOPT = groupRepository.findByGno(gno);

        if(groupOPT.isPresent()) {
            Group group = groupOPT.get();
            board.setBname(group.getGname() + " 모임의 게시판");
            board.setGroup(group);
            board.setBtype(1);
        }else{
            throw new RuntimeException("Group with gno " + gno + " not found.");
        }
        boardRepository.save(board);

    }

    public Board getBoardByBno(int bno) { return boardRepository.findByBno(bno);
    }

    // bno에 대한  gno를 반환받기 위해 작성(다혜)
    public int getGnoByBno(int bno) {
        return boardRepository.findGnoByBno(bno);
    }
}
