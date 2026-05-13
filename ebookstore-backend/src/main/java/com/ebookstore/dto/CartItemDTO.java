package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String coverImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;  // 库存
    private BigDecimal subtotal;
}