package com.catrescue.catrescueplatform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catrescue.catrescueplatform.entity.StatisticsSnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StatisticsSnapshotRepository extends BaseMapper<StatisticsSnapshot> {
}