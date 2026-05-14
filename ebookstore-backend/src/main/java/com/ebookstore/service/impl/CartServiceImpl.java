package com.ebookstore.service.impl;

import com.ebookstore.dto.CartItemDTO;
import com.ebookstore.mapper.BookMapper;
import com.ebookstore.mapper.CartMapper;
import com.ebookstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private BookMapper bookMapper;

    @Override
    public List<CartItemDTO> getCartList(Long userId) {
        return cartMapper.findCartByUserId(userId);
    }

    @Override
    @Transactional
    public void addToCart(Long userId, Long bookId, Integer quantity) {
        // 检查图书是否存在且有库存
        var book = bookMapper.findBookDetailById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存：" + book.getStock());
        }

        cartMapper.insertOrUpdate(userId, bookId, quantity);
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        cartMapper.updateQuantity(cartId, userId, quantity);
    }

    @Override
    public void deleteItem(Long userId, Long cartId) {
        cartMapper.deleteById(cartId, userId);
    }

    @Override
    public void clearCart(Long userId) {
        cartMapper.clearCart(userId);
    }
}