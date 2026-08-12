package com.ebookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebookstore.dto.DailySettlementDTO;
import com.ebookstore.entity.DailySettlement;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Mapper
public interface SettlementMapper extends BaseMapper<DailySettlement> {

    // 计算某天的销售数据
    @Select("SELECT " +
            "COALESCE(SUM(o.total_amount), 0) as total_sales, " +
            "COUNT(o.id) as order_count, " +
            "SUM(CASE WHEN o.status >= 2 THEN 1 ELSE 0 END) as paid_order_count " +
            "FROM `order` o " +
            "WHERE DATE(o.created_at) = #{date}")
    DailySettlementDTO calculateDailySales(@Param("date") LocalDate date);

    // 计算某天的成本（根据售出图书的成本价计算）
    @Select("SELECT COALESCE(SUM(oi.quantity * b.cost_price), 0) as total_cost " +
            "FROM order_item oi " +
            "JOIN `order` o ON oi.order_id = o.id " +
            "JOIN book b ON oi.book_id = b.id " +
            "WHERE DATE(o.created_at) = #{date} AND o.status >= 2")
    BigDecimal calculateDailyCost(@Param("date") LocalDate date);

    // 插入或更新日结数据
    @Insert("INSERT INTO daily_settlement (settle_date, total_sales, total_cost, total_profit, " +
            "order_count, paid_order_count) " +
            "VALUES (#{settleDate}, #{totalSales}, #{totalCost}, #{totalProfit}, " +
            "#{orderCount}, #{paidOrderCount}) " +
            "ON DUPLICATE KEY UPDATE " +
            "total_sales = VALUES(total_sales), " +
            "total_cost = VALUES(total_cost), " +
            "total_profit = VALUES(total_profit), " +
            "order_count = VALUES(order_count), " +
            "paid_order_count = VALUES(paid_order_count)")
    int save(DailySettlement settlement);

    // 查询日结列表
    @Select("SELECT * FROM daily_settlement ORDER BY settle_date DESC")
    List<DailySettlement> findAll();

    // 根据日期范围查询
    @Select("SELECT * FROM daily_settlement WHERE settle_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY settle_date DESC")
    List<DailySettlement> findByDateRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // 查询某天的日结
    @Select("SELECT * FROM daily_settlement WHERE settle_date = #{date}")
    DailySettlement findByDate(@Param("date") LocalDate date);
}