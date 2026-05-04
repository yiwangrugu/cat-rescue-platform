package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catrescue.catrescueplatform.dto.LoginRequest;
import com.catrescue.catrescueplatform.dto.RegisterRequest;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        if (userRepository.selectCount(new QueryWrapper<User>().eq("username", request.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.selectCount(new QueryWrapper<User>().eq("email", request.getEmail())) > 0) {
                throw new RuntimeException("邮箱已被注册");
            }
        }

        if (userRepository.selectCount(new QueryWrapper<User>().eq("phone", request.getPhone())) > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setLocation(request.getLocation());
        user.setIntroduction("该用户似乎什么也没留下~");
        user.setRole("USER");
        user.setStatus("ACTIVE");

        userRepository.insert(user);
        return user;
    }

    public User login(LoginRequest request) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", request.getPhone())
                .last("LIMIT 1");
        User user = userRepository.selectOne(wrapper);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("手机号或密码错误");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            // 检查是否到达解禁时间
            if (user.getBanEndTime() != null && LocalDateTime.now().isAfter(user.getBanEndTime())) {
                // 自动解禁用户
                user.setStatus("ACTIVE");
                user.setBanEndTime(null);
                userRepository.updateById(user);
            } else {
                // 构建详细的禁用信息
                String errorMessage = "账户已被禁用";
                if (user.getBanEndTime() != null) {
                    errorMessage += "|" + user.getBanEndTime().toString();
                }
                throw new RuntimeException(errorMessage);
            }
        }

        // 更新登录时间记录
        // 将当前最近登录时间保存为上一次登录时间
        user.setPreviousLoginTime(user.getLastLoginTime());
        // 设置新的最近登录时间为当前时间
        user.setLastLoginTime(LocalDateTime.now());
        // 设置用户在线状态为在线
        user.setIsOnline(1);
        userRepository.updateById(user);

        return user;
    }

    /**
     * 更新用户在线状态
     * 
     * @param userId   用户ID
     * @param isOnline 在线状态：1-在线，0-离线
     */
    public void updateOnlineStatus(Long userId, Integer isOnline) {
        User user = userRepository.selectById(userId);
        if (user != null) {
            user.setIsOnline(isOnline);
            userRepository.updateById(user);
        }
    }

    /**
     * 将超过指定时间未活动的用户设为离线
     * 
     * @param hours 小时数
     */
    public void setInactiveUsersOffline(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("is_online", 1);
        wrapper.le("last_login_time", cutoffTime);

        User updateUser = new User();
        updateUser.setIsOnline(0);
        userRepository.update(updateUser, wrapper);
    }

    public void logout(Long userId) {
        updateOnlineStatus(userId, 0);
    }

    public User findById(Long id) {
        return userRepository.selectById(id);
    }

    public User findByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username)
                .last("LIMIT 1");
        return userRepository.selectOne(wrapper);
    }

    public User updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        userRepository.updateById(user);
        return user;
    }

    public User updateProfile(Long userId, Map<String, Object> updates) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (updates.containsKey("username")) {
            user.setUsername((String) updates.get("username"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("gender")) {
            user.setGender((String) updates.get("gender"));
        }
        if (updates.containsKey("location")) {
            user.setLocation((String) updates.get("location"));
        }
        if (updates.containsKey("introduction")) {
            String introduction = (String) updates.get("introduction");
            if (introduction == null || introduction.trim().isEmpty()) {
                user.setIntroduction("该用户似乎什么也没留下~");
            } else {
                user.setIntroduction(introduction);
            }
        }

        userRepository.updateById(user);
        return user;
    }

    /**
     * 获取UserRepository实例，供其他服务调用
     */
    public UserRepository getUserRepository() {
        return userRepository;
    }
}