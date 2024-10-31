package com.example.foodingbyboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "invite_t")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ino;

    @ManyToOne
    @JoinColumn(name = "jno")
    private MemberGroup memberGroup; // 참여 (Join 엔티티)

    @ManyToOne
    @JoinColumn(name = "mno")
    private Member member; // 회원

    private int itype; // 처리유형

    @Column(name = "leadnum")
    private int leadNum;

    @Override
    public String toString() {
        return "Invite{ino=" + ino + ", memberGroup=" + memberGroup + ", itype=" + itype + "}";
    }
}
