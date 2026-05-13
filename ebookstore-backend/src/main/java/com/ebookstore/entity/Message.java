package com.ebookstore.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private String reply;
    private LocalDateTime repliedAt;
    private Integer status;
    private LocalDateTime createdAt;
}