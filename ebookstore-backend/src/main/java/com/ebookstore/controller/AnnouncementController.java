package com.ebookstore.controller;

import com.ebookstore.common.Result;
import com.ebookstore.dto.AnnouncementDTO;
import com.ebookstore.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AdminService adminService;

    // 获取已发布的公告列表
    @GetMapping("/list")
    public Result<List<AnnouncementDTO>> getPublishedAnnouncements() {
        return Result.ok(adminService.getPublishedAnnouncements());
    }

    // 获取公告详情
    @GetMapping("/detail/{id}")
    public Result<AnnouncementDTO> getAnnouncementDetail(@PathVariable Long id) {
        return Result.ok(adminService.getAnnouncementDetail(id));
    }
}
