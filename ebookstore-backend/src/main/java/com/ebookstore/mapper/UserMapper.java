package com.ebookstore.mapper;

import com.ebookstore.dto.UserInfoDTO;
import com.ebookstore.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    // 根据用户名查询用户
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    // 根据ID查询用户
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    // 插入新用户
    @Insert("INSERT INTO user (username, password, real_name, email, phone, address, role, status) " +
            "VALUES (#{username}, #{password}, #{realName}, #{email}, #{phone}, #{address}, 1, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    // 检查用户名是否存在
    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    // 获取用户信息（不含密码）
    @Select("SELECT id, username, real_name, email, phone, address, role " +
            "FROM user WHERE id = #{id}")
    UserInfoDTO findUserInfoById(@Param("id") Long id);
}