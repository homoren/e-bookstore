package com.ebookstore.dto;

import lombok.Data;

@Data
public class UserInfoDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String address;
    private Integer role;
}