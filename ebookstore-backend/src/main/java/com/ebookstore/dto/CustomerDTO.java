package com.ebookstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String address;
    private Integer orderCount;      // 订单总数
    private BigDecimal totalSpent;   // 累计消费
    private LocalDateTime lastOrderTime; // 最后下单时间
    private LocalDateTime createdAt;
}