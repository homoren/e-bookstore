package com.ebookstore.mapper;

import com.ebookstore.entity.Purchase;
import com.ebookstore.entity.PurchaseItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.math.BigDecimal;

@Mapper
public interface PurchaseMapper {

    @Insert("INSERT INTO purchase (purchase_no, supplier, total_cost, status, remark) " +
            "VALUES (#{purchaseNo}, #{supplier}, #{totalCost}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Purchase purchase);

    @Insert("INSERT INTO purchase_item (purchase_id, book_id, book_title, quantity, cost_price, subtotal) " +
            "VALUES (#{purchaseId}, #{bookId}, #{bookTitle}, #{quantity}, #{costPrice}, #{subtotal})")
    int insertItem(PurchaseItem item);

    @Select("SELECT * FROM purchase ORDER BY created_at DESC")
    List<Purchase> findAll();

    @Select("SELECT * FROM purchase WHERE id = #{id}")
    Purchase findById(@Param("id") Long id);

    @Select("SELECT * FROM purchase_item WHERE purchase_id = #{purchaseId}")
    List<PurchaseItem> findItemsByPurchaseId(@Param("purchaseId") Long purchaseId);

    @Update("UPDATE book SET stock = stock + #{quantity}, cost_price = #{costPrice} " +
            "WHERE id = #{bookId}")
    int increaseStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity,
                      @Param("costPrice") BigDecimal costPrice);
}