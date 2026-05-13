package com.ebookstore.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartRequest {
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}