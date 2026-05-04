package com.catrescue.catrescueplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catrescue.catrescueplatform.entity.Notification;
import com.catrescue.catrescueplatform.mapper.NotificationMapper;
import com.catrescue.catrescueplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知服务实现类
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public Notification createNotification(Notification notification) {
        notification.setIsRead(false);
        save(notification);
        return notification;
    }

    @Override
    public long getUnreadNotificationCount(Long userId) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("is_read", false);
        return count(queryWrapper);
    }

    @Override
    public IPage<Notification> getUserNotifications(Long userId, int page, int size) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");

        Page<Notification> pageParam = new Page<>(page, size);
        return page(pageParam, queryWrapper);
    }

    @Override
    public long getUnreadNotificationCountByPost(Long userId, Long postId) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("post_id", postId)
                .eq("is_read", false);
        return count(queryWrapper);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification != null) {
            notification.setIsRead(true);
            updateById(notification);
        }
    }

    @Override
    @Transactional
    public void markPostNotificationsAsRead(Long userId, Long postId) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("post_id", postId);

        Notification notification = new Notification();
        notification.setIsRead(true);
        update(notification, queryWrapper);
    }
}
