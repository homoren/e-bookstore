package com.ebookstore.mapper;

import com.ebookstore.dto.CustomerDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CustomerMapper {

    @Select("SELECT u.id, u.username, u.real_name, u.email, u.phone, u.address, u.created_at, " +
            "COUNT(o.id) as order_count, " +
            "COALESCE(SUM(CASE WHEN o.status >= 2 THEN o.total_amount ELSE 0 END), 0) as total_spent, " +
            "MAX(o.created_at) as last_order_time " +
            "FROM user u " +
            "LEFT JOIN `order` o ON u.id = o.user_id " +
            "WHERE u.role = 1 " +
            "GROUP BY u.id " +
            "ORDER BY total_spent DESC")
    List<CustomerDTO> findAllCustomers();

    @Select("SELECT u.id, u.username, u.real_name, u.email, u.phone, u.address, u.created_at, " +
            "COUNT(o.id) as order_count, " +
            "COALESCE(SUM(CASE WHEN o.status >= 2 THEN o.total_amount ELSE 0 END), 0) as total_spent, " +
            "MAX(o.created_at) as last_order_time " +
            "FROM user u " +
            "LEFT JOIN `order` o ON u.id = o.user_id " +
            "WHERE u.id = #{userId} " +
            "GROUP BY u.id")
    CustomerDTO findCustomerDetail(@Param("userId") Long userId);
}