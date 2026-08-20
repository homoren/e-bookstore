package com.ebookstore.service;

import com.ebookstore.common.BusinessException;
import com.ebookstore.dto.CartItemDTO;
import com.ebookstore.dto.CreateOrderRequest;
import com.ebookstore.dto.OrderDTO;
import com.ebookstore.entity.Order;
import com.ebookstore.entity.OrderItem;
import com.ebookstore.map.DTOMapper;
import com.ebookstore.mapper.CartMapper;
import com.ebookstore.mapper.OrderMapper;
import com.ebookstore.service.impl.OrderServiceImpl;
import com.ebookstore.utils.OrderNoGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 100L;

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private OrderNoGenerator orderNoGenerator;
    @Mock
    private DTOMapper dtoMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        when(orderNoGenerator.generate()).thenReturn("TEST-ORDER-NO");
        when(dtoMapper.toOrderDTO(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            OrderDTO dto = new OrderDTO();
            dto.setId(o.getId());
            dto.setUserId(o.getUserId());
            dto.setStatus(o.getStatus());
            dto.setTotalAmount(o.getTotalAmount());
            return dto;
        });
    }

    private Order orderWithStatus(Integer status) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);
        order.setStatus(status);
        return order;
    }

    private CartItemDTO cartItem(Long bookId, BigDecimal price, int quantity, int stock) {
        CartItemDTO item = new CartItemDTO();
        item.setId(1L);
        item.setBookId(bookId);
        item.setBookTitle("测试书");
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setStock(stock);
        return item;
    }

    // ========== 创建订单 ==========
    @Test
    @DisplayName("创建订单:计算总金额、扣减库存、清空购物车")
    void createOrder_success() {
        CartItemDTO item = cartItem(10L, new BigDecimal("50.00"), 2, 10);
        when(cartMapper.findByIds(USER_ID, List.of(1L))).thenReturn(List.of(item));
        when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(ORDER_ID);
            return 1;
        });
        when(orderMapper.reduceStock(anyLong(), anyInt())).thenReturn(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCartItemIds(List.of(1L));
        request.setReceiverName("张三");

        OrderDTO dto = orderService.createOrder(USER_ID, request);

        assertEquals(new BigDecimal("100.00"), dto.getTotalAmount());
        assertEquals(0, dto.getStatus());
        // 扣减库存 + 删除购物车
        verify(orderMapper).reduceStock(10L, 2);
        verify(cartMapper).deleteByIds(USER_ID, List.of(1L));
    }

    @Test
    @DisplayName("创建订单:购物车为空抛异常")
    void createOrder_noItems_throws() {
        when(cartMapper.findByIds(anyLong(), anyList())).thenReturn(List.of());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCartItemIds(List.of(1L));

        assertThrows(BusinessException.class, () -> orderService.createOrder(USER_ID, request));
    }

    @Test
    @DisplayName("创建订单:库存不足抛异常")
    void createOrder_insufficientStock_throws() {
        CartItemDTO item = cartItem(10L, new BigDecimal("50.00"), 5, 3);
        when(cartMapper.findByIds(USER_ID, List.of(1L))).thenReturn(List.of(item));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCartItemIds(List.of(1L));

        assertThrows(BusinessException.class, () -> orderService.createOrder(USER_ID, request));
    }

    // ========== 状态机流转 ==========
    @Test
    @DisplayName("已汇款:0->1 合法")
    void confirmRemittance_success() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(0));

        orderService.confirmRemittance(USER_ID, ORDER_ID);

        verify(orderMapper).updateStatus(ORDER_ID, 1);
    }

    @Test
    @DisplayName("已汇款:非本人操作抛异常")
    void confirmRemittance_notOwner_throws() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(0));

        assertThrows(BusinessException.class, () -> orderService.confirmRemittance(99L, ORDER_ID));
    }

    @Test
    @DisplayName("已汇款:1->1 非法流转抛异常")
    void confirmRemittance_invalidTransition_throws() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(1));

        assertThrows(BusinessException.class, () -> orderService.confirmRemittance(USER_ID, ORDER_ID));
        verify(orderMapper, never()).updateStatus(anyLong(), anyInt());
    }

    @Test
    @DisplayName("确认收款:1->2 合法,并设置配送截止日期")
    void confirmPayment_success() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(1));

        orderService.confirmPayment(ORDER_ID);

        verify(orderMapper).confirmPayment(eq(ORDER_ID), any());
    }

    @Test
    @DisplayName("确认收款:0->2 非法(跳过汇款确认)抛异常")
    void confirmPayment_skipTransition_throws() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(0));

        assertThrows(BusinessException.class, () -> orderService.confirmPayment(ORDER_ID));
        verify(orderMapper, never()).confirmPayment(anyLong(), any());
    }

    @Test
    @DisplayName("确认配送:2->3 合法")
    void confirmDelivery_success() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(2));

        orderService.confirmDelivery(ORDER_ID);

        verify(orderMapper).confirmDelivery(ORDER_ID);
    }

    @Test
    @DisplayName("完成订单:3->4 合法")
    void completeOrder_success() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(3));

        orderService.completeOrder(ORDER_ID, "sig");

        verify(orderMapper).completeOrder(ORDER_ID, "sig");
    }

    @Test
    @DisplayName("完成订单:2->4 非法跳转抛异常")
    void completeOrder_invalidTransition_throws() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(2));

        assertThrows(BusinessException.class, () -> orderService.completeOrder(ORDER_ID, "sig"));
    }

    // ========== 取消订单 ==========
    @Test
    @DisplayName("取消订单:0->5 合法,恢复库存")
    void cancelOrder_success_restoresStock() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(0));
        OrderItem item = new OrderItem();
        item.setBookId(10L);
        item.setQuantity(2);
        when(orderMapper.findItemsByOrderId(ORDER_ID)).thenReturn(List.of(item));

        orderService.cancelOrder(USER_ID, ORDER_ID);

        verify(orderMapper).restoreStock(10L, 2);
        verify(orderMapper).cancelOrder(ORDER_ID);
    }

    @Test
    @DisplayName("取消订单:已配送(3)无法取消抛异常")
    void cancelOrder_invalidStatus_throws() {
        when(orderMapper.findById(ORDER_ID)).thenReturn(orderWithStatus(3));

        assertThrows(BusinessException.class, () -> orderService.cancelOrder(USER_ID, ORDER_ID));
        verify(orderMapper, never()).cancelOrder(anyLong());
    }

    // ========== 定时关闭超时订单 ==========
    @Test
    @DisplayName("定时关闭超时订单:回补库存并置为已取消")
    void closeExpiredOrders_restoresStock() {
        Order expired = orderWithStatus(0);
        when(orderMapper.findExpiredUnpaidOrders(any(LocalDate.class))).thenReturn(List.of(expired));
        OrderItem item = new OrderItem();
        item.setBookId(10L);
        item.setQuantity(2);
        when(orderMapper.findItemsByOrderId(ORDER_ID)).thenReturn(List.of(item));

        int count = orderService.closeExpiredOrders();

        assertEquals(1, count);
        verify(orderMapper).restoreStock(10L, 2);
        verify(orderMapper).cancelOrder(ORDER_ID);
    }
}
