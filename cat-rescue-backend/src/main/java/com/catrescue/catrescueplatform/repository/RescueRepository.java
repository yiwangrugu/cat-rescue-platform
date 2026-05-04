package com.catrescue.catrescueplatform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catrescue.catrescueplatform.entity.Rescue;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RescueRepository extends BaseMapper<Rescue> {
}