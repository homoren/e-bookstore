package com.ebookstore.service.impl;

import com.ebookstore.common.BusinessException;
import com.ebookstore.dto.CartItemDTO;
import com.ebookstore.mapper.BookMapper;
import com.ebookstore.mapper.CartMapper;
import com.ebookstore.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final BookMapper bookMapper;

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
            throw new BusinessException("图书不存在");
        }
        if (book.getStock() < quantity) {
            throw new BusinessException("库存不足，当前库存：" + book.getStock());
        }

        cartMapper.insertOrUpdate(userId, bookId, quantity);
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        int result = cartMapper.updateQuantity(cartId, userId, quantity);
        if (result == 0) {
            throw new BusinessException("购物车商品不存在");
        }
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
