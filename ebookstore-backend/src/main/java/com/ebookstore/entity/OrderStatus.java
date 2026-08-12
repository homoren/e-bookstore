package com.ebookstore.entity;

import java.util.Map;
import java.util.Set;

/**
 * 订单状态机:定义状态与合法流转。
 * <pre>
 * 待付款(0) ──已汇款──> 待汇款确认(1) ──店主确认收款──> 已收款待配送(2) ──确认配送──> 已配送(3) ──完成──> 已完成(4)
 *    │                    │
 *    └──── 取消(0/1) ────┴─────────────> 已取消(5)
 * </pre>
 */
public enum OrderStatus {
    PENDING_PAYMENT(0, "待付款"),
    AWAITING_REMITTANCE(1, "待汇款确认"),
    PAID(2, "已收款待配送"),
    DELIVERED(3, "已配送"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            PENDING_PAYMENT, Set.of(AWAITING_REMITTANCE, CANCELLED),
            AWAITING_REMITTANCE, Set.of(PAID, CANCELLED),
            PAID, Set.of(DELIVERED),
            DELIVERED, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    private final int code;
    private final String text;

    OrderStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public boolean canTransitionTo(OrderStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public static OrderStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }
}
