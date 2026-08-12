package com.ebookstore.dto;

import com.ebookstore.entity.OrderStatus;
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

    // 获取状态文本(复用订单状态机枚举)
    public String getStatusText() {
        OrderStatus s = OrderStatus.fromCode(status);
        return s != null ? s.getText() : "未知";
    }
}