package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailySettlementDTO {
    private Long id;
    private LocalDate settleDate;
    private BigDecimal totalSales;
    private BigDecimal totalCost;
    private BigDecimal totalProfit;
    private Integer orderCount;
    private Integer paidOrderCount;
}