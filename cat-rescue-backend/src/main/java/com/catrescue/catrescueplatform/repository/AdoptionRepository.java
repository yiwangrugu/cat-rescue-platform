package com.catrescue.catrescueplatform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catrescue.catrescueplatform.entity.Adoption;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdoptionRepository extends BaseMapper<Adoption> {
}