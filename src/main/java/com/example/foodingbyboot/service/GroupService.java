package com.example.foodingbyboot.service;

import com.example.foodingbyboot.dto.GroupDTO;
import com.example.foodingbyboot.entity.Group;
import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.repository.GroupRepository;
import com.example.foodingbyboot.repository.MemberGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberGroupRepository memberGroupRepository;

    @Transactional
    public void save(Group group) {
        groupRepository.save(group);
    }

    public List<GroupDTO> getAllGroups() {
        List<Group> groups = groupRepository.findAll();
        List<GroupDTO> groupDTOs = new ArrayList<>();

        for (Group group : groups) {
            groupDTOs.add(new GroupDTO(group.getGno(), group.getGname(), group.getGdate()));
        }

        return groupDTOs;
    }

    public GroupDTO getGroupById(int gno) {
        Group group = groupRepository.findByGno(gno).orElse(null);
        if (group != null) {
            return new GroupDTO(group.getGno(), group.getGname(), group.getGdate());
        } else {
            return null;
        }
    }

    public List<GroupDTO> getGroupsByMember(Member member) {
        List<Group> groups = groupRepository.findGroupsByMember(member.getMno());
        List<GroupDTO> groupDTOs = new ArrayList<>();

        for (Group group : groups) {
            groupDTOs.add(new GroupDTO(group.getGno(), group.getGname(), group.getGdate()));
        }

        return groupDTOs;
    }

    @Transactional
    public void createGroup(GroupDTO groupDTO) {
        Group group = new Group();
        group.setGname(groupDTO.getGname());

        // 그룹을 저장하고 자동 생성된 gno 값을 설정
        Group savedGroup = groupRepository.save(group);

        // 저장된 그룹의 gno 값을 groupDTO에 설정
        groupDTO.setGno(savedGroup.getGno());
    }

    public void deleteGroupByGno(int gno) {
        groupRepository.deleteById(gno);
    }

    public int groupMemberCount(int gno) {
        return memberGroupRepository.countByGroupGno(gno);
    }

    // 모임방 수정을 위해 추가(다혜)
    public Group findGroupByGno(int gno) {
        return groupRepository.findByGno(gno).orElse(null);
    }
}