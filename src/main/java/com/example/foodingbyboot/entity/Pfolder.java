package com.example.foodingbyboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "pfolder_t")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pfolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int pfno;

    public String pfname;

    @ManyToOne
    @JoinColumn(name = "mno", nullable = true)
    public Member member;

}
