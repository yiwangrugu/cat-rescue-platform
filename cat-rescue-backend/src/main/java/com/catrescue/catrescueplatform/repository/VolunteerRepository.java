package com.catrescue.catrescueplatform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catrescue.catrescueplatform.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 志愿者数据访问层
 * 
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@Mapper
public interface VolunteerRepository extends BaseMapper<Volunteer> {
}