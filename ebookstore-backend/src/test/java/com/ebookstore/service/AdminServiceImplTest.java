package com.ebookstore.service;

import com.ebookstore.common.BusinessException;
import com.ebookstore.dto.BookDetailDTO;
import com.ebookstore.dto.CreatePurchaseRequest;
import com.ebookstore.dto.DailySettlementDTO;
import com.ebookstore.dto.PurchaseDTO;
import com.ebookstore.dto.TodayStatsDTO;
import com.ebookstore.entity.Book;
import com.ebookstore.entity.Purchase;
import com.ebookstore.map.DTOMapper;
import com.ebookstore.mapper.*;
import com.ebookstore.service.impl.AdminServiceImpl;
import com.ebookstore.utils.PurchaseNoGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminServiceImplTest {

    @Mock
    private PurchaseMapper purchaseMapper;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private AnnouncementMapper announcementMapper;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private SettlementMapper settlementMapper;
    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private PurchaseNoGenerator purchaseNoGenerator;
    @Mock
    private DTOMapper dtoMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        when(purchaseNoGenerator.generate()).thenReturn("TEST-PURCHASE-NO");
        when(dtoMapper.toPurchaseDTO(any(Purchase.class))).thenAnswer(inv -> {
            Purchase p = inv.getArgument(0);
            PurchaseDTO dto = new PurchaseDTO();
            dto.setTotalCost(p.getTotalCost());
            return dto;
        });
    }

    private BookDetailDTO bookDetail(Long bookId, String title) {
        BookDetailDTO dto = new BookDetailDTO();
        dto.setId(bookId);
        dto.setTitle(title);
        return dto;
    }

    private CreatePurchaseRequest.PurchaseItemRequest item(Long bookId, int quantity, String costPrice) {
        CreatePurchaseRequest.PurchaseItemRequest req = new CreatePurchaseRequest.PurchaseItemRequest();
        req.setBookId(bookId);
        req.setQuantity(quantity);
        req.setCostPrice(new BigDecimal(costPrice));
        return req;
    }

    // ========== 进货 ==========
    @Test
    @DisplayName("进货:计算总成本并增加库存、更新成本价")
    void createPurchase_calculatesTotalAndIncreaseStock() {
        when(bookMapper.findBookDetailById(10L)).thenReturn(bookDetail(10L, "Java编程思想"));
        when(bookMapper.findBookDetailById(11L)).thenReturn(bookDetail(11L, "MySQL必知必会"));
        when(purchaseMapper.insert(any(Purchase.class))).thenAnswer(inv -> {
            Purchase p = inv.getArgument(0);
            p.setId(200L);
            return 1;
        });

        CreatePurchaseRequest request = new CreatePurchaseRequest();
        request.setSupplier("出版社A");
        request.setItems(List.of(item(10L, 2, "30.00"), item(11L, 3, "20.00")));

        PurchaseDTO dto = adminService.createPurchase(request);

        // 总成本 = 2*30 + 3*20 = 120
        assertEquals(new BigDecimal("120.00"), dto.getTotalCost());
        verify(purchaseMapper).increaseStock(10L, 2, new BigDecimal("30.00"));
        verify(purchaseMapper).increaseStock(11L, 3, new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("进货:图书不存在抛异常")
    void createPurchase_bookNotFound_throws() {
        when(bookMapper.findBookDetailById(10L)).thenReturn(null);

        CreatePurchaseRequest request = new CreatePurchaseRequest();
        request.setItems(List.of(item(10L, 1, "30.00")));

        assertThrows(BusinessException.class, () -> adminService.createPurchase(request));
    }

    // ========== 图书上下架 ==========
    @Test
    @DisplayName("上下架:上架状态(1)切换为下架(0)")
    void toggleBookStatus_onToOff() {
        Book book = new Book();
        book.setId(10L);
        book.setStatus(1);
        when(bookMapper.findById(10L)).thenReturn(book);

        adminService.toggleBookStatus(10L);

        verify(bookMapper).updateStatus(10L, 0);
    }

    @Test
    @DisplayName("上下架:下架状态(0)切换为上架(1)")
    void toggleBookStatus_offToOn() {
        Book book = new Book();
        book.setId(10L);
        book.setStatus(0);
        when(bookMapper.findById(10L)).thenReturn(book);

        adminService.toggleBookStatus(10L);

        verify(bookMapper).updateStatus(10L, 1);
    }

    // ========== 今日统计 ==========
    @Test
    @DisplayName("今日统计:实时计算订单数/销售额/利润/会员数")
    void getTodayStats_calculatesLiveData() {
        DailySettlementDTO sales = new DailySettlementDTO();
        sales.setTotalSales(new BigDecimal("500.00"));
        sales.setOrderCount(3);
        when(settlementMapper.calculateDailySales(any(LocalDate.class))).thenReturn(sales);
        when(settlementMapper.calculateDailyCost(any(LocalDate.class))).thenReturn(new BigDecimal("300.00"));
        when(customerMapper.countCustomers()).thenReturn(10L);

        TodayStatsDTO stats = adminService.getTodayStats();

        assertEquals(3, stats.getOrderCount());
        assertEquals(new BigDecimal("500.00"), stats.getTotalSales());
        assertEquals(new BigDecimal("200.00"), stats.getTotalProfit());
        assertEquals(10L, stats.getMemberCount());
    }

    // ========== 日结 ==========
    @Test
    @DisplayName("日结:利润 = 销售额 - 成本")
    void generateDailySettlement_calculatesProfit() {
        DailySettlementDTO sales = new DailySettlementDTO();
        sales.setTotalSales(new BigDecimal("1000.00"));
        sales.setOrderCount(5);
        sales.setPaidOrderCount(3);
        when(settlementMapper.calculateDailySales(any(LocalDate.class))).thenReturn(sales);
        when(settlementMapper.calculateDailyCost(any(LocalDate.class))).thenReturn(new BigDecimal("600.00"));

        adminService.generateDailySettlement(LocalDate.of(2026, 8, 12));

        // 捕获保存的日结对象并校验利润
        verify(settlementMapper).save(argThat(s ->
                s.getTotalSales().compareTo(new BigDecimal("1000.00")) == 0
                        && s.getTotalCost().compareTo(new BigDecimal("600.00")) == 0
                        && s.getTotalProfit().compareTo(new BigDecimal("400.00")) == 0
                        && s.getOrderCount() == 5
                        && s.getPaidOrderCount() == 3));
    }
}
