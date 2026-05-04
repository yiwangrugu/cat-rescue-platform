package com.catrescue.catrescueplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("adoptions")
public class Adoption {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("cat_id")
    private Long catId;

    @TableField("user_id")
    private Long userId;

    @TableField("applicant_id")
    private Long applicantId;

    @TableField("status")
    private String status;

    @TableField(exist = false)
    private Long reviewerId;

    @TableField(exist = false)
    private String reviewComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private LocalDateTime approveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private LocalDateTime rejectTime;

    @TableField(exist = false)
    private LocalDateTime processTime;

    @TableField(exist = false)
    private String catName;

    @TableField(exist = false)
    private String catBreed;

    @TableField(exist = false)
    private String catImage;

    @TableField(exist = false)
    private Integer catAge;

    @TableField(exist = false)
    private String catGender;

    @TableField(exist = false)
    private String catHealth;

    @TableField(exist = false)
    private String catLocation;

    @TableField(exist = false)
    private String catPersonality;

    @TableField(exist = false)
    private Double catWeight;

    @TableField(exist = false)
    private String catDescription;

    @TableField(exist = false)
    private String catMedical;

    @TableField(exist = false)
    private String catStatus;

    @TableField(exist = false)
    private String applicantName;

    @TableField(exist = false)
    private String applicantPhone;

    @TableField(exist = false)
    private String applicantAvatar;

    @TableField(exist = false)
    private String reviewerName;

    // 申请人基本信息字段（用于接收前端数据）
    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String idCard;

    @TableField(exist = false)
    private String phone;

    @TableField(exist = false)
    private String address;

    // 申请人详细信息字段（从applicants表获取）
    @TableField(exist = false)
    private String applicationReason;

    @TableField(exist = false)
    private String livingCondition;

    @TableField(exist = false)
    private String experience;

    @TableField(exist = false)
    private String familyMembers;

    @TableField(exist = false)
    private String financialStatus;

    @TableField(exist = false)
    private String workSchedule;

    @TableField(exist = false)
    private String additionalInfo;

    // 关联申请人详细信息
    @TableField(exist = false)
    private Applicant applicant;

    // 手动添加getter和setter方法，因为Lombok可能没有正确工作
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCatId() {
        return catId;
    }

    public void setCatId(Long catId) {
        this.catId = catId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
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

    public LocalDateTime getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }

    public LocalDateTime getRejectTime() {
        return rejectTime;
    }

    public void setRejectTime(LocalDateTime rejectTime) {
        this.rejectTime = rejectTime;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getCatBreed() {
        return catBreed;
    }

    public void setCatBreed(String catBreed) {
        this.catBreed = catBreed;
    }

    public String getCatImage() {
        return catImage;
    }

    public void setCatImage(String catImage) {
        this.catImage = catImage;
    }

    public Integer getCatAge() {
        return catAge;
    }

    public void setCatAge(Integer catAge) {
        this.catAge = catAge;
    }

    public String getCatGender() {
        return catGender;
    }

    public void setCatGender(String catGender) {
        this.catGender = catGender;
    }

    public String getCatHealth() {
        return catHealth;
    }

    public void setCatHealth(String catHealth) {
        this.catHealth = catHealth;
    }

    public String getCatLocation() {
        return catLocation;
    }

    public void setCatLocation(String catLocation) {
        this.catLocation = catLocation;
    }

    public String getCatPersonality() {
        return catPersonality;
    }

    public void setCatPersonality(String catPersonality) {
        this.catPersonality = catPersonality;
    }

    public Double getCatWeight() {
        return catWeight;
    }

    public void setCatWeight(Double catWeight) {
        this.catWeight = catWeight;
    }

    public String getCatDescription() {
        return catDescription;
    }

    public void setCatDescription(String catDescription) {
        this.catDescription = catDescription;
    }

    public String getCatMedical() {
        return catMedical;
    }

    public void setCatMedical(String catMedical) {
        this.catMedical = catMedical;
    }

    public String getCatStatus() {
        return catStatus;
    }

    public void setCatStatus(String catStatus) {
        this.catStatus = catStatus;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantPhone() {
        return applicantPhone;
    }

    public void setApplicantPhone(String applicantPhone) {
        this.applicantPhone = applicantPhone;
    }

    public String getApplicantAvatar() {
        return applicantAvatar;
    }

    public void setApplicantAvatar(String applicantAvatar) {
        this.applicantAvatar = applicantAvatar;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApplicationReason() {
        return applicationReason;
    }

    public void setApplicationReason(String applicationReason) {
        this.applicationReason = applicationReason;
    }

    public String getLivingCondition() {
        return livingCondition;
    }

    public void setLivingCondition(String livingCondition) {
        this.livingCondition = livingCondition;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(String familyMembers) {
        this.familyMembers = familyMembers;
    }

    public String getFinancialStatus() {
        return financialStatus;
    }

    public void setFinancialStatus(String financialStatus) {
        this.financialStatus = financialStatus;
    }

    public String getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }
}