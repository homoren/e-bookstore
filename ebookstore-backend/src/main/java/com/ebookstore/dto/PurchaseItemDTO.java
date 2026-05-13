package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseItemDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Integer quantity;
    private BigDecimal costPrice;
    private BigDecimal subtotal;
}