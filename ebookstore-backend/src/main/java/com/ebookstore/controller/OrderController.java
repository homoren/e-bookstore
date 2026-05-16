package com.ebookstore.controller;

import com.ebookstore.dto.CreateOrderRequest;
import com.ebookstore.dto.OrderDTO;
import com.ebookstore.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 创建订单
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                         HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        try {
            OrderDTO order = orderService.createOrder(userId, request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单创建成功");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取用户订单列表
    @GetMapping("/list")
    public ResponseEntity<?> getOrderList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<OrderDTO> orders = orderService.getUserOrders(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", orders);
        return ResponseEntity.ok(response);
    }

    // 获取订单详情
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            OrderDTO order = orderService.getOrderDetail(id);
            // 验证订单是否属于当前用户（管理员除外）
            if (!order.getUserId().equals(userId)) {
                Integer role = (Integer) request.getAttribute("role");
                if (role != 2) {  // 不是店主
                    return ResponseEntity.status(403).body(Map.of("message", "无权查看此订单"));
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 取消订单
    @PutMapping("/cancel/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            OrderDTO order = orderService.getOrderDetail(id);
            if (!order.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("message", "无权操作此订单"));
            }
            orderService.cancelOrder(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "订单已取消"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}