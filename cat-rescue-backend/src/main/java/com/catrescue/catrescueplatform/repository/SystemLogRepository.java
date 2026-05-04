package com.catrescue.catrescueplatform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catrescue.catrescueplatform.entity.SystemLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemLogRepository extends BaseMapper<SystemLog> {
}