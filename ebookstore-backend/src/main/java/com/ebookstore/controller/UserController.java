package com.ebookstore.controller;

import com.ebookstore.dto.*;
import com.ebookstore.service.UserService;
import com.ebookstore.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功，请登录");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 检查用户名是否存在
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "用户名已存在" : "用户名可用");
        return ResponseEntity.ok(response);
    }

    // 获取当前用户信息（需要登录）
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            // 去掉 "Bearer " 前缀
            String token = authHeader.substring(7);

            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(401).body(Map.of("message", "Token无效或已过期"));
            }

            Long userId = jwtUtils.getUserIdFromToken(token);
            UserInfoDTO userInfo = userService.getUserInfo(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "认证失败"));
        }
    }
}