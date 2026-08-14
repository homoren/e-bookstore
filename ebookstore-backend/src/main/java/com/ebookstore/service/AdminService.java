package com.ebookstore.service;

import com.ebookstore.dto.*;
import com.ebookstore.entity.Book;
import java.time.LocalDate;
import java.util.List;

public interface AdminService {

    // ========== 进货管理 ==========
    PurchaseDTO createPurchase(CreatePurchaseRequest request);
    List<PurchaseDTO> getPurchaseList();
    PurchaseDTO getPurchaseDetail(Long purchaseId);

    // ========== 库存管理 ==========
    void updateBookStock(Long bookId, Integer stock);
    void updateBookStatus(Long bookId, Integer status);

    // ========== 图书管理 ==========
    Book createBook(Book book);
    void updateBook(Book book);
    void toggleBookStatus(Long bookId);

    // ========== 公告管理 ==========
    AnnouncementDTO createAnnouncement(CreateAnnouncementRequest request);
    AnnouncementDTO updateAnnouncement(Long id, CreateAnnouncementRequest request);
    void deleteAnnouncement(Long id);
    List<AnnouncementDTO> getAllAnnouncements();
    List<AnnouncementDTO> getPublishedAnnouncements();
    AnnouncementDTO getAnnouncementDetail(Long id);

    // ========== 留言管理 ==========
    MessageDTO createMessage(Long userId, String username, CreateMessageRequest request);
    MessageDTO replyMessage(Long id, ReplyMessageRequest request);
    void deleteMessage(Long id);
    void toggleMessageStatus(Long id, Integer status);
    List<MessageDTO> getAllMessages();
    List<MessageDTO> getPublishedMessages();

    // ========== 日结帐管理 ==========
    DailySettlementDTO generateDailySettlement(LocalDate date);
    List<DailySettlementDTO> getSettlementList();
    List<DailySettlementDTO> getSettlementByDateRange(LocalDate startDate, LocalDate endDate);
    DailySettlementDTO getTodaySettlement();

    // 今日实时统计(不落表,实时计算,供店主后台看板)
    TodayStatsDTO getTodayStats();

    // ========== 客户管理 ==========
    List<CustomerDTO> getAllCustomers();
    CustomerDTO getCustomerDetail(Long userId);

    // ========== 订单管理（店主视角）==========
    List<OrderDTO> getAllOrders();
    List<OrderDTO> getOrdersByStatus(Integer status);
}