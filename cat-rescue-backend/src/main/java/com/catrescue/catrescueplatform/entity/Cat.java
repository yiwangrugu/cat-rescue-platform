package com.catrescue.catrescueplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 猫咪实体类
 * 对应数据库表: cats
 * 
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@TableName("cats")
public class Cat {

    /** 猫咪ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 猫咪名字 */
    @TableField("name")
    private String name;

    /** 猫咪品种 */
    @TableField("breed")
    private String breed;

    /** 猫咪年龄（月） */
    @TableField("age")
    private Integer age;

    /** 猫咪体重（kg） */
    @TableField("weight")
    private Double weight;

    /** 猫咪性别：公/母 */
    @TableField("gender")
    private String gender;

    /** 健康状况：良好/一般/需要治疗 */
    @TableField("health_status")
    private String healthStatus;

    /** 医疗信息：疫苗接种情况、病史、用药情况等 */
    @TableField("medical_info")
    private String medicalInfo;

    /** 性格特点 */
    @TableField("personality")
    private String personality;

    /** 详细描述 */
    @TableField("description")
    private String description;

    /** 所在位置 */
    @TableField("location")
    private String location;

    /** 猫咪图片URL，JSON格式存储多个图片 */
    @TableField("images")
    @com.fasterxml.jackson.annotation.JsonRawValue
    private String images;

    /** 猫咪状态：待领养/已领养/治疗中 */
    @TableField("status")
    private String status;

    /** 审核状态：PENDING-待审核, APPROVED-审核通过, REJECTED-审核拒绝 */
    @TableField("audit_status")
    private String auditStatus;

    /** 创建者ID - 数据库中不存在此字段 */
    @TableField(exist = false)
    private Long creatorId;

    /** 审核备注 */
    @TableField("audit_remark")
    private String auditRemark;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /** 正式图片路径（审核通过后），JSON格式 */
    @TableField("official_images")
    @com.fasterxml.jackson.annotation.JsonRawValue
    private String officialImages;

    /** 创建时间，自动填充，JSON格式化 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，自动填充，JSON格式化 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 手动添加getter和setter方法，因为Lombok可能没有正确工作
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getMedicalInfo() {
        return medicalInfo;
    }

    public void setMedicalInfo(String medicalInfo) {
        this.medicalInfo = medicalInfo;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
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

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public String getOfficialImages() {
        return officialImages;
    }

    public void setOfficialImages(String officialImages) {
        this.officialImages = officialImages;
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
}