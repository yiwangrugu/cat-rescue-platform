package com.catrescue.catrescueplatform.controller;

import com.catrescue.catrescueplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取用户未读通知数量
     * 注意：实际项目中需要从登录上下文获取用户ID
     * 这里暂时使用请求参数传递用户ID
     */
    @GetMapping("/notifications/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount(@RequestParam("userId") Long userId) {
        // 如果没有提供userId，使用默认值1
        if (userId == null) {
            userId = 1L;
        }
        long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 获取帖子未读通知数量
     * 注意：实际项目中需要从登录上下文获取用户ID
     * 这里暂时使用请求参数传递用户ID
     */
    @GetMapping("/posts/{postId}/notifications/unread/count")
    public ResponseEntity<Long> getPostUnreadNotificationCount(
            @PathVariable Long postId,
            @RequestParam("userId") Long userId) {
        // 如果没有提供userId，使用默认值1
        if (userId == null) {
            userId = 1L;
        }
        long count = notificationService.getUnreadNotificationCountByPost(userId, postId);
        return ResponseEntity.ok(count);
    }

    /**
     * 标记帖子通知为已读
     * 注意：实际项目中需要从登录上下文获取用户ID
     * 这里暂时使用请求参数传递用户ID
     */
    @PutMapping("/posts/{postId}/notifications/read")
    public ResponseEntity<?> markPostNotificationsAsRead(
            @PathVariable Long postId,
            @RequestParam("userId") Long userId) {
        // 如果没有提供userId，使用默认值1
        if (userId == null) {
            userId = 1L;
        }
        notificationService.markPostNotificationsAsRead(userId, postId);
        return ResponseEntity.ok().build();
    }
}
