package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreatePurchaseRequest {
    private String supplier;
    private String remark;
    private List<PurchaseItemRequest> items;

    @Data
    public static class PurchaseItemRequest {
        private Long bookId;
        private Integer quantity;
        private BigDecimal costPrice;
    }
}