package com.ebookstore.controller;

import com.ebookstore.common.BusinessException;
import com.ebookstore.common.Result;
import com.ebookstore.dto.CreateOrderRequest;
import com.ebookstore.dto.OrderDTO;
import com.ebookstore.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 创建订单
    @PostMapping("/create")
    public Result<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                        HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        OrderDTO order = orderService.createOrder(userId, request);
        return Result.ok("订单创建成功", order);
    }

    // 获取用户订单列表
    @GetMapping("/list")
    public Result<List<OrderDTO>> getOrderList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.getUserOrders(userId));
    }

    // 获取订单详情
    @GetMapping("/detail/{id}")
    public Result<OrderDTO> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderDTO order = orderService.getOrderDetail(id);
        // 验证订单是否属于当前用户（店主除外）
        Integer role = (Integer) request.getAttribute("role");
        if (!order.getUserId().equals(userId) && role != 2) {
            throw new BusinessException("无权查看此订单");
        }
        return Result.ok(order);
    }

    // 取消订单
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(userId, id);
        return Result.ok("订单已取消");
    }
}
