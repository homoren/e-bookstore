package com.ebookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {
    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}