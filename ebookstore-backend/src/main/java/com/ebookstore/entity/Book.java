package com.ebookstore.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Book {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private LocalDate publishDate;
    private Integer categoryId;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stock;
    private String coverImage;
    private String description;
    private String detailHtml;
    private String sampleCodeUrl;
    private Integer difficultyLevel;
    private Integer salesCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}