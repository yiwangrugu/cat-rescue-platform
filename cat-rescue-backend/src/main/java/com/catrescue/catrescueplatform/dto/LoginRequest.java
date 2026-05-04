package com.catrescue.catrescueplatform.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String phone;
    private String password;

    // 手动添加getter和setter方法，因为Lombok可能没有正确工作
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}