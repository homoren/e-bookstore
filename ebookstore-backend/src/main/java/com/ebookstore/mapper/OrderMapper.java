package com.ebookstore.mapper;

import com.ebookstore.dto.OrderDTO;
import com.ebookstore.entity.Order;
import com.ebookstore.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    // 创建订单
    @Insert("INSERT INTO `order` (order_no, user_id, total_amount, status, " +
            "receiver_name, receiver_phone, receiver_address, remark, " +
            "payment_deadline, delivery_deadline) " +
            "VALUES (#{orderNo}, #{userId}, #{totalAmount}, #{status}, " +
            "#{receiverName}, #{receiverPhone}, #{receiverAddress}, #{remark}, " +
            "#{paymentDeadline}, #{deliveryDeadline})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    // 插入订单明细
    @Insert("INSERT INTO order_item (order_id, book_id, book_title, book_author, " +
            "book_price, quantity, subtotal) " +
            "VALUES (#{orderId}, #{bookId}, #{bookTitle}, #{bookAuthor}, " +
            "#{bookPrice}, #{quantity}, #{subtotal})")
    int insertItem(OrderItem item);

    // 查询用户订单列表
    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> findByUserId(@Param("userId") Long userId);

    // 查询订单详情
    @Select("SELECT * FROM `order` WHERE id = #{id}")
    Order findById(@Param("id") Long orderId);

    // 根据订单号查询
    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo}")
    Order findByOrderNo(@Param("orderNo") String orderNo);

    // 查询订单明细
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> findItemsByOrderId(@Param("orderId") Long orderId);

    // 更新订单状态
    @Update("UPDATE `order` SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long orderId, @Param("status") Integer status);

    // 店主确认收款
    @Update("UPDATE `order` SET status = 2, paid_at = NOW() WHERE id = #{id}")
    int confirmPayment(@Param("id") Long orderId);

    // 店主确认配送
    @Update("UPDATE `order` SET status = 3, delivered_at = NOW() WHERE id = #{id}")
    int confirmDelivery(@Param("id") Long orderId);

    // 完成订单（上传回执）
    @Update("UPDATE `order` SET status = 4, completed_at = NOW(), receipt_signature = #{receiptSignature} WHERE id = #{id}")
    int completeOrder(@Param("id") Long orderId, @Param("receiptSignature") String receiptSignature);

    // 取消订单
    @Update("UPDATE `order` SET status = 5 WHERE id = #{id}")
    int cancelOrder(@Param("id") Long orderId);

    // 扣减库存
    @Update("UPDATE book SET stock = stock - #{quantity}, sales_count = sales_count + #{quantity} WHERE id = #{bookId} AND stock >= #{quantity}")
    int reduceStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);

    // 恢复库存（取消订单时）
    @Update("UPDATE book SET stock = stock + #{quantity} WHERE id = #{bookId}")
    int restoreStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);

    // 查询所有订单
    @Select("SELECT * FROM `order` ORDER BY created_at DESC")
    List<Order> findAll();

    // 按状态查询订单
    @Select("SELECT * FROM `order` WHERE status = #{status} ORDER BY created_at DESC")
    List<Order> findByStatus(@Param("status") Integer status);
}