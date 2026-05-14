package com.ebookstore.service;

import com.ebookstore.dto.CartItemDTO;
import java.util.List;

public interface CartService {
    List<CartItemDTO> getCartList(Long userId);
    void addToCart(Long userId, Long bookId, Integer quantity);
    void updateQuantity(Long userId, Long cartId, Integer quantity);
    void deleteItem(Long userId, Long cartId);
    void clearCart(Long userId);
}