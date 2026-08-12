package com.ebookstore.service.impl;

import com.ebookstore.common.BusinessException;
import com.ebookstore.dto.LoginRequest;
import com.ebookstore.dto.LoginResponse;
import com.ebookstore.dto.RegisterRequest;
import com.ebookstore.dto.UserInfoDTO;
import com.ebookstore.entity.User;
import com.ebookstore.mapper.UserMapper;
import com.ebookstore.security.LoginAttemptService;
import com.ebookstore.service.UserService;
import com.ebookstore.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final LoginAttemptService loginAttemptService;

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();

        // 防爆破:被锁定则直接拒绝
        if (loginAttemptService.isLocked(username)) {
            long minutes = loginAttemptService.getRemainingLockSeconds(username) / 60 + 1;
            throw new BusinessException("尝试次数过多，账号已临时锁定，请 " + minutes + " 分钟后重试");
        }

        User user = userMapper.findByUsername(username);

        if (user == null) {
            loginAttemptService.loginFailed(username);
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(username);
            throw new BusinessException("用户名或密码错误");
        }

        loginAttemptService.loginSucceeded(username);
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole());

        return new LoginResponse(token, user.getUsername(), user.getRealName(), user.getRole());
    }

    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(1);  // 默认普通会员
        user.setStatus(1); // 默认正常状态

        userMapper.insert(user);
    }

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        return userMapper.findUserInfoById(userId);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userMapper.countByUsername(username) > 0;
    }
}
