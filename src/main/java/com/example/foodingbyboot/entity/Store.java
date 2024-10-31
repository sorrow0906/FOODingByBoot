package com.example.foodingbyboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "store_t")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Store {
    @Id
    private int sno;

    private String sname;
    private String saddr;
    private String stel;
    private String seg;
    private String scate;
    private String stime;
    private String spark;

    @Transient
    private String photoUrl; // @Transient 어노테이션을 추가해서 DB에 저장되지 않음

    @Transient
    private double distance;

    @Transient
    private int pickNum;

    @Transient
    private double scoreArg;

    @Override
    public String toString() {
        return "Store{ sno=" + sno + ", sname=" + sname + ", pickNum=" + pickNum + "scoreArg=" + scoreArg + " }";
    }
}
