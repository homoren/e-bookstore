package com.ebookstore.service.impl;

import com.ebookstore.dto.*;
import com.ebookstore.entity.Order;
import com.ebookstore.entity.OrderItem;
import com.ebookstore.mapper.CartMapper;
import com.ebookstore.mapper.OrderMapper;
import com.ebookstore.service.OrderService;
import com.ebookstore.utils.OrderNoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderNoGenerator orderNoGenerator;

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // 1. 获取购物车选中项
        List<CartItemDTO> cartItems = cartMapper.findByIds(userId, request.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车中没有选中的商品");
        }

        // 2. 检查库存并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDTO item : cartItems) {
            if (item.getQuantity() > item.getStock()) {
                throw new RuntimeException("商品《" + item.getBookTitle() + "》库存不足");
            }

            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setBookId(item.getBookId());
            orderItem.setBookTitle(item.getBookTitle());
            orderItem.setBookAuthor(item.getBookAuthor());
            orderItem.setBookPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);
        }

        // 3. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNoGenerator.generate());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);  // 待付款
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());
        order.setPaymentDeadline(LocalDate.now().plusDays(7));  // 7日内汇款

        orderMapper.insert(order);

        // 4. 保存订单明细并扣减库存
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderMapper.insertItem(item);

            // 扣减库存
            int result = orderMapper.reduceStock(item.getBookId(), item.getQuantity());
            if (result == 0) {
                throw new RuntimeException("扣减库存失败");
            }
        }

        // 5. 删除购物车中已下单的商品
        cartMapper.deleteByIds(userId, request.getCartItemIds());

        // 6. 返回订单信息
        return buildOrderDTO(order, orderItems);
    }

    @Override
    public OrderDTO getOrderDetail(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        List<OrderItem> items = orderMapper.findItemsByOrderId(orderId);
        return buildOrderDTO(order, items);
    }

    @Override
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.findByUserId(userId);
        List<OrderDTO> result = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> items = orderMapper.findItemsByOrderId(order.getId());
            result.add(buildOrderDTO(order, items));
        }
        return result;
    }

    @Override
    @Transactional
    public void confirmPayment(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确，当前状态：" + getStatusText(order.getStatus()));
        }

        orderMapper.confirmPayment(orderId);

        // 设置配送截止日期（收款后10日内）
        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setDeliveryDeadline(LocalDate.now().plusDays(10));
        // 这里可以添加一个更新配送截止日期的SQL
    }

    @Override
    @Transactional
    public void confirmDelivery(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确，当前状态：" + getStatusText(order.getStatus()));
        }

        orderMapper.confirmDelivery(orderId);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, String receiptSignature) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("订单状态不正确，当前状态：" + getStatusText(order.getStatus()));
        }

        orderMapper.completeOrder(orderId, receiptSignature);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new RuntimeException("当前状态无法取消订单");
        }

        // 恢复库存
        List<OrderItem> items = orderMapper.findItemsByOrderId(orderId);
        for (OrderItem item : items) {
            orderMapper.restoreStock(item.getBookId(), item.getQuantity());
        }

        orderMapper.cancelOrder(orderId);
    }

    private OrderDTO buildOrderDTO(Order order, List<OrderItem> items) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setRemark(order.getRemark());
        dto.setPaymentDeadline(order.getPaymentDeadline());
        dto.setDeliveryDeadline(order.getDeliveryDeadline());
        dto.setPaidAt(order.getPaidAt());
        dto.setDeliveredAt(order.getDeliveredAt());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setBookId(item.getBookId());
            itemDTO.setBookTitle(item.getBookTitle());
            itemDTO.setBookAuthor(item.getBookAuthor());
            itemDTO.setBookPrice(item.getBookPrice());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setSubtotal(item.getSubtotal());
            itemDTOs.add(itemDTO);
        }
        dto.setItems(itemDTOs);

        return dto;
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待汇款确认";
            case 2 -> "已收款待配送";
            case 3 -> "已配送";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }
}