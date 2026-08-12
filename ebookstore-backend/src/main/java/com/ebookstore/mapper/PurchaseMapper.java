package com.ebookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebookstore.entity.Purchase;
import com.ebookstore.entity.PurchaseItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.math.BigDecimal;

@Mapper
public interface PurchaseMapper extends BaseMapper<Purchase> {

    @Insert("INSERT INTO purchase_item (purchase_id, book_id, book_title, quantity, cost_price, subtotal) " +
            "VALUES (#{purchaseId}, #{bookId}, #{bookTitle}, #{quantity}, #{costPrice}, #{subtotal})")
    int insertItem(PurchaseItem item);

    @Select("SELECT * FROM purchase ORDER BY created_at DESC")
    List<Purchase> findAll();

    @Select("SELECT * FROM purchase WHERE id = #{id}")
    Purchase findById(@Param("id") Long id);

    @Select("SELECT * FROM purchase_item WHERE purchase_id = #{purchaseId}")
    List<PurchaseItem> findItemsByPurchaseId(@Param("purchaseId") Long purchaseId);

    // 批量查询进货单明细（消除 N+1）
    @Select("<script>" +
            "SELECT * FROM purchase_item WHERE purchase_id IN " +
            "<foreach collection='purchaseIds' item='purchaseId' open='(' separator=',' close=')'>#{purchaseId}</foreach>" +
            "</script>")
    List<PurchaseItem> findItemsByPurchaseIds(@Param("purchaseIds") List<Long> purchaseIds);

    @Update("UPDATE book SET stock = stock + #{quantity}, cost_price = #{costPrice} " +
            "WHERE id = #{bookId}")
    int increaseStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity,
                      @Param("costPrice") BigDecimal costPrice);
}