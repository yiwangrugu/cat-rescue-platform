package com.catrescue.catrescueplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 统计快照实体类
 * 对应数据库表: statistics_snapshot
 */
@Data
@TableName("statistics_snapshot")
public class StatisticsSnapshot {

    /** 快照ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 快照日期 */
    @TableField("snapshot_date")
    private LocalDate snapshotDate;

    // 用户统计
    @TableField("total_users")
    private Integer totalUsers;

    @TableField("active_users")
    private Integer activeUsers;

    @TableField("new_users")
    private Integer newUsers;

    @TableField("banned_users")
    private Integer bannedUsers;

    @TableField("online_users")
    private Integer onlineUsers;

    // 猫咪统计
    @TableField("total_cats")
    private Integer totalCats;

    @TableField("healthy_cats")
    private Integer healthyCats;

    @TableField("recovering_cats")
    private Integer recoveringCats;

    @TableField("caring_cats")
    private Integer caringCats;

    @TableField("adoptable_cats")
    private Integer adoptableCats;

    @TableField("adopted_cats")
    private Integer adoptedCats;

    // 救助统计
    @TableField("total_rescues")
    private Integer totalRescues;

    @TableField("pending_rescues")
    private Integer pendingRescues;

    @TableField("in_progress_rescues")
    private Integer inProgressRescues;

    @TableField("completed_rescues")
    private Integer completedRescues;

    // 领养申请统计
    @TableField("total_adoptions")
    private Integer totalAdoptions;

    @TableField("approved_adoptions")
    private Integer approvedAdoptions;

    @TableField("rejected_adoptions")
    private Integer rejectedAdoptions;

    // 帖子统计
    @TableField("total_posts")
    private Integer totalPosts;

    @TableField("new_posts")
    private Integer newPosts;

    @TableField("approved_posts")
    private Integer approvedPosts;

    @TableField("rejected_posts")
    private Integer rejectedPosts;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 手动添加getter和setter方法，因为Lombok可能没有正确工作
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Integer getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Integer getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(Integer newUsers) {
        this.newUsers = newUsers;
    }

    public Integer getBannedUsers() {
        return bannedUsers;
    }

    public void setBannedUsers(Integer bannedUsers) {
        this.bannedUsers = bannedUsers;
    }

    public Integer getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(Integer onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public Integer getTotalCats() {
        return totalCats;
    }

    public void setTotalCats(Integer totalCats) {
        this.totalCats = totalCats;
    }

    public Integer getHealthyCats() {
        return healthyCats;
    }

    public void setHealthyCats(Integer healthyCats) {
        this.healthyCats = healthyCats;
    }

    public Integer getRecoveringCats() {
        return recoveringCats;
    }

    public void setRecoveringCats(Integer recoveringCats) {
        this.recoveringCats = recoveringCats;
    }

    public Integer getCaringCats() {
        return caringCats;
    }

    public void setCaringCats(Integer caringCats) {
        this.caringCats = caringCats;
    }

    public Integer getAdoptableCats() {
        return adoptableCats;
    }

    public void setAdoptableCats(Integer adoptableCats) {
        this.adoptableCats = adoptableCats;
    }

    public Integer getAdoptedCats() {
        return adoptedCats;
    }

    public void setAdoptedCats(Integer adoptedCats) {
        this.adoptedCats = adoptedCats;
    }

    public Integer getTotalRescues() {
        return totalRescues;
    }

    public void setTotalRescues(Integer totalRescues) {
        this.totalRescues = totalRescues;
    }

    public Integer getPendingRescues() {
        return pendingRescues;
    }

    public void setPendingRescues(Integer pendingRescues) {
        this.pendingRescues = pendingRescues;
    }

    public Integer getInProgressRescues() {
        return inProgressRescues;
    }

    public void setInProgressRescues(Integer inProgressRescues) {
        this.inProgressRescues = inProgressRescues;
    }

    public Integer getCompletedRescues() {
        return completedRescues;
    }

    public void setCompletedRescues(Integer completedRescues) {
        this.completedRescues = completedRescues;
    }

    public Integer getTotalAdoptions() {
        return totalAdoptions;
    }

    public void setTotalAdoptions(Integer totalAdoptions) {
        this.totalAdoptions = totalAdoptions;
    }

    public Integer getApprovedAdoptions() {
        return approvedAdoptions;
    }

    public void setApprovedAdoptions(Integer approvedAdoptions) {
        this.approvedAdoptions = approvedAdoptions;
    }

    public Integer getRejectedAdoptions() {
        return rejectedAdoptions;
    }

    public void setRejectedAdoptions(Integer rejectedAdoptions) {
        this.rejectedAdoptions = rejectedAdoptions;
    }

    public Integer getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Integer totalPosts) {
        this.totalPosts = totalPosts;
    }

    public Integer getNewPosts() {
        return newPosts;
    }

    public void setNewPosts(Integer newPosts) {
        this.newPosts = newPosts;
    }

    public Integer getApprovedPosts() {
        return approvedPosts;
    }

    public void setApprovedPosts(Integer approvedPosts) {
        this.approvedPosts = approvedPosts;
    }

    public Integer getRejectedPosts() {
        return rejectedPosts;
    }

    public void setRejectedPosts(Integer rejectedPosts) {
        this.rejectedPosts = rejectedPosts;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}