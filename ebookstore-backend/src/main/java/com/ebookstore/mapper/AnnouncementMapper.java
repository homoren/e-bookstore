package com.ebookstore.mapper;

import com.ebookstore.entity.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Insert("INSERT INTO announcement (title, content, is_top, status) " +
            "VALUES (#{title}, #{content}, #{isTop}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    @Update("UPDATE announcement SET title = #{title}, content = #{content}, " +
            "is_top = #{isTop}, status = #{status} WHERE id = #{id}")
    int update(Announcement announcement);

    @Delete("DELETE FROM announcement WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM announcement WHERE status = 1 ORDER BY is_top DESC, created_at DESC")
    List<Announcement> findPublished();

    @Select("SELECT * FROM announcement ORDER BY is_top DESC, created_at DESC")
    List<Announcement> findAll();

    @Select("SELECT * FROM announcement WHERE id = #{id}")
    Announcement findById(@Param("id") Long id);

    @Update("UPDATE announcement SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
}