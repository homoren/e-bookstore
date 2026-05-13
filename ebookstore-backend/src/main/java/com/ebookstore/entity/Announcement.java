package com.ebookstore.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Announcement {
    private Long id;
    private String title;
    private String content;
    private Integer isTop;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}