package com.catrescue.catrescueplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表: users
 * 
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@TableName("users")
public class User {

    /** 用户ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，唯一标识 */
    @TableField("username")
    private String username;

    /** 密码，JSON序列化时忽略 */
    @JsonIgnore
    @TableField("password")
    private String password;

    /** 邮箱地址 */
    @TableField("email")
    private String email;

    /** 手机号码 */
    @TableField("phone")
    private String phone;

    /** 性别：male(男), female(女), unknown(保密) */
    @TableField("gender")
    private String gender;

    /** 用户角色：USER(普通用户), RESCUER(救助者), ADMIN(管理员) */
    @TableField("role")
    private String role;

    /** 头像URL */
    @TableField("avatar")
    private String avatar;

    /** 真实姓名 */
    @TableField("real_name")
    private String realName;

    /** 所在地区 */
    @TableField("location")
    private String location;

    /** 个人简介 */
    @TableField("introduction")
    private String introduction;

    /** 用户状态：ACTIVE(激活), INACTIVE(未激活), BANNED(封禁) */
    @TableField("status")
    private String status;

    /** 解禁时间，当用户被禁用时记录解禁时间 */
    @TableField("ban_end_time")
    private LocalDateTime banEndTime;

    /** 创建时间，自动填充 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，自动填充 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 最近登录时间 */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /** 上一次登录时间 */
    @TableField("previous_login_time")
    private LocalDateTime previousLoginTime;

    /** 帖子数量 */
    @TableField("post_count")
    private Integer postCount;

    /** 领养申请数量 */
    @TableField("adoption_count")
    private Integer adoptionCount;

    /** 收藏数量 */
    @TableField("favorite_count")
    private Integer favoriteCount;

    /** 救助需求数量 */
    @TableField("rescue_count")
    private Integer rescueCount;

    /** 在线状态：1-在线，0-离线 */
    @TableField("is_online")
    private Integer isOnline;

    // 手动添加getter和setter方法，因为Lombok可能没有正确工作
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Integer getRescueCount() {
        return rescueCount;
    }

    public void setRescueCount(Integer rescueCount) {
        this.rescueCount = rescueCount;
    }

    public Integer getAdoptionCount() {
        return adoptionCount;
    }

    public void setAdoptionCount(Integer adoptionCount) {
        this.adoptionCount = adoptionCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBanEndTime() {
        return banEndTime;
    }

    public void setBanEndTime(LocalDateTime banEndTime) {
        this.banEndTime = banEndTime;
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

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getPreviousLoginTime() {
        return previousLoginTime;
    }

    public void setPreviousLoginTime(LocalDateTime previousLoginTime) {
        this.previousLoginTime = previousLoginTime;
    }

    public Integer getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Integer isOnline) {
        this.isOnline = isOnline;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}