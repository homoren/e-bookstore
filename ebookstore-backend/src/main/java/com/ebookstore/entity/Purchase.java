package com.ebookstore.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Purchase {
    private Long id;
    private String purchaseNo;
    private String supplier;
    private BigDecimal totalCost;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}