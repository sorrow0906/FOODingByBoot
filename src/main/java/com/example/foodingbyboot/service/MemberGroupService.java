package com.example.foodingbyboot.service;

import com.example.foodingbyboot.dto.GroupDTO;
import com.example.foodingbyboot.dto.MemberGroupDTO;
import com.example.foodingbyboot.entity.Group;
import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.entity.MemberGroup;
import com.example.foodingbyboot.repository.GroupRepository;
import com.example.foodingbyboot.repository.MemberGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service
public class MemberGroupService {

    @Autowired
    private MemberGroupRepository memberGroupRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupService groupService;

    public boolean isMemberInGroup(String memberId, int gno) {
        return memberGroupRepository.existsByGroupGnoAndMemberMid(gno, memberId);
    }

    public void addMemberToGroup(Member member, Group group, int jauth) {
        MemberGroup memberGroup = new MemberGroup();
        memberGroup.setGroup(group);
        memberGroup.setMember(member);
        memberGroup.setJauth(jauth);

        memberGroupRepository.save(memberGroup);
    }

    // 회원이 모임장인 모임을 찾는 메서드 추가
    public List<Group> findGroupsWhereMemberIsLeader(String memberId) {
        List<MemberGroup> memberGroups = memberGroupRepository.findByMemberMidAndJauth(memberId, 1);
        List<Group> groups = new ArrayList<>();

        for (MemberGroup memberGroup : memberGroups) {
            groups.add(memberGroup.getGroup());
        }

        return groups;
    }


    // 특정 그룹(gno)의 모든 회원 목록을 조회하는 메서드
    public List<MemberGroup> findMembersByGroupGno(Integer gno) {
        return memberGroupRepository.findByGroupGnoIn(List.of(gno));
    }

    // 모임방 기능을 위한 추가 (수정자 : 희진)
    public List<MemberGroup> getMemberGroupsByGnos(List<Integer> gnos) {
        return memberGroupRepository.findByGroupGnoIn(gnos);
    }

    public MemberGroup getMemberGroupByGroupGnoAndMemberMid(int gno, String mid) {
        return memberGroupRepository.findByGroupGnoAndMemberMid(gno, mid);
    }

    public void removeMemberGroup(MemberGroup memberGroup) {
        memberGroupRepository.delete(memberGroup);
    }

 /*   public List<MemberGroup> getMemberGroupsWithGroup(Member member) {
        return memberGroupRepository.findByMember(member);
    } */

    public List<MemberGroupDTO> getMemberGroupsWithGroup(Member member) {
        List<MemberGroup> memberGroups = memberGroupRepository.findByMember(member);
        List<MemberGroupDTO> memberGroupDTOs = new ArrayList<>();

        for (MemberGroup memberGroup : memberGroups) {
            Group group = memberGroup.getGroup();
            if (group != null) {
                GroupDTO groupDTO;
                if (group.getGimage() != null && !group.getGimage().isEmpty()) {
                    // gimage 값이 비어있지 않으면 gimage를 포함한 생성자를 사용
                    groupDTO = new GroupDTO(group.getGno(), group.getGname(), group.getGdate(), group.getGimage());
                } else {
                    // gimage 값이 비어있으면 gimage 없이 생성자를 사용
                    groupDTO = new GroupDTO(group.getGno(), group.getGname(), group.getGdate());
                }
                MemberGroupDTO memberGroupDTO = new MemberGroupDTO(
                        memberGroup.getJno(),
                        groupDTO,
                        memberGroup.getMember().getMnick(),
                        memberGroup.getJauth(),
                        memberGroup.getJdate()
                );
                memberGroupDTOs.add(memberGroupDTO);
            }
        }

        return memberGroupDTOs;
    }

    public List<MemberGroupDTO> findMembersByGroupGnoWithDTO(int gno) {
        List<MemberGroup> memberGroups = memberGroupRepository.findByGroupGno(gno);

        // Convert to MemberGroupDTO
        return memberGroups.stream().map(mg -> {
            GroupDTO groupDTO = groupService.getGroupById(mg.getGroup().getGno());
            return new MemberGroupDTO(
                    mg.getJno(),
                    groupDTO,
                    mg.getMember().getMnick(),
                    mg.getJauth(),
                    mg.getJdate()
            );
        }).collect(Collectors.toList());
    }

