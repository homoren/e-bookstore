package com.ebookstore.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private String reply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}