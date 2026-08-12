package com.ebookstore.common;

/**
 * 业务异常。由 GlobalExceptionHandler 统一转换为 Result.fail(message)。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
