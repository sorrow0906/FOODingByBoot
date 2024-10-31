package com.example.foodingbyboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "review_t") // 데이터베이스 테이블과 매핑
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rno;

    @ManyToOne
    @JoinColumn(name = "mno")
    private Member member;


    @ManyToOne
    @JoinColumn(name = "sno")
    private Store store;

    private int rstar;
    private String rcomm;
    private LocalDateTime rdate;

    private Integer mdelete;
    private Integer adelete;

    @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE)
    private List<ReviewTag> reviewTags;

    @Transient
    private List<Tag> tags;

    @Transient
    private String dateToString;

    @PrePersist
    protected void onCreate() {
        rdate = LocalDateTime.now();
        /*rsdate = rdate.toString();
        String[] parts = rsdate.split("T");
        rsdate = parts[0];
        rstime = parts[1].substring(0, parts[1].indexOf('.'));*/
    }

    @Override
    public String toString() {
        return "Review{id=" + rno + ", content='" + rcomm + "'}";
    }

}