package com.ebookstore.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String address;
    private Integer role;      // 1普通会员 2店主
    private Integer status;    // 1正常 0禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}