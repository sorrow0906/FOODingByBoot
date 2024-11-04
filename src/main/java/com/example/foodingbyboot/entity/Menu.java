package com.example.foodingbyboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_t")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int mnno;

    private String mnname;

    private String mnprice;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "sno")
    private Store store;
}
