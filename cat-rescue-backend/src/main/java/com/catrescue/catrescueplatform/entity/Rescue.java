package com.catrescue.catrescueplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 救助需求实体类
 * 对应数据库表: rescues
 * 
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@TableName("rescues")
public class Rescue {

    /** 救助需求ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 救助标题 */
    @TableField("title")
    private String title;

    /** 救助描述 */
    @TableField("description")
    private String description;

    /** 救助地点 */
    @TableField("location")
    private String location;

    /** 猫咪状况描述 */
    @TableField("cat_condition")
    private String catCondition;

    /** 紧急程度：紧急/高/中/低 */
    @TableField("urgency_level")
    private String urgencyLevel;

    /** 联系电话 */
    @TableField("contact_phone")
    private String contactPhone;

    /** 报告人ID，关联用户表 */
    @TableField("reporter_id")
    private Long reporterId;

    /** 负责人ID，关联用户表（处理本次救助的账号） */
    @TableField(exist = false)
    private Long rescuerId;

    /** 接单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("accept_time")
    private LocalDateTime acceptTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /** 志愿者姓名 */
    @TableField("volunteer_name")
    private String volunteerName;

    /** 志愿者电话 */
    @TableField("volunteer_phone")
    private String volunteerPhone;

    /** 救助状态：待处理/进行中/已完成 */
    @TableField("status")
    private String status;

    /** 相关图片URL，多个图片用逗号分隔 */
    @TableField("images")
    private String images;

    /** 救援日志图片URL，多个图片用逗号分隔 */
    @TableField("rescue_log_images")
    private String rescueLogImages;

    /** 创建时间，自动填充，JSON格式化 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，自动填充，JSON格式化 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 报告人用户名，非数据库字段 */
    @TableField(exist = false)
    private String reporterUsername;

    /** 报告人头像，非数据库字段 */
    @TableField(exist = false)
    private String reporterAvatar;

    /** 救助者用户名，非数据库字段 */
    @TableField(exist = false)
    private String rescuerUsername;

    /** 救助者头像，非数据库字段 */
    @TableField(exist = false)
    private String rescuerAvatar;

    // 手动添加getter和setter方法，因为Lombok可能没有正确工作
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCatCondition() {
        return catCondition;
    }

    public void setCatCondition(String catCondition) {
        this.catCondition = catCondition;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public Long getRescuerId() {
        return rescuerId;
    }

    public void setRescuerId(Long rescuerId) {
        this.rescuerId = rescuerId;
    }

    public LocalDateTime getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(LocalDateTime acceptTime) {
        this.acceptTime = acceptTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    public String getVolunteerName() {
        return volunteerName;
    }

    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }

    public String getVolunteerPhone() {
        return volunteerPhone;
    }

    public void setVolunteerPhone(String volunteerPhone) {
        this.volunteerPhone = volunteerPhone;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getRescueLogImages() {
        return rescueLogImages;
    }

    public void setRescueLogImages(String rescueLogImages) {
        this.rescueLogImages = rescueLogImages;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public void setReporterUsername(String reporterUsername) {
        this.reporterUsername = reporterUsername;
    }

    public String getReporterAvatar() {
        return reporterAvatar;
    }

    public void setReporterAvatar(String reporterAvatar) {
        this.reporterAvatar = reporterAvatar;
    }

    public String getRescuerUsername() {
        return rescuerUsername;
    }

    public void setRescuerUsername(String rescuerUsername) {
        this.rescuerUsername = rescuerUsername;
    }

    public String getRescuerAvatar() {
        return rescuerAvatar;
    }

    public void setRescuerAvatar(String rescuerAvatar) {
        this.rescuerAvatar = rescuerAvatar;
    }
}