package com.ebookstore.controller;

import com.ebookstore.dto.CreateMessageRequest;
import com.ebookstore.dto.MessageDTO;
import com.ebookstore.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {

    @Autowired
    private AdminService adminService;

    // 获取已发布的留言列表
    @GetMapping("/list")
    public ResponseEntity<?> getPublishedMessages() {
        List<MessageDTO> messages = adminService.getPublishedMessages();
        return ResponseEntity.ok(Map.of("success", true, "data", messages));
    }

    // 发布留言
    @PostMapping("/create")
    public ResponseEntity<?> createMessage(@Valid @RequestBody CreateMessageRequest request,
                                           HttpServletRequest httpRequest) {
        try {
            Long userId = null;
            String username = "匿名用户";

            // 如果用户已登录，使用用户信息
            try {
                userId = (Long) httpRequest.getAttribute("userId");
                username = (String) httpRequest.getAttribute("username");
            } catch (Exception e) {
                // 未登录用户，允许匿名留言
            }

            MessageDTO message = adminService.createMessage(userId, username, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "留言成功", "data", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}