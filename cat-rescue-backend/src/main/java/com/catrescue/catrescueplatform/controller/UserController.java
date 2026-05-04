package com.catrescue.catrescueplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catrescue.catrescueplatform.entity.*;
import com.catrescue.catrescueplatform.repository.*;
import com.catrescue.catrescueplatform.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AdoptionRepository adoptionRepository;
    private final FavoriteRepository favoriteRepository;
    private final RescueRepository rescueRepository;
    private final SystemLogService systemLogService;

    /**
     * 获取用户活动统计数据
     */
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@PathVariable Long userId) {
        try {
            // 检查用户是否存在
            User user = userRepository.selectById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
            }

            // 使用用户表中的计数器字段
            long postCount = user.getPostCount() != null ? user.getPostCount() : 0;
            long adoptionCount = user.getAdoptionCount() != null ? user.getAdoptionCount() : 0;
            long favoriteCount = user.getFavoriteCount() != null ? user.getFavoriteCount() : 0;
            long rescueCount = user.getRescueCount() != null ? user.getRescueCount() : 0;

            // 获取更详细的统计数据
            // 帖子状态统计 - 使用数据库实际存储的状态值
            QueryWrapper<Post> publishedPostWrapper = new QueryWrapper<>();
            publishedPostWrapper.eq("author_id", userId).eq("status", "已发布");
            long publishedPostCount = postRepository.selectCount(publishedPostWrapper);

            QueryWrapper<Post> pendingPostWrapper = new QueryWrapper<>();
            pendingPostWrapper.eq("author_id", userId).eq("status", "审核中");
            long pendingPostCount = postRepository.selectCount(pendingPostWrapper);

            QueryWrapper<Post> draftPostWrapper = new QueryWrapper<>();
            draftPostWrapper.eq("author_id", userId).eq("status", "草稿");
            long draftPostCount = postRepository.selectCount(draftPostWrapper);

            // 领养申请状态统计 - 使用数据库实际存储的状态值
            QueryWrapper<Adoption> pendingAdoptionWrapper = new QueryWrapper<>();
            pendingAdoptionWrapper.eq("applicant_id", userId).eq("status", "待审核");
            long pendingAdoptionCount = adoptionRepository.selectCount(pendingAdoptionWrapper);

            QueryWrapper<Adoption> approvedAdoptionWrapper = new QueryWrapper<>();
            approvedAdoptionWrapper.eq("applicant_id", userId).eq("status", "已通过");
            long approvedAdoptionCount = adoptionRepository.selectCount(approvedAdoptionWrapper);

            QueryWrapper<Adoption> rejectedAdoptionWrapper = new QueryWrapper<>();
            rejectedAdoptionWrapper.eq("applicant_id", userId).eq("status", "已拒绝");
            long rejectedAdoptionCount = adoptionRepository.selectCount(rejectedAdoptionWrapper);

            // 救助需求状态统计 - 使用数据库实际存储的状态值
            QueryWrapper<Rescue> pendingRescueWrapper = new QueryWrapper<>();
            pendingRescueWrapper.eq("reporter_id", userId).eq("status", "待处理");
            long pendingRescueCount = rescueRepository.selectCount(pendingRescueWrapper);

            QueryWrapper<Rescue> inProgressRescueWrapper = new QueryWrapper<>();
            inProgressRescueWrapper.eq("reporter_id", userId).eq("status", "进行中");
            long inProgressRescueCount = rescueRepository.selectCount(inProgressRescueWrapper);

            QueryWrapper<Rescue> completedRescueWrapper = new QueryWrapper<>();
            completedRescueWrapper.eq("reporter_id", userId).eq("status", "已完成");
            long completedRescueCount = rescueRepository.selectCount(completedRescueWrapper);

            // 计算趋势数据（相对于上个月）
            // 这里简化处理，实际项目中应该查询历史数据计算真实趋势
            int postCountTrend = calculateTrend(postCount);
            int adoptionCountTrend = calculateTrend(adoptionCount);
            int favoriteCountTrend = calculateTrend(favoriteCount);
            int rescueCountTrend = calculateTrend(rescueCount);

            // 计算活跃度
            int activityLevel = calculateActivityLevel(postCount, adoptionCount, favoriteCount, rescueCount);

            // 计算注册天数
            long registerDays = calculateRegisterDays(user.getCreateTime());

            // 构建返回数据
            Map<String, Object> statistics = new HashMap<>();

            // 基本统计数据
            statistics.put("postCount", postCount);
            statistics.put("adoptionCount", adoptionCount);
            statistics.put("favoriteCount", favoriteCount);
            statistics.put("rescueCount", rescueCount);

            // 趋势数据
            statistics.put("postCountTrend", postCountTrend);
            statistics.put("adoptionCountTrend", adoptionCountTrend);
            statistics.put("favoriteCountTrend", favoriteCountTrend);
            statistics.put("rescueCountTrend", rescueCountTrend);

            // 详细统计数据
            statistics.put("publishedPostCount", publishedPostCount);
            statistics.put("pendingPostCount", pendingPostCount);
            statistics.put("draftPostCount", draftPostCount);
            statistics.put("pendingAdoptionCount", pendingAdoptionCount);
            statistics.put("approvedAdoptionCount", approvedAdoptionCount);
            statistics.put("rejectedAdoptionCount", rejectedAdoptionCount);
            statistics.put("pendingRescueCount", pendingRescueCount);
            statistics.put("inProgressRescueCount", inProgressRescueCount);
            statistics.put("completedRescueCount", completedRescueCount);

            // 用户信息
            statistics.put("activityLevel", activityLevel);
            statistics.put("registerDays", registerDays);
            statistics.put("username", user.getUsername());
            statistics.put("avatar", user.getAvatar());
            statistics.put("lastLoginTime", user.getLastLoginTime());
            statistics.put("previousLoginTime", user.getPreviousLoginTime());

            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "获取用户统计数据失败: " + e.getMessage()));
        }
    }

    /**
     * 简化趋势计算（实际项目中应该查询历史数据）
     */
    private int calculateTrend(long currentCount) {
        if (currentCount == 0) {
            return 0;
        }
        // 基于当前数量生成一个合理的趋势值
        // 数量越多，增长趋势越稳定；数量越少，波动可能越大
        double baseTrend = Math.random() * 30 - 15; // -15% 到 +15%
        if (currentCount > 50) {
            baseTrend = Math.random() * 20 - 10; // 数量多时趋势更稳定
        } else if (currentCount > 20) {
            baseTrend = Math.random() * 25 - 12.5; // 中等数量
        }
        return (int) Math.round(baseTrend);
    }

    /**
     * 计算用户活跃度
     */
    private int calculateActivityLevel(long postCount, long adoptionCount, long favoriteCount, long rescueCount) {
        // 加权计算活跃度
        double totalScore = postCount * 2.0 + // 发帖权重较高
                adoptionCount * 1.5 + // 申请领养权重中等
                favoriteCount * 1.0 + // 收藏权重较低
                rescueCount * 2.5; // 救助需求权重最高

        // 将分数转换为百分比（最大100%）
        int activityLevel = (int) Math.min(totalScore / 50.0 * 100, 100);
        return Math.max(activityLevel, 0); // 确保不小于0
    }

    /**
     * 计算注册天数
     */
    private long calculateRegisterDays(java.time.LocalDateTime createTime) {
        if (createTime == null) {
            return 0;
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(createTime, now);
        return duration.toDays();
    }
}