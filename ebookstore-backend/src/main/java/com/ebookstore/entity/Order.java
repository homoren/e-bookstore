package com.ebookstore.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;  // 0待付款 1待汇款确认 2已收款待配送 3已配送 4已完成 5已取消
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDate paymentDeadline;
    private LocalDate deliveryDeadline;
    private LocalDateTime paidAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private String receiptSignature;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}