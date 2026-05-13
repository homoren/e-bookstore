package com.ebookstore.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseItem {
    private Long id;
    private Long purchaseId;
    private Long bookId;
    private String bookTitle;
    private Integer quantity;
    private BigDecimal costPrice;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
}