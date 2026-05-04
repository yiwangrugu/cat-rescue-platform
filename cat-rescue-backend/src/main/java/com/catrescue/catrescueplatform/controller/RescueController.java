package com.catrescue.catrescueplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.catrescue.catrescueplatform.config.BusinessLog;
import com.catrescue.catrescueplatform.entity.Rescue;
import com.catrescue.catrescueplatform.service.RescueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.catrescue.catrescueplatform.config.RescueWebSocketHandler;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rescues")
@RequiredArgsConstructor
public class RescueController {

    private final RescueService rescueService;

    // 发送WebSocket通知的方法
    private void sendRescueUpdateNotification() {
        RescueWebSocketHandler.notifyRescueUpdate();
    }

    @GetMapping
    public ResponseEntity<IPage<Rescue>> getRescues(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String urgencyLevel,
            @RequestParam(required = false) String search) {

        IPage<Rescue> rescues = rescueService.getRescues(page, size, status, urgencyLevel, search);
        return ResponseEntity.ok(rescues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rescue> getRescue(@PathVariable Long id) {
        Rescue rescue = rescueService.getRescueById(id);
        return ResponseEntity.ok(rescue);
    }

    @PostMapping
    @BusinessLog(action = "发布救助需求", module = "救助管理", description = "发布了救助需求：{rescue.title}")
    public ResponseEntity<Rescue> createRescue(@RequestBody Rescue rescue) {
        Rescue createdRescue = rescueService.createRescue(rescue);
        sendRescueUpdateNotification();
        return ResponseEntity.ok(createdRescue);
    }

    @PutMapping("/{id}")
    @BusinessLog(action = "更新救助需求", module = "救助管理", description = "更新了救助需求（ID: {id}）")
    public ResponseEntity<Rescue> updateRescue(@PathVariable Long id, @RequestBody Rescue rescue) {
        rescue.setId(id);
        Rescue updatedRescue = rescueService.updateRescue(rescue);
        sendRescueUpdateNotification();
        return ResponseEntity.ok(updatedRescue);
    }

    @DeleteMapping("/{id}")
    @BusinessLog(action = "删除救助需求", module = "救助管理", description = "删除了救助需求（ID: {id}）")
    public ResponseEntity<?> deleteRescue(@PathVariable Long id) {
        rescueService.deleteRescue(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<List<Rescue>> getRescuesByReporter(@PathVariable Long reporterId) {
        List<Rescue> rescues = rescueService.getRescuesByReporter(reporterId);
        return ResponseEntity.ok(rescues);
    }

    @GetMapping("/rescuer/{rescuerId}")
    public ResponseEntity<List<Rescue>> getRescuesByRescuer(@PathVariable Long rescuerId) {
        List<Rescue> rescues = rescueService.getRescuesByRescuer(rescuerId);
        return ResponseEntity.ok(rescues);
    }

    @PostMapping("/{id}/take")
    @BusinessLog(action = "接单救助任务", module = "救助管理", description = "接单了救助任务（ID: {id}）")
    public ResponseEntity<Rescue> takeRescue(@PathVariable Long id, @RequestBody Long rescuerId) {
        Rescue rescue = rescueService.takeRescue(id, rescuerId);
        sendRescueUpdateNotification();
        return ResponseEntity.ok(rescue);
    }

    @PostMapping("/{id}/take-with-volunteer")
    @BusinessLog(action = "接单救助任务", module = "救助管理", description = "接单了救助任务（ID: {id}）")
    public ResponseEntity<Rescue> takeRescueWithVolunteer(@PathVariable Long id,
            @RequestBody com.catrescue.catrescueplatform.dto.TakeRescueRequest request) {
        Rescue rescue = rescueService.takeRescueWithVolunteer(id, request);
        sendRescueUpdateNotification();
        return ResponseEntity.ok(rescue);
    }

    @PostMapping("/{id}/complete")
    @BusinessLog(action = "完成救助任务", module = "救助管理", description = "完成了救助任务（ID: {id}）")
    public ResponseEntity<Rescue> completeRescue(@PathVariable Long id,
            @RequestParam(value = "rescuerId", required = false) String rescuerIdStr) {
        Long rescuerId = null;
        if (rescuerIdStr != null && !rescuerIdStr.trim().isEmpty()) {
            try {
                rescuerId = Long.parseLong(rescuerIdStr);
            } catch (NumberFormatException e) {
                System.err.println("负责人ID格式错误: " + rescuerIdStr);
            }
        }
        Rescue rescue = rescueService.completeRescue(id, rescuerId);
        sendRescueUpdateNotification();
        return ResponseEntity.ok(rescue);
    }

    @PostMapping(value = "/{id}/complete-with-images", consumes = "multipart/form-data")
    @BusinessLog(action = "完成救助任务", module = "救助管理", description = "完成了救助任务（ID: {id}）")
    public ResponseEntity<Rescue> completeRescueWithImages(@PathVariable Long id,
            @RequestParam(value = "rescueLogImages", required = false) List<org.springframework.web.multipart.MultipartFile> rescueLogImages,
            @RequestParam(value = "rescuerId", required = false) String rescuerIdStr) {
        Long rescuerId = null;
        if (rescuerIdStr != null && !rescuerIdStr.trim().isEmpty()) {
            try {
                rescuerId = Long.parseLong(rescuerIdStr);
            } catch (NumberFormatException e) {
                System.err.println("负责人ID格式错误: " + rescuerIdStr);
            }
        }
        Rescue rescue = rescueService.completeRescueWithImages(id, rescueLogImages, rescuerId);
        sendRescueUpdateNotification();
        return ResponseEntity.ok(rescue);
    }

    /**
     * 获取我的救助任务
     */
    @GetMapping("/my")
    public ResponseEntity<IPage<Rescue>> getMyRescues(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long rescuerId) {

        try {
            // 如果指定了救助者ID，获取该救助者的救助任务
            if (rescuerId != null) {
                IPage<Rescue> rescues = rescueService.getRescuesByRescuerWithPaging(page, size, rescuerId);
                return ResponseEntity.ok(rescues);
            } else {
                // 如果没有指定救助者ID，返回所有救助任务
                IPage<Rescue> rescues = rescueService.getRescues(page, size, null, null, null);
                return ResponseEntity.ok(rescues);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取救助统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRescueStatistics(
            @RequestParam(required = false) Long rescuerId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            java.time.LocalDateTime startDateTime = null;
            java.time.LocalDateTime endDateTime = null;

            // 解析日期时间参数
            if (startTime != null && !startTime.isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime);
            }

            Map<String, Object> statistics = rescueService.getRescueStatisticsWithDetails(rescuerId, startDateTime,
                    endDateTime);

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取救助统计信息失败: " + e.getMessage()));
        }
    }
}