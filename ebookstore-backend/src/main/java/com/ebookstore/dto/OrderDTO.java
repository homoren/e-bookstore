package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;  // 状态文本
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDate paymentDeadline;
    private LocalDate deliveryDeadline;
    private LocalDateTime paidAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    // 获取状态文本
    public String getStatusText() {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待汇款确认";
            case 2 -> "已收款，待配送";
            case 3 -> "已配送";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }
}