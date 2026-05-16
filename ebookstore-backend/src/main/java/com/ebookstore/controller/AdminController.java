package com.ebookstore.controller;

import com.ebookstore.dto.*;
import com.ebookstore.service.AdminService;
import com.ebookstore.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private OrderService orderService;

    // 验证店主权限
    private boolean checkAdminRole(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        return role != null && role == 2;
    }

    private ResponseEntity<?> requireAdmin(HttpServletRequest request) {
        if (!checkAdminRole(request)) {
            return ResponseEntity.status(403).body(Map.of("message", "需要店主权限"));
        }
        return null;
    }

    // ========== 进货管理 ==========
    @PostMapping("/purchase/create")
    public ResponseEntity<?> createPurchase(@Valid @RequestBody CreatePurchaseRequest request,
                                            HttpServletRequest httpRequest) {
        ResponseEntity<?> authCheck = requireAdmin(httpRequest);
        if (authCheck != null) return authCheck;

        try {
            PurchaseDTO purchase = adminService.createPurchase(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "进货成功", "data", purchase));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/purchase/list")
    public ResponseEntity<?> getPurchaseList(HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        List<PurchaseDTO> purchases = adminService.getPurchaseList();
        return ResponseEntity.ok(Map.of("success", true, "data", purchases));
    }

    @GetMapping("/purchase/detail/{id}")
    public ResponseEntity<?> getPurchaseDetail(@PathVariable Long id, HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        try {
            PurchaseDTO purchase = adminService.getPurchaseDetail(id);
            return ResponseEntity.ok(Map.of("success", true, "data", purchase));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ========== 订单管理（店主操作）==========
    @PutMapping("/order/confirm-payment/{id}")
    public ResponseEntity<?> confirmPayment(@PathVariable Long id, HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        try {
            orderService.confirmPayment(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "已确认收款"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/order/confirm-delivery/{id}")
    public ResponseEntity<?> confirmDelivery(@PathVariable Long id, HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        try {
            orderService.confirmDelivery(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "已确认配送"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/order/complete/{id}")
    public ResponseEntity<?> completeOrder(@PathVariable Long id,
                                           @RequestParam(required = false) String receiptSignature,
                                           HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        try {
            orderService.completeOrder(id, receiptSignature);
            return ResponseEntity.ok(Map.of("success", true, "message", "订单已完成"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/order/list")
    public ResponseEntity<?> getAllOrders(HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        List<OrderDTO> orders = adminService.getAllOrders();
        return ResponseEntity.ok(Map.of("success", true, "data", orders));
    }

    // ========== 公告管理 ==========
    @PostMapping("/announcement/create")
    public ResponseEntity<?> createAnnouncement(@Valid @RequestBody CreateAnnouncementRequest request,
                                                HttpServletRequest httpRequest) {
        ResponseEntity<?> authCheck = requireAdmin(httpRequest);
        if (authCheck != null) return authCheck;

        AnnouncementDTO announcement = adminService.createAnnouncement(request);
        return ResponseEntity.ok(Map.of("success", true, "message", "公告发布成功", "data", announcement));
    }

    @PutMapping("/announcement/update/{id}")
    public ResponseEntity<?> updateAnnouncement(@PathVariable Long id,
                                                @Valid @RequestBody CreateAnnouncementRequest request,
                                                HttpServletRequest httpRequest) {
        ResponseEntity<?> authCheck = requireAdmin(httpRequest);
        if (authCheck != null) return authCheck;

        try {
            AnnouncementDTO announcement = adminService.updateAnnouncement(id, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "公告更新成功", "data", announcement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/announcement/delete/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id, HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        adminService.deleteAnnouncement(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "公告已删除"));
    }

    @GetMapping("/announcement/list")
    public ResponseEntity<?> getAllAnnouncements(HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        List<AnnouncementDTO> announcements = adminService.getAllAnnouncements();
        return ResponseEntity.ok(Map.of("success", true, "data", announcements));
    }

    // ========== 留言管理 ==========
    @PutMapping("/message/reply/{id}")
    public ResponseEntity<?> replyMessage(@PathVariable Long id,
                                          @Valid @RequestBody ReplyMessageRequest request,
                                          HttpServletRequest httpRequest) {
        ResponseEntity<?> authCheck = requireAdmin(httpRequest);
        if (authCheck != null) return authCheck;

        try {
            MessageDTO message = adminService.replyMessage(id, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "回复成功", "data", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/message/delete/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id, HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        adminService.deleteMessage(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "留言已删除"));
    }

    @PutMapping("/message/toggle/{id}")
    public ResponseEntity<?> toggleMessageStatus(@PathVariable Long id,
                                                 @RequestParam Integer status,
                                                 HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        adminService.toggleMessageStatus(id, status);
        return ResponseEntity.ok(Map.of("success", true, "message", "状态已更新"));
    }

    @GetMapping("/message/list")
    public ResponseEntity<?> getAllMessages(HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        List<MessageDTO> messages = adminService.getAllMessages();
        return ResponseEntity.ok(Map.of("success", true, "data", messages));
    }

    // ========== 日结帐管理 ==========
    @PostMapping("/settlement/generate")
    public ResponseEntity<?> generateSettlement(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        DailySettlementDTO settlement = adminService.generateDailySettlement(date);
        return ResponseEntity.ok(Map.of("success", true, "message", "日结生成成功", "data", settlement));
    }

    @GetMapping("/settlement/list")
    public ResponseEntity<?> getSettlementList(HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        List<DailySettlementDTO> settlements = adminService.getSettlementList();
        return ResponseEntity.ok(Map.of("success", true, "data", settlements));
    }

    @GetMapping("/settlement/today")
    public ResponseEntity<?> getTodaySettlement(HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        DailySettlementDTO settlement = adminService.getTodaySettlement();
        return ResponseEntity.ok(Map.of("success", true, "data", settlement));
    }

    @GetMapping("/settlement/range")
    public ResponseEntity<?> getSettlementByRange(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                  HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        List<DailySettlementDTO> settlements = adminService.getSettlementByDateRange(startDate, endDate);
        return ResponseEntity.ok(Map.of("success", true, "data", settlements));
    }

    // ========== 客户管理 ==========
    @GetMapping("/customer/list")
    public ResponseEntity<?> getAllCustomers(HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        List<CustomerDTO> customers = adminService.getAllCustomers();
        return ResponseEntity.ok(Map.of("success", true, "data", customers));
    }

    @GetMapping("/customer/detail/{userId}")
    public ResponseEntity<?> getCustomerDetail(@PathVariable Long userId, HttpServletRequest request) {
        ResponseEntity<?> authCheck = requireAdmin(request);
        if (authCheck != null) return authCheck;

        CustomerDTO customer = adminService.getCustomerDetail(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", customer));
    }
}