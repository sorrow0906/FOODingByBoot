package com.example.foodingbyboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberGroupDTO {
    private int jno;
    private GroupDTO group;
    private String memberNick;
    private int jauth;
    private LocalDateTime jdate;
}