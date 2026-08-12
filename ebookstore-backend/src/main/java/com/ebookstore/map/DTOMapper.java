package com.ebookstore.map;

import com.ebookstore.dto.AnnouncementDTO;
import com.ebookstore.dto.DailySettlementDTO;
import com.ebookstore.dto.MessageDTO;
import com.ebookstore.dto.OrderDTO;
import com.ebookstore.dto.OrderItemDTO;
import com.ebookstore.dto.PurchaseDTO;
import com.ebookstore.dto.PurchaseItemDTO;
import com.ebookstore.entity.Announcement;
import com.ebookstore.entity.DailySettlement;
import com.ebookstore.entity.Message;
import com.ebookstore.entity.Order;
import com.ebookstore.entity.OrderItem;
import com.ebookstore.entity.Purchase;
import com.ebookstore.entity.PurchaseItem;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 实体 -> DTO 映射,由 MapStruct 在编译期生成实现。
 */
@Mapper(componentModel = "spring")
public interface DTOMapper {

    OrderDTO toOrderDTO(Order order);

    List<OrderItemDTO> toOrderItemDTOs(List<OrderItem> items);

    PurchaseDTO toPurchaseDTO(Purchase purchase);

    List<PurchaseItemDTO> toPurchaseItemDTOs(List<PurchaseItem> items);

    AnnouncementDTO toAnnouncementDTO(Announcement announcement);

    MessageDTO toMessageDTO(Message message);

    DailySettlementDTO toSettlementDTO(DailySettlement settlement);
}
