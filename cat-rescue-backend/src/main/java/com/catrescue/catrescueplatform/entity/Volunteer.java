package com.catrescue.catrescueplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 志愿者实体类
 * 对应数据库表: volunteers
 * 
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@TableName("volunteers")
public class Volunteer {

    /** 志愿者ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 志愿者姓名 */
    @TableField("name")
    private String name;

    /** 志愿者电话 */
    @TableField("phone")
    private String phone;

    /** 志愿者邮箱 */
    @TableField("email")
    private String email;

    /** 志愿者地址 */
    @TableField("address")
    private String address;

    /** 志愿者状态：空闲/忙碌 */
    @TableField("status")
    private String status;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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