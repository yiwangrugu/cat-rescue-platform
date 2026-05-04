package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.catrescue.catrescueplatform.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 创建通知
     * 
     * @param notification 通知实体
     * @return 创建的通知
     */
    Notification createNotification(Notification notification);

    /**
     * 获取用户的未读通知数量
     * 
     * @param userId 用户ID
     * @return 未读通知数量
     */
    long getUnreadNotificationCount(Long userId);

    /**
     * 获取用户的通知列表
     * 
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 通知列表
     */
    IPage<Notification> getUserNotifications(Long userId, int page, int size);

    /**
     * 获取用户在特定帖子的未读通知
     * 
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 未读通知数量
     */
    long getUnreadNotificationCountByPost(Long userId, Long postId);

    /**
     * 标记通知为已读
     * 
     * @param notificationId 通知ID
     */
    void markAsRead(Long notificationId);

    /**
     * 标记帖子的所有通知为已读
     * 
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void markPostNotificationsAsRead(Long userId, Long postId);
}
