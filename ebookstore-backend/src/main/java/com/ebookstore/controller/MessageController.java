package com.ebookstore.controller;

import com.ebookstore.common.Result;
import com.ebookstore.dto.CreateMessageRequest;
import com.ebookstore.dto.MessageDTO;
import com.ebookstore.service.AdminService;
import com.ebookstore.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final AdminService adminService;
    private final JwtUtils jwtUtils;

    // 获取已发布的留言列表
    @GetMapping("/list")
    public Result<List<MessageDTO>> getPublishedMessages() {
        return Result.ok(adminService.getPublishedMessages());
    }

    // 发布留言（登录用户可选，未登录允许匿名）
    @PostMapping("/create")
    public Result<MessageDTO> createMessage(@Valid @RequestBody CreateMessageRequest request,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = null;
        String username = "匿名用户";

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                userId = jwtUtils.getUserIdFromToken(token);
                username = jwtUtils.getUsernameFromToken(token);
            }
        }

        return Result.ok("留言成功", adminService.createMessage(userId, username, request));
    }
}
