package com.ebookstore.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private BigDecimal bookPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
}