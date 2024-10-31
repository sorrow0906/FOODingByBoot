package com.example.foodingbyboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "storetag_t")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int stno; // 리뷰 태그의 고유 식별자

    @ManyToOne
    @JoinColumn(name = "sno")
    private Store store; // 리뷰와의 다대일 관계

    @ManyToOne
    @JoinColumn(name = "tno")
    private Tag tag; // 태그와의 다대일 관계

    private int tagCount;

}