    public MemberGroup findMemberGroupByGroupGnoAndNick(int gno, String nick) {
        return memberGroupRepository.findByGroupGnoAndMemberNick(gno, nick);
    }

    public void updateMemberGroupJauth(int gno, String mid, int newJauth) {
        MemberGroup memberGroup = memberGroupRepository.findByGroupGnoAndMemberMid(gno, mid);
        if (memberGroup != null) {
            memberGroup.setJauth(newJauth);
            memberGroupRepository.save(memberGroup);
        }
    }

    public void delegateGroupLeadership(Member leavingMember) {
        // 1. 탈퇴하는 회원이 모임장인 모임을 찾습니다.
        List<Group> groups = findGroupsWhereMemberIsLeader(leavingMember.getMid());

        for (Group group : groups) {
            // 2. 각 모임의 모든 회원을 조회합니다.
            List<MemberGroup> memberGroups = findMembersByGroupGno(group.getGno());
            List<Member> eligibleMembers = new ArrayList<>();

            // 3. 모임장 권한이 없는 일반회원들만 필터링
            for (MemberGroup memberGroup : memberGroups) {
                if (memberGroup.getJauth() == 0) { // 일반회원
                    eligibleMembers.add(memberGroup.getMember());
                }
            }

            // 4. 모임장 권한이 없는 회원이 없는 경우 (모임장 본인만 있는 경우)
            if(eligibleMembers.isEmpty()) {
                groupService.deleteGroupByGno(group.getGno());
            }

            else {
                // 5. 일반회원이 있는 경우
                // 무작위로 한 명의 일반회원 선택
                Random rand = new Random();
                Member newLeader = eligibleMembers.get(rand.nextInt(eligibleMembers.size()));

                // 6. 새 모임장 권한 부여
                updateMemberGroupJauth(group.getGno(), newLeader.getMid(), 1); // 1: 모임장 권한

                // 기존 모임장 권한 제거
                MemberGroup currentLeaderGroup = getMemberGroupByGroupGnoAndMemberMid(group.getGno(), leavingMember.getMid());
                if (currentLeaderGroup != null) {
                    removeMemberGroup(currentLeaderGroup);
                }
            }
        }
    }

    public int getMemberJauth(String memberId, int gno) {
        MemberGroup memberGroup = memberGroupRepository.findByGroupGnoAndMemberMid(gno, memberId);
        return memberGroup != null ? memberGroup.getJauth() : -1; // 권한이 없는 경우 -1 반환
    }

    // 특정 모임의 모임장 정보 조회
    public MemberGroup getGroupLeaderMemberGroup(int groupId) {
        return memberGroupRepository.findLeaderMemberGroupByGroupId(groupId);
    }

    public Member findMemberByJno(int jno) {
        MemberGroup memberGroup = memberGroupRepository.findByJno(jno)
                .orElseThrow(() -> new IllegalArgumentException("No MemberGroup found for jno: " + jno));

        return memberGroup.getMember();
    }


    /*-------------------------------------- 메인화면에 모임방을 위해 추가한 메서드들 (다혜) ------------------------------------------------*/

    public String findMnicksByGroupGno(Integer gno) {
        List<MemberGroup> memberGroups = memberGroupRepository.findByGroupGnoIn(List.of(gno));
        StringJoiner allMemberString = new StringJoiner(" / ");

        for (MemberGroup memberGroup : memberGroups) {
            allMemberString.add(memberGroup.getMember().getMnick());
        }
        return allMemberString.toString();
    }

    public MemberGroup getLeaderByGno(int gno) {
        List<MemberGroup> memberGroups = memberGroupRepository.findByGroupGnoAndJauthIsOne(gno);

        if (memberGroups == null || memberGroups.isEmpty()) {
            return null;
        }
        return memberGroups.get(0);
    }
}