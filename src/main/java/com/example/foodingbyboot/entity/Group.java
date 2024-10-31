package com.example.foodingbyboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "group_table")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gno")
    private int gno;

    @OneToMany(mappedBy = "group")
    private List<MemberGroup> memberGroupList;

    @Column(name = "gname", nullable = false)
    private String gname;

    @Column(name = "gdate", nullable = false)
    private LocalDateTime gdate;

    private String gimage;

    @PrePersist
    protected void onCreate() {
        gdate = LocalDateTime.now();
    }

    public String toString() {
        return "Group( gno = " + gno + ", gname = " + gname +" )";
    }
}