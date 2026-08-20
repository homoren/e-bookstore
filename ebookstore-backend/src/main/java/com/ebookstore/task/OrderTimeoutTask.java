package com.ebookstore.task;

import com.ebookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时自动关闭:每小时扫描超期未付款/未确认汇款的订单,回补库存。
 */
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    private final OrderService orderService;

    // 每小时整点执行
    @Scheduled(cron = "0 0 * * * *")
    public void closeExpiredOrders() {
        try {
            int count = orderService.closeExpiredOrders();
            if (count > 0) {
                log.info("定时任务:自动关闭超时订单 {} 个", count);
            }
        } catch (Exception e) {
            log.error("定时关闭超时订单失败", e);
        }
    }
}
