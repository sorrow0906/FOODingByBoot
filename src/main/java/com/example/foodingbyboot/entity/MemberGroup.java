package com.example.foodingbyboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "join_t")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jno")
    private int jno;

    @ManyToOne
    @JoinColumn(name = "gno", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "mno", nullable = false)
    private Member member;

    @Column(name = "jauth")
    private int jauth;

    // 새로 추가된 부분
    @Column(name = "jdate", nullable = false)
    private LocalDateTime jdate;

    @PrePersist
    protected void onCreate() {
        jdate = LocalDateTime.now();
    }
}