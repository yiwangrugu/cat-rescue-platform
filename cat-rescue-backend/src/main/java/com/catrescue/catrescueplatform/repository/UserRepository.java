package com.catrescue.catrescueplatform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catrescue.catrescueplatform.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository extends BaseMapper<User> {
    User findByUsername(String username);
    User findByEmail(String email);
}