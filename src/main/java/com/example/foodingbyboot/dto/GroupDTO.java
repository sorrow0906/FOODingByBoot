package com.example.foodingbyboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {
    private int gno;
    private String gname;
    private LocalDateTime gdate;

    private String gimage;

    public GroupDTO(int gno, String gname, LocalDateTime gdate) {
        this.gno = gno;
        this.gname = gname;
        this.gdate = gdate;
    }
}