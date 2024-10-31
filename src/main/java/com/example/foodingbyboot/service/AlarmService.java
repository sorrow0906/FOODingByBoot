package com.example.foodingbyboot.service;

import com.example.foodingbyboot.entity.Alarm;
import com.example.foodingbyboot.entity.Member;
import com.example.foodingbyboot.repository.AlarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmService {

    @Autowired
    private AlarmRepository alarmRepository;

    // 로그인한 사용자의 모든 알림을 조회
    public List<Alarm> getAlarms(Member member) {
        return alarmRepository.findByMember(member);
    }

    // 알림이 존재하는지 확인
    public boolean hasAlarms(Member member) {
        List<Alarm> alarms = getAlarms(member);
        return !alarms.isEmpty();
    }

    public void saveAlarm(Alarm alarm) {
        alarmRepository.save(alarm);
    }

    // 회원의 ID로 알림을 조회
    public List<Alarm> getAlarmsByMember(String memberId) {
        return alarmRepository.findByMember_Mid(memberId);
    }

    // 알림 ID로 알림 조회
    public Alarm findById(int alarmId) {
        return alarmRepository.findByAno(alarmId);
    }

    public void deleteAlarm(int alarmId) {
        alarmRepository.deleteById(alarmId);
    }

    public void deleteAlarmsByInviteId(int inviteId) {
        // 초대 ID를 문자열로 변환하여 linkedPk로 사용
        String linkedPk = String.valueOf(inviteId);
        alarmRepository.deleteByLinkedPk(linkedPk);
    }
}
