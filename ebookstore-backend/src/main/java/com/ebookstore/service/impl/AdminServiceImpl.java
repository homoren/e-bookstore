package com.ebookstore.service.impl;

import com.ebookstore.common.BusinessException;
import com.ebookstore.config.CacheConfig;
import com.ebookstore.dto.*;
import com.ebookstore.entity.*;
import com.ebookstore.map.DTOMapper;
import com.ebookstore.mapper.*;
import com.ebookstore.service.AdminService;
import com.ebookstore.utils.PurchaseNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final PurchaseMapper purchaseMapper;
    private final BookMapper bookMapper;
    private final AnnouncementMapper announcementMapper;
    private final MessageMapper messageMapper;
    private final SettlementMapper settlementMapper;
    private final CustomerMapper customerMapper;
    private final OrderMapper orderMapper;
    private final PurchaseNoGenerator purchaseNoGenerator;
    private final DTOMapper dtoMapper;

    // ========== 进货管理 ==========
    @Override
    @Transactional
    @CacheEvict(value = {CacheConfig.BOOK_LIST, CacheConfig.BOOK_DETAIL}, allEntries = true)
    public PurchaseDTO createPurchase(CreatePurchaseRequest request) {
        BigDecimal totalCost = BigDecimal.ZERO;
        List<PurchaseItem> items = new ArrayList<>();

        for (var itemReq : request.getItems()) {
            var book = bookMapper.findBookDetailById(itemReq.getBookId());
            if (book == null) {
                throw new BusinessException("图书ID " + itemReq.getBookId() + " 不存在");
            }

            BigDecimal subtotal = itemReq.getCostPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalCost = totalCost.add(subtotal);

            PurchaseItem item = new PurchaseItem();
            item.setBookId(itemReq.getBookId());
            item.setBookTitle(book.getTitle());
            item.setQuantity(itemReq.getQuantity());
            item.setCostPrice(itemReq.getCostPrice());
            item.setSubtotal(subtotal);
            items.add(item);
        }

        Purchase purchase = new Purchase();
        purchase.setPurchaseNo(purchaseNoGenerator.generate());
        purchase.setSupplier(request.getSupplier());
        purchase.setTotalCost(totalCost);
        purchase.setStatus(1);
        purchase.setRemark(request.getRemark());

        purchaseMapper.insert(purchase);

        for (PurchaseItem item : items) {
            item.setPurchaseId(purchase.getId());
            purchaseMapper.insertItem(item);

            // 增加库存并更新成本价
            purchaseMapper.increaseStock(item.getBookId(), item.getQuantity(), item.getCostPrice());
        }

        return buildPurchaseDTO(purchase, items);
    }

    @Override
    public List<PurchaseDTO> getPurchaseList() {
        List<Purchase> purchases = purchaseMapper.findAll();
        if (purchases.isEmpty()) {
            return List.of();
        }
        List<Long> purchaseIds = purchases.stream().map(Purchase::getId).toList();
        Map<Long, List<PurchaseItem>> itemsByPurchase = purchaseMapper.findItemsByPurchaseIds(purchaseIds)
                .stream()
                .collect(Collectors.groupingBy(PurchaseItem::getPurchaseId));
        return purchases.stream()
                .map(p -> buildPurchaseDTO(p, itemsByPurchase.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    @Override
    public PurchaseDTO getPurchaseDetail(Long purchaseId) {
        Purchase purchase = purchaseMapper.findById(purchaseId);
        if (purchase == null) {
            throw new BusinessException("进货单不存在");
        }
        List<PurchaseItem> items = purchaseMapper.findItemsByPurchaseId(purchaseId);
        return buildPurchaseDTO(purchase, items);
    }

    // ========== 库存管理 ==========
    @Override
    @CacheEvict(value = {CacheConfig.BOOK_LIST, CacheConfig.BOOK_DETAIL}, allEntries = true)
    public void updateBookStock(Long bookId, Integer stock) {
        bookMapper.updateStock(bookId, stock);
    }

    @Override
    @CacheEvict(value = {CacheConfig.BOOK_LIST, CacheConfig.BOOK_DETAIL}, allEntries = true)
    public void updateBookStatus(Long bookId, Integer status) {
        bookMapper.updateStatus(bookId, status);
    }

    // ========== 图书管理 ==========
    @Override
    @CacheEvict(value = {CacheConfig.BOOK_LIST, CacheConfig.BOOK_DETAIL}, allEntries = true)
    public Book createBook(Book book) {
        book.setId(null);
        book.setStatus(1);  // 新增默认上架
        bookMapper.insert(book);
        return book;
    }

    @Override
    @CacheEvict(value = {CacheConfig.BOOK_LIST, CacheConfig.BOOK_DETAIL}, allEntries = true)
    public void updateBook(Book book) {
        Book exist = bookMapper.findById(book.getId());
        if (exist == null) {
            throw new BusinessException("图书不存在");
        }
        bookMapper.update(book);
    }

    @Override
    @CacheEvict(value = {CacheConfig.BOOK_LIST, CacheConfig.BOOK_DETAIL}, allEntries = true)
    public void toggleBookStatus(Long bookId) {
        Book exist = bookMapper.findById(bookId);
        if (exist == null) {
            throw new BusinessException("图书不存在");
        }
        int newStatus = (exist.getStatus() != null && exist.getStatus() == 1) ? 0 : 1;
        bookMapper.updateStatus(bookId, newStatus);
    }

    // ========== 公告管理 ==========
    @Override
    @CacheEvict(value = CacheConfig.ANNOUNCEMENTS, allEntries = true)
    public AnnouncementDTO createAnnouncement(CreateAnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setIsTop(request.getIsTop());
        announcement.setStatus(1);
        announcementMapper.insert(announcement);
        return buildAnnouncementDTO(announcement);
    }

    @Override
    @CacheEvict(value = CacheConfig.ANNOUNCEMENTS, allEntries = true)
    public AnnouncementDTO updateAnnouncement(Long id, CreateAnnouncementRequest request) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setIsTop(request.getIsTop());
        announcementMapper.update(announcement);
        return buildAnnouncementDTO(announcement);
    }

    @Override
    @CacheEvict(value = CacheConfig.ANNOUNCEMENTS, allEntries = true)
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    public List<AnnouncementDTO> getAllAnnouncements() {
        List<Announcement> announcements = announcementMapper.findAll();
        return announcements.stream().map(this::buildAnnouncementDTO).toList();
    }

    @Override
    @Cacheable(value = CacheConfig.ANNOUNCEMENTS, key = "'published'")
    public List<AnnouncementDTO> getPublishedAnnouncements() {
        List<Announcement> announcements = announcementMapper.findPublished();
        // 用 ArrayList 而非 .toList(),保证缓存反序列化时能构造容器
        return announcements.stream().map(this::buildAnnouncementDTO).collect(Collectors.toList());
    }

    @Override
    public AnnouncementDTO getAnnouncementDetail(Long id) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        announcementMapper.incrementViewCount(id);
        return buildAnnouncementDTO(announcement);
    }

    // ========== 留言管理 ==========
    @Override
    public MessageDTO createMessage(Long userId, String username, CreateMessageRequest request) {
        Message message = new Message();
        message.setUserId(userId);
        message.setUsername(username);
        message.setContent(request.getContent());
        messageMapper.insert(message);
        return buildMessageDTO(message);
    }

    @Override
    public MessageDTO replyMessage(Long id, ReplyMessageRequest request) {
        Message message = messageMapper.findById(id);
        if (message == null) {
            throw new BusinessException("留言不存在");
        }
        messageMapper.reply(id, request.getReply());
        message.setReply(request.getReply());
        return buildMessageDTO(message);
    }

    @Override
    public void deleteMessage(Long id) {
        messageMapper.deleteById(id);
    }

    @Override
    public void toggleMessageStatus(Long id, Integer status) {
        messageMapper.updateStatus(id, status);
    }

    @Override
    public List<MessageDTO> getAllMessages() {
        List<Message> messages = messageMapper.findAll();
        return messages.stream().map(this::buildMessageDTO).toList();
    }

    @Override
    public List<MessageDTO> getPublishedMessages() {
        List<Message> messages = messageMapper.findPublished();
        return messages.stream().map(this::buildMessageDTO).toList();
    }

    // ========== 日结帐管理 ==========
    @Override
    @Transactional
    public DailySettlementDTO generateDailySettlement(LocalDate date) {
        DailySettlementDTO salesData = settlementMapper.calculateDailySales(date);
        BigDecimal totalCost = settlementMapper.calculateDailyCost(date);

        DailySettlement settlement = new DailySettlement();
        settlement.setSettleDate(date);
        settlement.setTotalSales(salesData.getTotalSales());
        settlement.setTotalCost(totalCost != null ? totalCost : BigDecimal.ZERO);
        settlement.setTotalProfit(settlement.getTotalSales().subtract(settlement.getTotalCost()));
        settlement.setOrderCount(salesData.getOrderCount());
        settlement.setPaidOrderCount(salesData.getPaidOrderCount());

        settlementMapper.save(settlement);

        return buildSettlementDTO(settlement);
    }

    @Override
    public List<DailySettlementDTO> getSettlementList() {
        List<DailySettlement> settlements = settlementMapper.findAll();
        return settlements.stream().map(this::buildSettlementDTO).toList();
    }

    @Override
    public List<DailySettlementDTO> getSettlementByDateRange(LocalDate startDate, LocalDate endDate) {
        List<DailySettlement> settlements = settlementMapper.findByDateRange(startDate, endDate);
        return settlements.stream().map(this::buildSettlementDTO).toList();
    }

    @Override
    public DailySettlementDTO getTodaySettlement() {
        LocalDate today = LocalDate.now();
        DailySettlement settlement = settlementMapper.findByDate(today);
        if (settlement == null) {
            return generateDailySettlement(today);
        }
        return buildSettlementDTO(settlement);
    }

    // 今日实时统计:每次实时计算,不读日结表的快照
    @Override
    public TodayStatsDTO getTodayStats() {
        LocalDate today = LocalDate.now();
        DailySettlementDTO sales = settlementMapper.calculateDailySales(today);
        BigDecimal totalCost = settlementMapper.calculateDailyCost(today);
        BigDecimal totalSales = sales.getTotalSales() != null ? sales.getTotalSales() : BigDecimal.ZERO;

        TodayStatsDTO stats = new TodayStatsDTO();
        stats.setOrderCount(sales.getOrderCount() != null ? sales.getOrderCount() : 0);
        stats.setTotalSales(totalSales);
        stats.setTotalProfit(totalSales.subtract(totalCost != null ? totalCost : BigDecimal.ZERO));
        stats.setMemberCount(customerMapper.countCustomers());
        return stats;
    }

    // ========== 客户管理 ==========
    @Override
    public List<CustomerDTO> getAllCustomers() {
        return customerMapper.findAllCustomers();
    }

    @Override
    public CustomerDTO getCustomerDetail(Long userId) {
        return customerMapper.findCustomerDetail(userId);
    }

    // ========== 订单管理（店主视角）==========
    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderMapper.findAll();
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrder = orderMapper.findItemsByOrderIds(orderIds)
                .stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        return orders.stream()
                .map(o -> buildOrderDTO(o, itemsByOrder.getOrDefault(o.getId(), List.of())))
                .toList();
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(Integer status) {
        List<Order> orders = orderMapper.findByStatus(status);
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrder = orderMapper.findItemsByOrderIds(orderIds)
                .stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        return orders.stream()
                .map(o -> buildOrderDTO(o, itemsByOrder.getOrDefault(o.getId(), List.of())))
                .toList();
    }

    // ========== 辅助方法(实体 -> DTO 由 MapStruct 生成) ==========
    private PurchaseDTO buildPurchaseDTO(Purchase purchase, List<PurchaseItem> items) {
        PurchaseDTO dto = dtoMapper.toPurchaseDTO(purchase);
        dto.setItems(dtoMapper.toPurchaseItemDTOs(items));
        return dto;
    }

    private AnnouncementDTO buildAnnouncementDTO(Announcement announcement) {
        return dtoMapper.toAnnouncementDTO(announcement);
    }

    private MessageDTO buildMessageDTO(Message message) {
        return dtoMapper.toMessageDTO(message);
    }

    private DailySettlementDTO buildSettlementDTO(DailySettlement settlement) {
        return dtoMapper.toSettlementDTO(settlement);
    }

    private OrderDTO buildOrderDTO(Order order, List<OrderItem> items) {
        OrderDTO dto = dtoMapper.toOrderDTO(order);
        dto.setItems(dtoMapper.toOrderItemDTOs(items));
        return dto;
    }
}
