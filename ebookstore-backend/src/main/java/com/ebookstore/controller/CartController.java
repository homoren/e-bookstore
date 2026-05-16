package com.ebookstore.controller;

import com.ebookstore.dto.AddToCartRequest;
import com.ebookstore.dto.CartItemDTO;
import com.ebookstore.dto.UpdateCartRequest;
import com.ebookstore.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Objects;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {

    @Autowired
    private CartService cartService;

    // 获取购物车列表
    @GetMapping("/list")
    public ResponseEntity<?> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CartItemDTO> cartList = cartService.getCartList(userId);

        // 计算总金额
        BigDecimal total = cartList.stream()
                .map(CartItemDTO::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", cartList);
        response.put("total", total);
        response.put("count", cartList.size());
        return ResponseEntity.ok(response);
    }

    // 添加到购物车
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest addRequest,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            cartService.addToCart(userId, addRequest.getBookId(), addRequest.getQuantity());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "添加成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 更新购物车数量
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long id,
                                            @Valid @RequestBody UpdateCartRequest request,
                                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        try {
            cartService.updateQuantity(userId, id, request.getQuantity());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 删除购物车商品
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.deleteItem(userId, id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }

    // 清空购物车
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearCart(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "购物车已清空");
        return ResponseEntity.ok(response);
    }
}