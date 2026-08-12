package com.ebookstore.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录防爆破:连续失败超过阈值后,对该用户名临时锁定。
 * 生产可用 Redis 替代内存 Map(多实例共享),这里为单体部署的内存实现。
 */
@Service
public class LoginAttemptService {

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.lock-minutes:15}")
    private long lockMinutes;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    private record Attempt(int count, LocalDateTime lockedUntil) {}

    public boolean isLocked(String username) {
        Attempt a = attempts.get(username);
        return a != null && a.lockedUntil() != null && LocalDateTime.now().isBefore(a.lockedUntil());
    }

    public void loginFailed(String username) {
        Attempt a = attempts.get(username);
        int count = (a == null ? 0 : a.count()) + 1;
        if (count >= maxAttempts) {
            attempts.put(username, new Attempt(count, LocalDateTime.now().plusMinutes(lockMinutes)));
        } else {
            attempts.put(username, new Attempt(count, null));
        }
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    /** 剩余锁定秒数,供前端提示 */
    public long getRemainingLockSeconds(String username) {
        Attempt a = attempts.get(username);
        if (a != null && a.lockedUntil() != null) {
            long secs = Duration.between(LocalDateTime.now(), a.lockedUntil()).getSeconds();
            return Math.max(secs, 0);
        }
        return 0;
    }
}
