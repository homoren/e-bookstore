package com.ebookstore.controller;

import com.ebookstore.common.Result;
import com.ebookstore.dto.LoginRequest;
import com.ebookstore.dto.LoginResponse;
import com.ebookstore.dto.RegisterRequest;
import com.ebookstore.dto.UserInfoDTO;
import com.ebookstore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 用户注册
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.ok("注册成功，请登录");
    }

    // 用户登录
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.ok("登录成功", response);
    }

    // 检查用户名是否存在
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        return Result.ok(userService.checkUsernameExists(username));
    }

    // 获取当前用户信息（需要登录，由 JwtInterceptor 校验）
    @GetMapping("/info")
    public Result<UserInfoDTO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserInfoDTO userInfo = userService.getUserInfo(userId);
        return Result.ok(userInfo);
    }
}
