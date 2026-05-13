package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseDTO {
    private Long id;
    private String purchaseNo;
    private String supplier;
    private BigDecimal totalCost;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private List<PurchaseItemDTO> items;
}