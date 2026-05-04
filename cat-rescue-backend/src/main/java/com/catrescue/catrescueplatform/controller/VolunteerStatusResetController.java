package com.catrescue.catrescueplatform.controller;

import com.catrescue.catrescueplatform.service.VolunteerStatusResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 志愿者状态重置控制器
 * 提供手动重置志愿者状态的接口
 * 
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class VolunteerStatusResetController {

    private final VolunteerStatusResetService volunteerStatusResetService;

    /**
     * 手动重置志愿者状态
     * 将所有状态为"忙碌"的志愿者重置为"空闲"
     */
    @PostMapping("/volunteers/reset-status")
    public ResponseEntity<Map<String, Object>> resetVolunteerStatus() {
        try {
            int updated = volunteerStatusResetService.manualResetVolunteerStatus();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("updated", updated);
            result.put("message", updated > 0 ? 
                "成功重置 " + updated + " 名忙碌志愿者的状态为空闲" : 
                "没有需要重置状态的志愿者");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "重置志愿者状态失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
}