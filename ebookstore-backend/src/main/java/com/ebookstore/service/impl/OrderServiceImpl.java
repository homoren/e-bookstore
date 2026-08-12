package com.ebookstore.service.impl;

import com.ebookstore.common.BusinessException;
import com.ebookstore.dto.CartItemDTO;
import com.ebookstore.dto.CreateOrderRequest;
import com.ebookstore.dto.OrderDTO;
import com.ebookstore.entity.Order;
import com.ebookstore.entity.OrderItem;
import com.ebookstore.map.DTOMapper;
import com.ebookstore.mapper.CartMapper;
import com.ebookstore.mapper.OrderMapper;
import com.ebookstore.service.OrderService;
import com.ebookstore.utils.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;
    private final OrderNoGenerator orderNoGenerator;
    private final DTOMapper dtoMapper;

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // 1. 获取购物车选中项
        List<CartItemDTO> cartItems = cartMapper.findByIds(userId, request.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车中没有选中的商品");
        }

        // 2. 检查库存并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDTO item : cartItems) {
            if (item.getQuantity() > item.getStock()) {
                throw new BusinessException("商品《" + item.getBookTitle() + "》库存不足");
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
                throw new BusinessException("扣减库存失败");
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
            throw new BusinessException("订单不存在");
        }
        List<OrderItem> items = orderMapper.findItemsByOrderId(orderId);
        return buildOrderDTO(order, items);
    }

    @Override
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.findByUserId(userId);
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrder = orderMapper.findItemsByOrderIds(orderIds)
                .stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        return orders.stream()
                .map(o -> buildOrderDTO(o, itemsByOrder.getOrDefault(o.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public void confirmPayment(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusText(order.getStatus()));
        }

        // 确认收款，并设置配送截止日期（收款后10日内）
        orderMapper.confirmPayment(orderId, LocalDate.now().plusDays(10));
    }

    @Override
    @Transactional
    public void confirmDelivery(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusText(order.getStatus()));
        }

        orderMapper.confirmDelivery(orderId);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, String receiptSignature) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusText(order.getStatus()));
        }

        orderMapper.completeOrder(orderId, receiptSignature);
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException("当前状态无法取消订单");
        }

        // 恢复库存
        List<OrderItem> items = orderMapper.findItemsByOrderId(orderId);
        for (OrderItem item : items) {
            orderMapper.restoreStock(item.getBookId(), item.getQuantity());
        }

        orderMapper.cancelOrder(orderId);
    }

    private OrderDTO buildOrderDTO(Order order, List<OrderItem> items) {
        OrderDTO dto = dtoMapper.toOrderDTO(order);
        dto.setItems(dtoMapper.toOrderItemDTOs(items));
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
