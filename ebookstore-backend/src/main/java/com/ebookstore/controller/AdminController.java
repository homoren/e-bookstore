package com.ebookstore.controller;

import com.ebookstore.common.Result;
import com.ebookstore.dto.*;
import com.ebookstore.entity.Book;
import com.ebookstore.service.AdminService;
import com.ebookstore.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    // ========== 图书管理 ==========
    @PostMapping("/book/create")
    public Result<Book> createBook(@RequestBody Book book) {
        return Result.ok("新增成功", adminService.createBook(book));
    }

    @PutMapping("/book/update/{id}")
    public Result<Void> updateBook(@PathVariable Long id, @RequestBody Book book) {
        book.setId(id);
        adminService.updateBook(book);
        return Result.ok("更新成功");
    }

    @PutMapping("/book/status/{id}")
    public Result<Void> toggleBookStatus(@PathVariable Long id) {
        adminService.toggleBookStatus(id);
        return Result.ok("状态已更新");
    }

    // ========== 进货管理 ==========
    @PostMapping("/purchase/create")
    public Result<PurchaseDTO> createPurchase(@Valid @RequestBody CreatePurchaseRequest request) {
        return Result.ok("进货成功", adminService.createPurchase(request));
    }

    @GetMapping("/purchase/list")
    public Result<List<PurchaseDTO>> getPurchaseList() {
        return Result.ok(adminService.getPurchaseList());
    }

    @GetMapping("/purchase/detail/{id}")
    public Result<PurchaseDTO> getPurchaseDetail(@PathVariable Long id) {
        return Result.ok(adminService.getPurchaseDetail(id));
    }

    // ========== 订单管理（店主操作）==========
    @PutMapping("/order/confirm-payment/{id}")
    public Result<Void> confirmPayment(@PathVariable Long id) {
        orderService.confirmPayment(id);
        return Result.ok("已确认收款");
    }

    @PutMapping("/order/confirm-delivery/{id}")
    public Result<Void> confirmDelivery(@PathVariable Long id) {
        orderService.confirmDelivery(id);
        return Result.ok("已确认配送");
    }

    @PutMapping("/order/complete/{id}")
    public Result<Void> completeOrder(@PathVariable Long id,
                                      @RequestParam(required = false) String receiptSignature) {
        orderService.completeOrder(id, receiptSignature);
        return Result.ok("订单已完成");
    }

    @GetMapping("/order/list")
    public Result<List<OrderDTO>> getAllOrders() {
        return Result.ok(adminService.getAllOrders());
    }

    @GetMapping("/order/list-by-status/{status}")
    public Result<List<OrderDTO>> getOrdersByStatus(@PathVariable Integer status) {
        return Result.ok(adminService.getOrdersByStatus(status));
    }

    // ========== 公告管理 ==========
    @PostMapping("/announcement/create")
    public Result<AnnouncementDTO> createAnnouncement(@Valid @RequestBody CreateAnnouncementRequest request) {
        return Result.ok("公告发布成功", adminService.createAnnouncement(request));
    }

    @PutMapping("/announcement/update/{id}")
    public Result<AnnouncementDTO> updateAnnouncement(@PathVariable Long id,
                                                      @Valid @RequestBody CreateAnnouncementRequest request) {
        return Result.ok("公告更新成功", adminService.updateAnnouncement(id, request));
    }

    @DeleteMapping("/announcement/delete/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        adminService.deleteAnnouncement(id);
        return Result.ok("公告已删除");
    }

    @GetMapping("/announcement/list")
    public Result<List<AnnouncementDTO>> getAllAnnouncements() {
        return Result.ok(adminService.getAllAnnouncements());
    }

    // ========== 留言管理 ==========
    @PutMapping("/message/reply/{id}")
    public Result<MessageDTO> replyMessage(@PathVariable Long id,
                                           @Valid @RequestBody ReplyMessageRequest request) {
        return Result.ok("回复成功", adminService.replyMessage(id, request));
    }

    @DeleteMapping("/message/delete/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        adminService.deleteMessage(id);
        return Result.ok("留言已删除");
    }

    @PutMapping("/message/toggle/{id}")
    public Result<Void> toggleMessageStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.toggleMessageStatus(id, status);
        return Result.ok("状态已更新");
    }

    @GetMapping("/message/list")
    public Result<List<MessageDTO>> getAllMessages() {
        return Result.ok(adminService.getAllMessages());
    }

    // ========== 日结帐管理 ==========
    @PostMapping("/settlement/generate")
    public Result<DailySettlementDTO> generateSettlement(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.ok("日结生成成功", adminService.generateDailySettlement(date));
    }

    @GetMapping("/settlement/list")
    public Result<List<DailySettlementDTO>> getSettlementList() {
        return Result.ok(adminService.getSettlementList());
    }

    @GetMapping("/settlement/today")
    public Result<DailySettlementDTO> getTodaySettlement() {
        return Result.ok(adminService.getTodaySettlement());
    }

    @GetMapping("/settlement/range")
    public Result<List<DailySettlementDTO>> getSettlementByRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.ok(adminService.getSettlementByDateRange(startDate, endDate));
    }

    // ========== 客户管理 ==========
    @GetMapping("/customer/list")
    public Result<List<CustomerDTO>> getAllCustomers() {
        return Result.ok(adminService.getAllCustomers());
    }

    @GetMapping("/customer/detail/{userId}")
    public Result<CustomerDTO> getCustomerDetail(@PathVariable Long userId) {
        return Result.ok(adminService.getCustomerDetail(userId));
    }
}
