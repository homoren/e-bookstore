package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookDetailDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private LocalDate publishDate;
    private BigDecimal price;
    private Integer stock;
    private String coverImage;
    private String description;
    private String detailHtml;
    private String sampleCodeUrl;
    private Integer difficultyLevel;
    private String categoryName;      // 所属二级分类名
    private String parentCategoryName; // 所属一级分类名
}