package com.ebookstore.controller;

import com.ebookstore.common.Result;
import com.ebookstore.dto.AddToCartRequest;
import com.ebookstore.dto.CartItemDTO;
import com.ebookstore.dto.UpdateCartRequest;
import com.ebookstore.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 获取购物车列表
    @GetMapping("/list")
    public Result<List<CartItemDTO>> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(cartService.getCartList(userId));
    }

    // 添加到购物车
    @PostMapping("/add")
    public Result<Void> addToCart(@Valid @RequestBody AddToCartRequest addRequest,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.addToCart(userId, addRequest.getBookId(), addRequest.getQuantity());
        return Result.ok("添加成功");
    }

    // 更新购物车数量
    @PutMapping("/update/{id}")
    public Result<Void> updateQuantity(@PathVariable Long id,
                                       @Valid @RequestBody UpdateCartRequest request,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        cartService.updateQuantity(userId, id, request.getQuantity());
        return Result.ok("更新成功");
    }

    // 删除购物车商品
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteItem(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.deleteItem(userId, id);
        return Result.ok("删除成功");
    }

    // 清空购物车
    @DeleteMapping("/clear")
    public Result<Void> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearCart(userId);
        return Result.ok("购物车已清空");
    }
}
