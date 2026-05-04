package com.catrescue.catrescueplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.catrescue.catrescueplatform.entity.SystemLog;
import com.catrescue.catrescueplatform.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class SystemLogController {

    private final SystemLogService systemLogService;

    /**
     * 获取系统日志列表
     */
    @GetMapping
    public ResponseEntity<IPage<SystemLog>> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        IPage<SystemLog> logs = systemLogService.getLogs(page, size, module, action, startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取日志统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getLogStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> statistics = systemLogService.getLogStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 清理过期日志
     */
    @DeleteMapping("/clean")
    public ResponseEntity<String> cleanOldLogs(@RequestParam(defaultValue = "30") int days) {
        systemLogService.cleanOldLogs(days);
        return ResponseEntity.ok("过期日志清理完成");
    }

    /**
     * 清理不良日志（如 BaseMapper 相关日志）
     */
    @DeleteMapping("/clean-bad")
    public ResponseEntity<Map<String, Object>> cleanBadLogs() {
        int deleted = systemLogService.cleanBadLogs();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("deleted", deleted);
        result.put("message", "已清理 " + deleted + " 条不良日志");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取可用的模块列表
     */
    @GetMapping("/modules")
    public ResponseEntity<String[]> getAvailableModules() {
        String[] modules = {
                "用户管理", "猫咪管理", "救助管理", "领养管理",
                "帖子管理", "系统设置", "数据统计", "系统日志"
        };
        return ResponseEntity.ok(modules);
    }

    /**
     * 获取可用的操作列表
     */
    @GetMapping("/actions")
    public ResponseEntity<String[]> getAvailableActions() {
        String[] actions = {
                "登录", "登出", "创建", "更新", "删除",
                "审核", "导出", "导入", "备份", "恢复"
        };
        return ResponseEntity.ok(actions);
    }
}