package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookListDTO {
    private Long id;
    private String title;
    private String author;
    private String coverImage;
    private BigDecimal price;
    private Integer stock;
    private Integer difficultyLevel;
    private String publisher;
}