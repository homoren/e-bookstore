package com.ebookstore.service;

import com.ebookstore.dto.CreateOrderRequest;
import com.ebookstore.dto.OrderDTO;
import java.util.List;

public interface OrderService {
    OrderDTO createOrder(Long userId, CreateOrderRequest request);
    OrderDTO getOrderDetail(Long orderId);
    List<OrderDTO> getUserOrders(Long userId);
    void confirmPayment(Long orderId);  // 店主确认收款
    void confirmDelivery(Long orderId); // 店主确认配送
    void completeOrder(Long orderId, String receiptSignature); // 完成订单
    void cancelOrder(Long userId, Long orderId); // 用户取消订单（校验归属）
}