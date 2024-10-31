package com.example.foodingbyboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alarm_t")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ano;

    @Column(name = "linkedpk")
    private String linkedPk; // 출처

    private String atype; // 알림유형

    @Transient
    private String message;

    @ManyToOne
    @JoinColumn(name = "mno")
    private Member member; // 회원

    @Column(name = "ischecked")
    private int isChecked; // 확인여부

}
