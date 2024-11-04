package com.example.foodingbyboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "member_t") // 데이터베이스 테이블과 매핑
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int mno;

    @JsonIgnore
    @OneToMany(mappedBy = "member") // 수정사항
    private List<MemberGroup> memberGroupList; // 수정사항

    private String mid;
    private String mname;
    private String mpass;

    // 비밀번호 확인 필드 (테이블에 저장되지 않음)
    private transient String mpassConfirm;

    private int mtype;
    private String mnick;
    private String mbirth;
    private String mphone;
    private String memail;
    private String maddr;
    private LocalDateTime mdate;
    private String mimage;


    private int mwarning;

    @PrePersist
    protected void onCreate() {
        mdate = LocalDateTime.now();
    }

    public String getMpassConfirm() {
        return mpassConfirm;
    }

    public void setMpassConfirm(String mpassConfirm) {
        this.mpassConfirm = mpassConfirm;
    }

    @Override
    public String toString() {
        return "Member{mno=" + mno + ", mid='" + mid + "', mname='" + mname + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(mid, member.mid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mid);
    }
}
