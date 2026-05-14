package com.ebookstore.service;

import com.ebookstore.dto.LoginRequest;
import com.ebookstore.dto.LoginResponse;
import com.ebookstore.dto.RegisterRequest;
import com.ebookstore.dto.UserInfoDTO;

public interface UserService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    UserInfoDTO getUserInfo(Long userId);
    boolean checkUsernameExists(String username);
}