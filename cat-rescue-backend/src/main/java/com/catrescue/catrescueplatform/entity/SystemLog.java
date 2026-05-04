package com.catrescue.catrescueplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统日志实体类
 * 对应数据库表: system_logs
 */
@Data
@TableName("system_logs")
public class SystemLog {

    /** 日志ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，关联用户表 */
    @TableField("user_id")
    private Long userId;

    /** 操作动作 */
    @TableField("action")
    private String action;

    /** 模块名称 */
    @TableField("module")
    private String module;

    /** 操作描述 */
    @TableField("description")
    private String description;

    /** IP地址 */
    @TableField("ip_address")
    private String ipAddress;

    /** 用户代理 */
    @TableField("user_agent")
    private String userAgent;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 关联的用户信息（非数据库字段） */
    @TableField(exist = false)
    private User user;

    // 手动添加getter和setter方法，因为Lombok可能没有正确工作
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}