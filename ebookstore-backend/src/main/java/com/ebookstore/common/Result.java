package com.ebookstore.common;

import lombok.Data;

/**
 * 统一响应体。code: 200 成功, 400 业务失败, 500 服务器内部错误。
 * success/message/data 字段与前端拦截器约定保持一致。
 */
@Data
public class Result<T> {

    private Integer code;
    private Boolean success;
    private String message;
    private T data;

    public Result(Integer code, Boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, true, "操作成功", data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, true, message, data);
    }

    public static <T> Result<T> ok(String message) {
        return new Result<>(200, true, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(400, false, message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, false, message, null);
    }
}
