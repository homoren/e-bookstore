package com.ebookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebookstore.config.CacheConfig;
import com.ebookstore.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    // 一级分类
    @Cacheable(value = CacheConfig.CATEGORIES, key = "'level1'")
    @Select("SELECT * FROM category WHERE parent_id = 0 ORDER BY sort_order")
    List<Category> findLevel1();

    // 某一级分类下的二级分类
    @Cacheable(value = CacheConfig.CATEGORIES, key = "'level2_' + #parentId")
    @Select("SELECT * FROM category WHERE parent_id = #{parentId} ORDER BY sort_order")
    List<Category> findByParentId(@Param("parentId") Integer parentId);
}
