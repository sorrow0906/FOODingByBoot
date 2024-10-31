package com.example.foodingbyboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "reviewtag_t")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rtno; // 리뷰 태그의 고유 식별자

    @ManyToOne
    @JoinColumn(name = "rno")
    private Review review; // 리뷰와의 다대일 관계

    @ManyToOne
    @JoinColumn(name = "tno")
    private Tag tag; // 태그와의 다대일 관계

    @Override
    public String toString() {
        return "ReviewTag{id=" + rtno + ", tag='" + tag + "'}";
    }
}
