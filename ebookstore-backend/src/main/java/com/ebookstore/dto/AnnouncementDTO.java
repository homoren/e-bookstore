package com.ebookstore.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private Integer isTop;
    private Integer viewCount;
    private LocalDateTime createdAt;
}