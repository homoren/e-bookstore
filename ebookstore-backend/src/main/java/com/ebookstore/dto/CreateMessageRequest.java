package com.ebookstore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMessageRequest {
    @NotBlank(message = "留言内容不能为空")
    private String content;
}