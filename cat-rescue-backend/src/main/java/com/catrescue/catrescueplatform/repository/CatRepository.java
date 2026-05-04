package com.catrescue.catrescueplatform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catrescue.catrescueplatform.entity.Cat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CatRepository extends BaseMapper<Cat> {
}