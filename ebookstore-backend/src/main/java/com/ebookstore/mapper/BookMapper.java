package com.ebookstore.mapper;

import com.ebookstore.dto.BookDetailDTO;
import com.ebookstore.dto.BookListDTO;
import com.ebookstore.entity.Book;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookMapper {

    // 根据二级分类ID查询图书列表（二级目录展示）
    @Select("SELECT id, title, author, cover_image, price, stock, difficulty_level, publisher " +
            "FROM book WHERE category_id = #{categoryId} AND status = 1 ORDER BY sales_count DESC")
    @Results(id = "bookListMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "author", column = "author"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "price", column = "price"),
            @Result(property = "stock", column = "stock"),
            @Result(property = "difficultyLevel", column = "difficulty_level"),
            @Result(property = "publisher", column = "publisher")
    })
    List<BookListDTO> findBooksByCategoryId(@Param("categoryId") Integer categoryId);

    // 获取图书详情（三层信息）
    @Select("SELECT b.*, c.name as category_name, pc.name as parent_category_name " +
            "FROM book b " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "LEFT JOIN category pc ON c.parent_id = pc.id " +
            "WHERE b.id = #{id} AND b.status = 1")
    @Results(id = "bookDetailMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "author", column = "author"),
            @Result(property = "isbn", column = "isbn"),
            @Result(property = "publisher", column = "publisher"),
            @Result(property = "publishDate", column = "publish_date"),
            @Result(property = "price", column = "price"),
            @Result(property = "stock", column = "stock"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "description", column = "description"),
            @Result(property = "detailHtml", column = "detail_html"),
            @Result(property = "sampleCodeUrl", column = "sample_code_url"),
            @Result(property = "difficultyLevel", column = "difficulty_level"),
            @Result(property = "categoryName", column = "category_name"),
            @Result(property = "parentCategoryName", column = "parent_category_name")
    })
    BookDetailDTO findBookDetailById(@Param("id") Long id);

    // 根据一级分类ID获取所有图书（可选，用于"全部"浏览）
    @Select("SELECT id, title, author, cover_image, price, stock, difficulty_level, publisher " +
            "FROM book WHERE category_id IN (SELECT id FROM category WHERE parent_id = #{parentId}) " +
            "AND status = 1 ORDER BY sales_count DESC")
    List<BookListDTO> findBooksByParentCategoryId(@Param("parentId") Integer parentId);

    // 更新库存
    @Update("UPDATE book SET stock = #{stock} WHERE id = #{id}")
    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);

    // 更新状态（上架/下架）
    @Update("UPDATE book SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 支持关键词模糊搜索
    @Select({
            "<script>",
            "SELECT id, title, author, cover_image, price, stock, difficulty_level, publisher ",
            "FROM book ",
            "WHERE status = 1 ",
            "<if test='categoryId != null and categoryId != 0'>",
            "   AND category_id = #{categoryId} ",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "   AND (title LIKE CONCAT('%', #{keyword}, '%') OR author LIKE CONCAT('%', #{keyword}, '%')) ",
            "</if>",
            "ORDER BY sales_count DESC",
            "</script>"
    })
    List<BookListDTO> findBooksByCategoryIdAndKeyword(
            @Param("categoryId") Integer categoryId,
            @Param("keyword") String keyword
    );
}