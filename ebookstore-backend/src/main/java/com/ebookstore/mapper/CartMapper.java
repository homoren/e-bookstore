package com.ebookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebookstore.dto.CartItemDTO;
import com.ebookstore.entity.Cart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    // 查询用户购物车
    @Select("SELECT c.id, c.book_id, c.quantity, b.title as book_title, b.author as book_author, " +
            "b.cover_image, b.price, b.stock " +
            "FROM cart c " +
            "JOIN book b ON c.book_id = b.id " +
            "WHERE c.user_id = #{userId} AND b.status = 1 " +
            "ORDER BY c.created_at DESC")
    List<CartItemDTO> findCartByUserId(@Param("userId") Long userId);

    // 添加商品到购物车
    @Insert("INSERT INTO cart (user_id, book_id, quantity) VALUES (#{userId}, #{bookId}, #{quantity}) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}")
    int insertOrUpdate(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("quantity") Integer quantity);

    // 更新购物车数量
    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id} AND user_id = #{userId}")
    int updateQuantity(@Param("id") Long cartId, @Param("userId") Long userId, @Param("quantity") Integer quantity);

    // 删除购物车商品
    @Delete("DELETE FROM cart WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long cartId, @Param("userId") Long userId);

    // 批量删除
    @Delete("<script>" +
            "DELETE FROM cart WHERE user_id = #{userId} AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int deleteByIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    // 清空用户购物车
    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    int clearCart(@Param("userId") Long userId);

    // 根据ID查询购物车项
    @Select("SELECT * FROM cart WHERE id = #{id} AND user_id = #{userId}")
    Cart findById(@Param("id") Long cartId, @Param("userId") Long userId);

    // 批量查询购物车项
    @Select("<script>" +
            "SELECT c.*, b.title as book_title, b.author as book_author, b.price, b.stock " +
            "FROM cart c JOIN book b ON c.book_id = b.id " +
            "WHERE c.user_id = #{userId} AND c.id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<CartItemDTO> findByIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}