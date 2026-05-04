package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catrescue.catrescueplatform.entity.*;
import com.catrescue.catrescueplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserRepository userRepository;
    private final CatRepository catRepository;
    private final RescueRepository rescueRepository;
    private final AdoptionRepository adoptionRepository;
    private final PostRepository postRepository;
    private final StatisticsSnapshotRepository statisticsSnapshotRepository;

    /**
     * 获取实时统计数据
     */
    public Map<String, Object> getRealTimeStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 调试信息：打印查询结果
        System.out.println("=== 开始统计实时数据 ===");

        // 用户统计
        int totalUsers = Math.toIntExact(userRepository.selectCount(null));
        int activeUsers = getActiveUsersCount();
        int newUsersToday = getNewUsersTodayCount();
        int bannedUsers = getBannedUsersCount();
        int onlineUsers = getOnlineUsersCount();

        System.out.println("用户统计 - 总用户数: " + totalUsers + ", 活跃用户: " + activeUsers + ", 今日新增: " + newUsersToday
                + ", 禁用用户: " + bannedUsers + ", 在线用户: " + onlineUsers);

        statistics.put("totalUsers", totalUsers);
        statistics.put("activeUsers", activeUsers);
        statistics.put("newUsersToday", newUsersToday);
        statistics.put("bannedUsers", bannedUsers);
        statistics.put("onlineUsers", onlineUsers);

        // 猫咪统计
        int totalCats = Math.toIntExact(catRepository.selectCount(null));
        int healthyCats = getCatsByHealthStatus("健康");
        int recoveringCats = getCatsByHealthStatus("恢复中");
        int caringCats = getCatsByStatus("照顾中");
        int adoptableCats = getCatsByStatus("待领养");
        int adoptedCats = getCatsByStatus("已领养");

        System.out.println("猫咪统计 - 总猫咪数: " + totalCats + ", 健康: " + healthyCats + ", 恢复中: " + recoveringCats + ", 照顾中: "
                + caringCats + ", 待领养: " + adoptableCats + ", 已领养: " + adoptedCats);

        statistics.put("totalCats", totalCats);
        statistics.put("healthyCats", healthyCats);
        statistics.put("recoveringCats", recoveringCats);
        statistics.put("caringCats", caringCats);
        statistics.put("adoptableCats", adoptableCats);
        statistics.put("adoptedCats", adoptedCats);

        // 救助统计
        int totalRescues = Math.toIntExact(rescueRepository.selectCount(null));
        int pendingRescues = getRescuesByStatus("待处理");
        int inProgressRescues = getRescuesByStatus("进行中");
        int completedRescues = getRescuesByStatus("已完成");
        int criticalRescues = getRescuesByUrgencyLevel("紧急");
        int highRescues = getRescuesByUrgencyLevel("高");
        int mediumRescues = getRescuesByUrgencyLevel("中");
        int lowRescues = getRescuesByUrgencyLevel("低");

        System.out.println("救助统计 - 总救助数: " + totalRescues + ", 待处理: " + pendingRescues + ", 进行中: " + inProgressRescues
                + ", 已完成: " + completedRescues);
        System.out.println("救助紧急程度 - 紧急: " + criticalRescues + ", 高: " + highRescues + ", 中: " + mediumRescues + ", 低: "
                + lowRescues);

        statistics.put("totalRescues", totalRescues);
        statistics.put("pendingRescues", pendingRescues);
        statistics.put("inProgressRescues", inProgressRescues);
        statistics.put("completedRescues", completedRescues);
        statistics.put("criticalRescues", criticalRescues);
        statistics.put("highRescues", highRescues);
        statistics.put("mediumRescues", mediumRescues);
        statistics.put("lowRescues", lowRescues);

        // 领养申请统计
        int totalAdoptions = Math.toIntExact(adoptionRepository.selectCount(null));
        int pendingAdoptions = getAdoptionsByStatus("待审核");
        int approvedAdoptions = getAdoptionsByStatus("已通过");
        int rejectedAdoptions = getAdoptionsByStatus("已拒绝");

        System.out.println("领养统计 - 总领养数: " + totalAdoptions + ", 待审核: " + pendingAdoptions + ", 已通过: "
                + approvedAdoptions + ", 已拒绝: " + rejectedAdoptions);

        statistics.put("totalAdoptions", totalAdoptions);
        statistics.put("pendingAdoptions", pendingAdoptions);
        statistics.put("approvedAdoptions", approvedAdoptions);
        statistics.put("rejectedAdoptions", rejectedAdoptions);

        // 帖子统计
        int totalPosts = Math.toIntExact(postRepository.selectCount(new QueryWrapper<Post>().eq("is_valid", true)));
        int pendingPosts = getPostsByStatus("待审核");
        int newPostsToday = getNewPostsTodayCount();
        int approvedPosts = getPostsByStatus("已发布");
        int rejectedPosts = getPostsByStatus("已拒绝");

        System.out.println("帖子统计 - 总帖子数: " + totalPosts + ", 待审核: " + pendingPosts + ", 今日新增: " + newPostsToday
                + ", 已发布: " + approvedPosts + ", 已拒绝: " + rejectedPosts);

        statistics.put("totalPosts", totalPosts);
        statistics.put("pendingPosts", pendingPosts);
        statistics.put("newPostsToday", newPostsToday);
        statistics.put("approvedPosts", approvedPosts);
        statistics.put("rejectedPosts", rejectedPosts);

        // 添加默认的趋势数据（最近7天）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        statistics.put("userTrend", getUserTrend(startDate, endDate));
        statistics.put("catTrend", getCatTrend(startDate, endDate));
        statistics.put("rescueTrend", getRescueTrend(startDate, endDate));
        statistics.put("adoptionTrend", getAdoptionTrend(startDate, endDate));
        statistics.put("postTrend", getPostTrend(startDate, endDate));
        statistics.put("activityAnalysis", getActivityAnalysis(startDate, endDate));

        // 添加图表数据
        Map<String, Object> chartData = getChartData(startDate, endDate);
        statistics.putAll(chartData);

        return statistics;
    }

    /**
     * 获取日期区间统计数据
     */
    public Map<String, Object> getDateRangeStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> statistics = new HashMap<>();

        // 添加实时统计数据
        Map<String, Object> realTimeStats = getRealTimeStatistics();
        statistics.putAll(realTimeStats);

        // 添加日期区间限制的统计数据
        statistics.put("newUsers", getNewUsersByDateRange(startDate, endDate));
        statistics.put("newRescues", getNewRescuesByDateRange(startDate, endDate));
        statistics.put("newAdoptions", getNewAdoptionsByDateRange(startDate, endDate));
        statistics.put("newPosts", getNewPostsByDateRange(startDate, endDate));
        statistics.put("newCats", getNewCatsByDateRange(startDate, endDate));
        statistics.put("rejectedCats", getRejectedCatsCount());

        // 用户趋势
        statistics.put("userTrend", getUserTrend(startDate, endDate));

        // 猫咪趋势
        statistics.put("catTrend", getCatTrend(startDate, endDate));

        // 救助趋势
        statistics.put("rescueTrend", getRescueTrend(startDate, endDate));

        // 领养趋势
        statistics.put("adoptionTrend", getAdoptionTrend(startDate, endDate));

        // 帖子趋势
        statistics.put("postTrend", getPostTrend(startDate, endDate));

        // 活跃度分析
        statistics.put("activityAnalysis", getActivityAnalysis(startDate, endDate));

        // 添加图表数据
        Map<String, Object> chartData = getChartData(startDate, endDate);
        statistics.putAll(chartData);

        return statistics;
    }

    /**
     * 获取图表数据
     */
    public Map<String, Object> getChartData(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> charts = new HashMap<>();

        // 饼图数据
        charts.put("userDistribution", getUserDistribution());
        charts.put("catStatusDistribution", getCatStatusDistribution());
        charts.put("rescueStatusDistribution", getRescueStatusDistribution());
        charts.put("postCategoryDistribution", getPostCategoryDistribution());

        // 柱状图数据
        charts.put("dailyActivity", getDailyActivity(startDate, endDate));
        charts.put("weeklyComparison", getWeeklyComparison(startDate, endDate));

        // 折线图数据
        charts.put("trendAnalysis", getTrendAnalysis(startDate, endDate));

        return charts;
    }

    /**
     * 创建每日统计快照
     */
    public void createDailySnapshot() {
        LocalDate today = LocalDate.now();

        // 检查是否已存在今日快照
        QueryWrapper<StatisticsSnapshot> wrapper = new QueryWrapper<>();
        wrapper.eq("snapshot_date", today);
        if (statisticsSnapshotRepository.selectCount(wrapper) > 0) {
            return; // 今日快照已存在
        }

        StatisticsSnapshot snapshot = new StatisticsSnapshot();
        snapshot.setSnapshotDate(today);

        // 设置统计数据
        Map<String, Object> realTimeStats = getRealTimeStatistics();
        snapshot.setTotalUsers((Integer) realTimeStats.get("totalUsers"));
        snapshot.setActiveUsers((Integer) realTimeStats.get("activeUsers"));
        snapshot.setNewUsers((Integer) realTimeStats.get("newUsersToday"));
        snapshot.setBannedUsers((Integer) realTimeStats.get("bannedUsers"));
        snapshot.setOnlineUsers((Integer) realTimeStats.get("onlineUsers"));

        snapshot.setTotalCats((Integer) realTimeStats.get("totalCats"));
        snapshot.setHealthyCats((Integer) realTimeStats.get("healthyCats"));
        snapshot.setRecoveringCats((Integer) realTimeStats.get("recoveringCats"));
        snapshot.setCaringCats((Integer) realTimeStats.get("caringCats"));
        snapshot.setAdoptableCats((Integer) realTimeStats.get("adoptableCats"));
        snapshot.setAdoptedCats((Integer) realTimeStats.get("adoptedCats"));

        snapshot.setTotalRescues((Integer) realTimeStats.get("totalRescues"));
        snapshot.setPendingRescues((Integer) realTimeStats.get("pendingRescues"));
        snapshot.setInProgressRescues((Integer) realTimeStats.get("inProgressRescues"));
        snapshot.setCompletedRescues((Integer) realTimeStats.get("completedRescues"));

        snapshot.setTotalAdoptions((Integer) realTimeStats.get("totalAdoptions"));
        snapshot.setApprovedAdoptions((Integer) realTimeStats.get("approvedAdoptions"));
        snapshot.setRejectedAdoptions((Integer) realTimeStats.get("rejectedAdoptions"));

        snapshot.setTotalPosts((Integer) realTimeStats.get("totalPosts"));
        snapshot.setNewPosts((Integer) realTimeStats.get("newPostsToday"));
        snapshot.setApprovedPosts((Integer) realTimeStats.get("approvedPosts"));
        snapshot.setRejectedPosts((Integer) realTimeStats.get("rejectedPosts"));

        statisticsSnapshotRepository.insert(snapshot);
    }

    // 辅助方法
    private int getActiveUsersCount() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "ACTIVE");
        return Math.toIntExact(userRepository.selectCount(wrapper));
    }

    private int getNewUsersTodayCount() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", LocalDate.now().atStartOfDay());
        return Math.toIntExact(userRepository.selectCount(wrapper));
    }

    private int getBannedUsersCount() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "INACTIVE");
        return Math.toIntExact(userRepository.selectCount(wrapper));
    }

    private int getOnlineUsersCount() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("is_online", 1);
        return Math.toIntExact(userRepository.selectCount(wrapper));
    }

    private int getCatsByHealthStatus(String healthStatus) {
        QueryWrapper<Cat> wrapper = new QueryWrapper<>();
        wrapper.eq("health_status", healthStatus);
        return Math.toIntExact(catRepository.selectCount(wrapper));
    }

    private int getCatsByStatus(String status) {
        QueryWrapper<Cat> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status);
        return Math.toIntExact(catRepository.selectCount(wrapper));
    }

    private int getRescuesByStatus(String status) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();

        // 支持中英文状态值查询
        if ("待处理".equals(status)) {
            wrapper.and(w -> w.eq("status", "待处理").or().eq("status", "PENDING"));
        } else if ("进行中".equals(status)) {
            wrapper.and(w -> w.eq("status", "进行中").or().eq("status", "IN_PROGRESS"));
        } else if ("已完成".equals(status)) {
            wrapper.and(w -> w.eq("status", "已完成").or().eq("status", "COMPLETED"));
        } else {
            wrapper.eq("status", status);
        }

        return Math.toIntExact(rescueRepository.selectCount(wrapper));
    }

    private int getAdoptionsByStatus(String status) {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();

        // 支持中英文状态值查询
        if ("待审核".equals(status)) {
            wrapper.and(w -> w.eq("status", "待审核").or().eq("status", "PENDING").or().eq("status", "UNDER_REVIEW"));
        } else if ("已通过".equals(status)) {
            wrapper.and(w -> w.eq("status", "已通过").or().eq("status", "APPROVED"));
        } else if ("已拒绝".equals(status)) {
            wrapper.and(w -> w.eq("status", "已拒绝").or().eq("status", "REJECTED"));
        } else {
            wrapper.eq("status", status);
        }

        return Math.toIntExact(adoptionRepository.selectCount(wrapper));
    }

    private int getRescuesByUrgencyLevel(String urgencyLevel) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();

        // 支持中英文紧急程度值查询
        if ("紧急".equals(urgencyLevel)) {
            wrapper.and(w -> w.eq("urgency_level", "紧急").or().eq("urgency_level", "CRITICAL"));
        } else if ("高".equals(urgencyLevel)) {
            wrapper.and(w -> w.eq("urgency_level", "高").or().eq("urgency_level", "HIGH"));
        } else if ("中".equals(urgencyLevel)) {
            wrapper.and(w -> w.eq("urgency_level", "中").or().eq("urgency_level", "MEDIUM"));
        } else if ("低".equals(urgencyLevel)) {
            wrapper.and(w -> w.eq("urgency_level", "低").or().eq("urgency_level", "LOW"));
        } else {
            wrapper.eq("urgency_level", urgencyLevel);
        }

        return Math.toIntExact(rescueRepository.selectCount(wrapper));
    }

    private int getPostsByStatus(String status) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();

        // 支持中英文状态值查询
        if ("待审核".equals(status)) {
            wrapper.and(w -> w.eq("status", "待审核").or().eq("status", "PENDING").or().eq("status", "PENDING_REVIEW"));
        } else if ("已发布".equals(status)) {
            wrapper.and(w -> w.eq("status", "已发布").or().eq("status", "PUBLISHED"));
        } else if ("已拒绝".equals(status)) {
            wrapper.and(w -> w.eq("status", "已拒绝").or().eq("status", "REJECTED"));
        } else {
            wrapper.eq("status", status);
        }

        wrapper.eq("is_valid", true);
        return Math.toIntExact(postRepository.selectCount(wrapper));
    }

    private int getNewPostsTodayCount() {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", LocalDate.now().atStartOfDay()).eq("is_valid", true);
        return Math.toIntExact(postRepository.selectCount(wrapper));
    }

    private int getNewUsersByDateRange(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate.atStartOfDay());
        wrapper.le("create_time", endDate.atTime(23, 59, 59));
        return Math.toIntExact(userRepository.selectCount(wrapper));
    }

    private int getNewRescuesByDateRange(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate.atStartOfDay());
        wrapper.le("create_time", endDate.atTime(23, 59, 59));
        return Math.toIntExact(rescueRepository.selectCount(wrapper));
    }

    private int getNewAdoptionsByDateRange(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate.atStartOfDay());
        wrapper.le("create_time", endDate.atTime(23, 59, 59));
        return Math.toIntExact(adoptionRepository.selectCount(wrapper));
    }

    private int getNewPostsByDateRange(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate.atStartOfDay());
        wrapper.le("create_time", endDate.atTime(23, 59, 59));
        wrapper.eq("is_valid", true);
        return Math.toIntExact(postRepository.selectCount(wrapper));
    }

    private int getNewCatsByDateRange(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Cat> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate.atStartOfDay());
        wrapper.le("create_time", endDate.atTime(23, 59, 59));
        return Math.toIntExact(catRepository.selectCount(wrapper));
    }

    private int getRejectedCatsCount() {
        QueryWrapper<Cat> wrapper = new QueryWrapper<>();
        wrapper.eq("audit_status", "REJECTED");
        return Math.toIntExact(catRepository.selectCount(wrapper));
    }

    // 趋势分析方法
    private List<Map<String, Object>> getUserTrend(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> dailyData = new HashMap<>();
            dailyData.put("date", date.toString());

            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("totalUsers", userRepository.selectCount(wrapper));

            QueryWrapper<User> dailyWrapper = new QueryWrapper<>();
            dailyWrapper.ge("create_time", date.atStartOfDay());
            dailyWrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("newUsers", userRepository.selectCount(dailyWrapper));

            trendData.add(dailyData);
        }

        return trendData;
    }

    private List<Map<String, Object>> getCatTrend(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> dailyData = new HashMap<>();
            dailyData.put("date", date.toString());

            QueryWrapper<Cat> wrapper = new QueryWrapper<>();
            wrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("totalCats", catRepository.selectCount(wrapper));

            QueryWrapper<Cat> dailyWrapper = new QueryWrapper<>();
            dailyWrapper.ge("create_time", date.atStartOfDay());
            dailyWrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("newCats", catRepository.selectCount(dailyWrapper));

            trendData.add(dailyData);
        }

        return trendData;
    }

    private List<Map<String, Object>> getRescueTrend(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> dailyData = new HashMap<>();
            dailyData.put("date", date.toString());

            QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
            wrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("totalRescues", rescueRepository.selectCount(wrapper));

            QueryWrapper<Rescue> dailyWrapper = new QueryWrapper<>();
            dailyWrapper.ge("create_time", date.atStartOfDay());
            dailyWrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("newRescues", rescueRepository.selectCount(dailyWrapper));

            trendData.add(dailyData);
        }

        return trendData;
    }

    private List<Map<String, Object>> getAdoptionTrend(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> dailyData = new HashMap<>();
            dailyData.put("date", date.toString());

            QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
            wrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("totalAdoptions", adoptionRepository.selectCount(wrapper));

            QueryWrapper<Adoption> dailyWrapper = new QueryWrapper<>();
            dailyWrapper.ge("create_time", date.atStartOfDay());
            dailyWrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("newAdoptions", adoptionRepository.selectCount(dailyWrapper));

            trendData.add(dailyData);
        }

        return trendData;
    }

    private List<Map<String, Object>> getPostTrend(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> dailyData = new HashMap<>();
            dailyData.put("date", date.toString());

            QueryWrapper<Post> wrapper = new QueryWrapper<>();
            wrapper.le("create_time", date.atTime(23, 59, 59)).eq("is_valid", true);
            dailyData.put("totalPosts", postRepository.selectCount(wrapper));

            QueryWrapper<Post> dailyWrapper = new QueryWrapper<>();
            dailyWrapper.ge("create_time", date.atStartOfDay());
            dailyWrapper.le("create_time", date.atTime(23, 59, 59));
            dailyWrapper.eq("is_valid", true);
            dailyData.put("newPosts", postRepository.selectCount(dailyWrapper));

            trendData.add(dailyData);
        }

        return trendData;
    }

    private Map<String, Object> getActivityAnalysis(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analysis = new HashMap<>();

        // 这里可以添加更复杂的活跃度分析逻辑
        analysis.put("totalActivities", 1000); // 示例数据
        analysis.put("averageDailyActivities", 50);
        analysis.put("peakActivityTime", "14:00-16:00");

        return analysis;
    }

    private Map<String, Object> getUserDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("role", "USER");
        int userCount = Math.toIntExact(userRepository.selectCount(userWrapper));

        userWrapper = new QueryWrapper<>();
        userWrapper.eq("role", "RESCUER");
        int rescuerCount = Math.toIntExact(userRepository.selectCount(userWrapper));

        userWrapper = new QueryWrapper<>();
        userWrapper.eq("role", "ADMIN");
        int adminCount = Math.toIntExact(userRepository.selectCount(userWrapper));

        distribution.put("普通用户", userCount);
        distribution.put("救助者", rescuerCount);
        distribution.put("管理员", adminCount);

        return distribution;
    }

    private Map<String, Object> getCatStatusDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        distribution.put("待领养", getCatsByStatus("待领养"));
        distribution.put("已领养", getCatsByStatus("已领养"));
        distribution.put("照顾中", getCatsByStatus("照顾中"));

        return distribution;
    }

    private Map<String, Object> getRescueStatusDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        distribution.put("待处理", getRescuesByStatus("待处理"));
        distribution.put("进行中", getRescuesByStatus("进行中"));
        distribution.put("已完成", getRescuesByStatus("已完成"));

        return distribution;
    }

    private Map<String, Object> getPostCategoryDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        // 这里需要根据实际的帖子分类数据来统计
        distribution.put("经验分享", 40);
        distribution.put("求助咨询", 30);
        distribution.put("领养故事", 20);
        distribution.put("其他", 10);

        return distribution;
    }

    private List<Map<String, Object>> getDailyActivity(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> activityData = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> dailyData = new HashMap<>();
            dailyData.put("date", date.toString());

            // 每日新增用户数
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.ge("create_time", date.atStartOfDay());
            userWrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("users", userRepository.selectCount(userWrapper));

            // 每日新增帖子数
            QueryWrapper<Post> postWrapper = new QueryWrapper<>();
            postWrapper.ge("create_time", date.atStartOfDay());
            postWrapper.le("create_time", date.atTime(23, 59, 59));
            postWrapper.eq("is_valid", true);
            dailyData.put("posts", postRepository.selectCount(postWrapper));

            // 每日新增救助数
            QueryWrapper<Rescue> rescueWrapper = new QueryWrapper<>();
            rescueWrapper.ge("create_time", date.atStartOfDay());
            rescueWrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("rescues", rescueRepository.selectCount(rescueWrapper));

            // 每日新增领养数
            QueryWrapper<Adoption> adoptionWrapper = new QueryWrapper<>();
            adoptionWrapper.ge("create_time", date.atStartOfDay());
            adoptionWrapper.le("create_time", date.atTime(23, 59, 59));
            dailyData.put("adoptions", adoptionRepository.selectCount(adoptionWrapper));

            activityData.add(dailyData);
        }

        return activityData;
    }

    private Map<String, Object> getWeeklyComparison(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> comparison = new HashMap<>();

        // 这里可以添加周对比数据
        comparison.put("currentWeek", 500);
        comparison.put("lastWeek", 450);
        comparison.put("growthRate", 11.1);

        return comparison;
    }

    private Map<String, Object> getTrendAnalysis(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trend = new HashMap<>();

        // 这里可以添加趋势分析数据
        trend.put("dates", Arrays.asList("03-25", "03-26", "03-27", "03-28", "03-29", "03-30", "03-31"));
        trend.put("users", Arrays.asList(100, 105, 110, 115, 120, 125, 130));
        trend.put("posts", Arrays.asList(50, 55, 60, 65, 70, 75, 80));
        trend.put("adoptions", Arrays.asList(5, 6, 7, 8, 9, 10, 11));

        return trend;
    }
}