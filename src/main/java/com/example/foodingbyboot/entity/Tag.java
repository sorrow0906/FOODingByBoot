package com.example.foodingbyboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Tag_t")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tno;

    private String ttag;

    @OneToMany(mappedBy = "tag")
    private List<ReviewTag> reviewTags;

}
