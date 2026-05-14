package com.ebookstore.service.impl;

import com.ebookstore.dto.*;
import com.ebookstore.entity.*;
import com.ebookstore.mapper.*;
import com.ebookstore.service.AdminService;
import com.ebookstore.utils.PurchaseNoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PurchaseNoGenerator purchaseNoGenerator;

    // ========== 进货管理 ==========
    @Override
    @Transactional
    public PurchaseDTO createPurchase(CreatePurchaseRequest request) {
        BigDecimal totalCost = BigDecimal.ZERO;
        List<PurchaseItem> items = new ArrayList<>();

        for (var itemReq : request.getItems()) {
            var book = bookMapper.findBookDetailById(itemReq.getBookId());
            if (book == null) {
                throw new RuntimeException("图书ID " + itemReq.getBookId() + " 不存在");
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
        List<PurchaseDTO> result = new ArrayList<>();
        for (Purchase purchase : purchases) {
            List<PurchaseItem> items = purchaseMapper.findItemsByPurchaseId(purchase.getId());
            result.add(buildPurchaseDTO(purchase, items));
        }
        return result;
    }

    @Override
    public PurchaseDTO getPurchaseDetail(Long purchaseId) {
        Purchase purchase = purchaseMapper.findById(purchaseId);
        if (purchase == null) {
            throw new RuntimeException("进货单不存在");
        }
        List<PurchaseItem> items = purchaseMapper.findItemsByPurchaseId(purchaseId);
        return buildPurchaseDTO(purchase, items);
    }

    // ========== 库存管理 ==========
    @Override
    public void updateBookStock(Long bookId, Integer stock) {
        // 直接更新库存
        // 这里可以添加一个专门的更新库存的方法
    }

    @Override
    public void updateBookStatus(Long bookId, Integer status) {
        // 更新图书上下架状态
    }

    // ========== 公告管理 ==========
    @Override
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
    public AnnouncementDTO updateAnnouncement(Long id, CreateAnnouncementRequest request) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setIsTop(request.getIsTop());
        announcementMapper.update(announcement);
        return buildAnnouncementDTO(announcement);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    public List<AnnouncementDTO> getAllAnnouncements() {
        List<Announcement> announcements = announcementMapper.findAll();
        return announcements.stream().map(this::buildAnnouncementDTO).toList();
    }

    @Override
    public List<AnnouncementDTO> getPublishedAnnouncements() {
        List<Announcement> announcements = announcementMapper.findPublished();
        return announcements.stream().map(this::buildAnnouncementDTO).toList();
    }

    @Override
    public AnnouncementDTO getAnnouncementDetail(Long id) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
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
            throw new RuntimeException("留言不存在");
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
        List<Order> orders = orderMapper.findAll();  // 需要添加 findAll 方法
        List<OrderDTO> result = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> items = orderMapper.findItemsByOrderId(order.getId());
            result.add(buildOrderDTO(order, items));
        }
        return result;
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(Integer status) {
        // 需要添加按状态查询的方法
        return null;
    }

    // ========== 辅助方法 ==========
    private PurchaseDTO buildPurchaseDTO(Purchase purchase, List<PurchaseItem> items) {
        PurchaseDTO dto = new PurchaseDTO();
        dto.setId(purchase.getId());
        dto.setPurchaseNo(purchase.getPurchaseNo());
        dto.setSupplier(purchase.getSupplier());
        dto.setTotalCost(purchase.getTotalCost());
        dto.setStatus(purchase.getStatus());
        dto.setRemark(purchase.getRemark());
        dto.setCreatedAt(purchase.getCreatedAt());

        List<PurchaseItemDTO> itemDTOs = new ArrayList<>();
        for (PurchaseItem item : items) {
            PurchaseItemDTO itemDTO = new PurchaseItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setBookId(item.getBookId());
            itemDTO.setBookTitle(item.getBookTitle());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setCostPrice(item.getCostPrice());
            itemDTO.setSubtotal(item.getSubtotal());
            itemDTOs.add(itemDTO);
        }
        dto.setItems(itemDTOs);

        return dto;
    }

    private AnnouncementDTO buildAnnouncementDTO(Announcement announcement) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        dto.setIsTop(announcement.getIsTop());
        dto.setViewCount(announcement.getViewCount());
        dto.setCreatedAt(announcement.getCreatedAt());
        return dto;
    }

    private MessageDTO buildMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setUserId(message.getUserId());
        dto.setUsername(message.getUsername());
        dto.setContent(message.getContent());
        dto.setReply(message.getReply());
        dto.setRepliedAt(message.getRepliedAt());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    private DailySettlementDTO buildSettlementDTO(DailySettlement settlement) {
        DailySettlementDTO dto = new DailySettlementDTO();
        dto.setId(settlement.getId());
        dto.setSettleDate(settlement.getSettleDate());
        dto.setTotalSales(settlement.getTotalSales());
        dto.setTotalCost(settlement.getTotalCost());
        dto.setTotalProfit(settlement.getTotalProfit());
        dto.setOrderCount(settlement.getOrderCount());
        dto.setPaidOrderCount(settlement.getPaidOrderCount());
        return dto;
    }

    private OrderDTO buildOrderDTO(Order order, List<OrderItem> items) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setRemark(order.getRemark());
        dto.setPaymentDeadline(order.getPaymentDeadline());
        dto.setDeliveryDeadline(order.getDeliveryDeadline());
        dto.setPaidAt(order.getPaidAt());
        dto.setDeliveredAt(order.getDeliveredAt());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setBookId(item.getBookId());
            itemDTO.setBookTitle(item.getBookTitle());
            itemDTO.setBookAuthor(item.getBookAuthor());
            itemDTO.setBookPrice(item.getBookPrice());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setSubtotal(item.getSubtotal());
            itemDTOs.add(itemDTO);
        }
        dto.setItems(itemDTOs);

        return dto;
    }
}