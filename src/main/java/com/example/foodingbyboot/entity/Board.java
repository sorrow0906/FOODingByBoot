package com.example.foodingbyboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "board_t") // 데이터베이스 테이블과 매핑
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bno")
    private int bno;

    @ManyToOne
    @JoinColumn(name = "gno", nullable = false)
    private Group group;

    @Column(name = "bname", nullable = false)
    private String bname;

    @Column(name = "btype", nullable = false)
    private int btype;

    @OneToMany(mappedBy = "board")
    private List<Write> writes;

    @Override
    public String toString() {
        return "Board{bno=" + bno + ", bname='" + bname + "'}";
    }
}
