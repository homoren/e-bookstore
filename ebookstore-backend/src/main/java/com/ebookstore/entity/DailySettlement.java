package com.ebookstore.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DailySettlement {
    private Long id;
    private LocalDate settleDate;
    private BigDecimal totalSales;
    private BigDecimal totalCost;
    private BigDecimal totalProfit;
    private Integer orderCount;
    private Integer paidOrderCount;
    private LocalDateTime createdAt;
}