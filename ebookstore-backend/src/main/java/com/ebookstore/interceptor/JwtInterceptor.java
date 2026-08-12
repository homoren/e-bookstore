// JwtInterceptor.java
package com.ebookstore.interceptor;

import com.ebookstore.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final int ROLE_ADMIN = 2;

    private final JwtUtils jwtUtils;

    public JwtInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, 401, "未提供认证令牌");
            return false;
        }

        String token = authHeader.substring(7);

        if (!jwtUtils.validateToken(token)) {
            writeError(response, 401, "Token无效或已过期");
            return false;
        }

        // 将用户信息存入 request 属性，供后续使用
        Integer role = jwtUtils.getRoleFromToken(token);
        request.setAttribute("userId", jwtUtils.getUserIdFromToken(token));
        request.setAttribute("username", jwtUtils.getUsernameFromToken(token));
        request.setAttribute("role", role);

        // 管理员接口集中校验角色
        if (request.getRequestURI().startsWith("/api/admin/") && role != ROLE_ADMIN) {
            writeError(response, 403, "需要店主权限");
            return false;
        }

        return true;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}
