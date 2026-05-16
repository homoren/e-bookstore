package com.ebookstore.controller;

import com.ebookstore.dto.AnnouncementDTO;
import com.ebookstore.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnnouncementController {

    @Autowired
    private AdminService adminService;

    // 获取已发布的公告列表
    @GetMapping("/list")
    public ResponseEntity<?> getPublishedAnnouncements() {
        List<AnnouncementDTO> announcements = adminService.getPublishedAnnouncements();
        return ResponseEntity.ok(Map.of("success", true, "data", announcements));
    }

    // 获取公告详情
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getAnnouncementDetail(@PathVariable Long id) {
        try {
            AnnouncementDTO announcement = adminService.getAnnouncementDetail(id);
            return ResponseEntity.ok(Map.of("success", true, "data", announcement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}