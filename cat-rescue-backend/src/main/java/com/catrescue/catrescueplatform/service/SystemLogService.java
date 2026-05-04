package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catrescue.catrescueplatform.entity.SystemLog;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.repository.SystemLogRepository;
import com.catrescue.catrescueplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;

    /**
     * 记录系统日志
     */
    public void log(String action, String module, String description, HttpServletRequest request) {
        SystemLog log = new SystemLog();

        // 获取当前用户
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                log.setUserId(((User) principal).getId());
            } else if (principal instanceof String) {
                String username = (String) principal;
                User user = userRepository.findByUsername(username);
                if (user != null) {
                    log.setUserId(user.getId());
                }
            }
        }

        log.setAction(action);
        log.setModule(module);
        log.setDescription(description);

        // 获取请求信息
        if (request != null) {
            log.setIpAddress(getClientIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        systemLogRepository.insert(log);
    }

    /**
     * 获取系统日志列表
     */
    public IPage<SystemLog> getLogs(int page, int size, String module, String action, LocalDate startDate,
            LocalDate endDate) {
        QueryWrapper<SystemLog> wrapper = new QueryWrapper<>();

        if (module != null && !module.isEmpty()) {
            wrapper.eq("module", module);
        }

        if (action != null && !action.isEmpty()) {
            wrapper.eq("action", action);
        }

        if (startDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay());
        }

        if (endDate != null) {
            wrapper.le("create_time", endDate.atTime(23, 59, 59));
        }

        wrapper.orderByDesc("create_time");

        IPage<SystemLog> logPage = systemLogRepository.selectPage(new Page<>(page, size), wrapper);

        // 填充用户信息
        if (logPage.getRecords() != null) {
            for (SystemLog log : logPage.getRecords()) {
                if (log.getUserId() != null) {
                    User user = userRepository.selectById(log.getUserId());
                    if (user != null) {
                        log.setUser(user);
                    }
                }
            }
        }

        return logPage;
    }

    /**
     * 获取日志统计信息
     */
    public Map<String, Object> getLogStatistics(LocalDate startDate, LocalDate endDate) {
        System.out.println("=== getLogStatistics 开始 ===");
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);

        QueryWrapper<SystemLog> totalWrapper = new QueryWrapper<>();

        if (startDate != null) {
            totalWrapper.ge("create_time", startDate.atStartOfDay());
        }

        if (endDate != null) {
            totalWrapper.le("create_time", endDate.atTime(23, 59, 59));
        }

        long totalLogs = systemLogRepository.selectCount(totalWrapper);
        System.out.println("总日志数: " + totalLogs);

        QueryWrapper<SystemLog> allLogsWrapper = new QueryWrapper<>();
        if (startDate != null) {
            allLogsWrapper.ge("create_time", startDate.atStartOfDay());
        }
        if (endDate != null) {
            allLogsWrapper.le("create_time", endDate.atTime(23, 59, 59));
        }
        allLogsWrapper.isNotNull("user_id");
        List<SystemLog> allLogs = systemLogRepository.selectList(allLogsWrapper);
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (SystemLog log : allLogs) {
            if (log.getUserId() != null) {
                userIds.add(log.getUserId());
            }
        }
        long activeUsersCount = userIds.size();
        System.out.println("活跃用户数: " + activeUsersCount);

        QueryWrapper<SystemLog> moduleWrapper = new QueryWrapper<>();
        if (startDate != null) {
            moduleWrapper.ge("create_time", startDate.atStartOfDay());
        }
        if (endDate != null) {
            moduleWrapper.le("create_time", endDate.atTime(23, 59, 59));
        }
        moduleWrapper.groupBy("module");
        moduleWrapper.select("module, COUNT(*) as count");
        List<Map<String, Object>> moduleStats = systemLogRepository.selectMaps(moduleWrapper);
        System.out.println("模块统计: " + moduleStats);

        QueryWrapper<SystemLog> actionWrapper = new QueryWrapper<>();
        if (startDate != null) {
            actionWrapper.ge("create_time", startDate.atStartOfDay());
        }
        if (endDate != null) {
            actionWrapper.le("create_time", endDate.atTime(23, 59, 59));
        }
        actionWrapper.groupBy("action");
        actionWrapper.select("action, COUNT(*) as count");
        List<Map<String, Object>> actionStats = systemLogRepository.selectMaps(actionWrapper);
        System.out.println("操作统计: " + actionStats);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", totalLogs);
        result.put("activeUsers", activeUsersCount);
        result.put("moduleStats", moduleStats);
        result.put("actionStats", actionStats);

        System.out.println("返回结果: " + result);
        System.out.println("=== getLogStatistics 结束 ===");

        return result;
    }

    /**
     * 清理过期日志
     */
    public void cleanOldLogs(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        QueryWrapper<SystemLog> wrapper = new QueryWrapper<>();
        wrapper.lt("create_time", cutoffDate);

        systemLogRepository.delete(wrapper);
    }

    /**
     * 清理不良日志（如 BaseMapper 相关日志）
     */
    public int cleanBadLogs() {
        QueryWrapper<SystemLog> wrapper = new QueryWrapper<>();
        wrapper.like("description", "BaseMapper");
        wrapper.or().like("description", "耗时");

        return systemLogRepository.delete(wrapper);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 对于多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}