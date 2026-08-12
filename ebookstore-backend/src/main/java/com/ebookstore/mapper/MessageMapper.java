package com.ebookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebookstore.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Update("UPDATE message SET reply = #{reply}, replied_at = NOW() WHERE id = #{id}")
    int reply(@Param("id") Long id, @Param("reply") String reply);

    @Update("UPDATE message SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM message WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM message WHERE status = 1 ORDER BY created_at DESC")
    List<Message> findPublished();

    @Select("SELECT * FROM message ORDER BY created_at DESC")
    List<Message> findAll();

    @Select("SELECT * FROM message WHERE id = #{id}")
    Message findById(@Param("id") Long id);
}