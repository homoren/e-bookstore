package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TodayStatsDTO {
    private Integer orderCount;
    private BigDecimal totalSales;
    private BigDecimal totalProfit;
    private Long memberCount;
}
