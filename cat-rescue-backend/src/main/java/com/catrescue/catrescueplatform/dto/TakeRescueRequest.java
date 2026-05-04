package com.catrescue.catrescueplatform.dto;

import lombok.Data;

/**
 * 马上救援请求DTO
 * 
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@Data
public class TakeRescueRequest {

    /** 负责人ID（处理本次救助的账号） */
    private Long rescuerId;

    /** 志愿者姓名 */
    private String volunteerName;

    /** 志愿者电话 */
    private String volunteerPhone;

    // 手动添加getter和setter方法，因为Lombok可能没有正确工作
    public Long getRescuerId() {
        return rescuerId;
    }

    public void setRescuerId(Long rescuerId) {
        this.rescuerId = rescuerId;
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
}