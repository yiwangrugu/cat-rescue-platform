package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catrescue.catrescueplatform.entity.Volunteer;
import com.catrescue.catrescueplatform.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 志愿者状态重置服务
 * 每天凌晨自动将忙碌的志愿者状态重置为空闲
 * 
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VolunteerStatusResetService {

    private final VolunteerRepository volunteerRepository;

    // 手动添加log变量，因为Lombok可能没有正确工作
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VolunteerStatusResetService.class);

    /**
     * 每天凌晨2点自动重置志愿者状态
     * 将状态为"忙碌"的志愿者重置为"空闲"
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    @Transactional
    public void resetVolunteerStatus() {
        try {
            QueryWrapper<Volunteer> wrapper = new QueryWrapper<>();
            wrapper.eq("status", "忙碌");

            Long count = volunteerRepository.selectCount(wrapper);

            if (count != null && count > 0) {
                // 批量更新志愿者状态
                Volunteer updateVolunteer = new Volunteer();
                updateVolunteer.setStatus("空闲");
                updateVolunteer.setUpdateTime(LocalDateTime.now());

                int updated = volunteerRepository.update(updateVolunteer, wrapper);

                log.info("志愿者状态重置完成: 共重置 {} 名忙碌志愿者的状态为空闲", updated);
                System.out.println("志愿者状态重置完成: 共重置 " + updated + " 名忙碌志愿者的状态为空闲");
            } else {
                log.info("没有需要重置状态的志愿者");
                System.out.println("没有需要重置状态的志愿者");
            }
        } catch (Exception e) {
            log.error("志愿者状态重置失败", e);
            System.err.println("志愿者状态重置失败: " + e.getMessage());
        }
    }

    /**
     * 手动重置志愿者状态（用于测试和管理）
     */
    @Transactional
    public int manualResetVolunteerStatus() {
        try {
            QueryWrapper<Volunteer> wrapper = new QueryWrapper<>();
            wrapper.eq("status", "忙碌");

            Long count = volunteerRepository.selectCount(wrapper);

            if (count != null && count > 0) {
                // 批量更新志愿者状态
                Volunteer updateVolunteer = new Volunteer();
                updateVolunteer.setStatus("空闲");
                updateVolunteer.setUpdateTime(LocalDateTime.now());

                int updated = volunteerRepository.update(updateVolunteer, wrapper);

                log.info("手动重置志愿者状态完成: 共重置 {} 名忙碌志愿者的状态为空闲", updated);
                System.out.println("手动重置志愿者状态完成: 共重置 " + updated + " 名忙碌志愿者的状态为空闲");
                return updated;
            } else {
                log.info("没有需要重置状态的志愿者");
                System.out.println("没有需要重置状态的志愿者");
                return 0;
            }
        } catch (Exception e) {
            log.error("手动重置志愿者状态失败", e);
            System.err.println("手动重置志愿者状态失败: " + e.getMessage());
            return -1;
        }
    }
}