package com.catrescue.catrescueplatform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catrescue.catrescueplatform.entity.Applicant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ApplicantRepository extends BaseMapper<Applicant> {

    /**
     * 根据用户ID查找申请人信息
     */
    @Select("SELECT * FROM applicants WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT 1")
    Applicant findByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已有申请人信息
     */
    @Select("SELECT COUNT(*) FROM applicants WHERE user_id = #{userId}")
    int existsByUserId(@Param("userId") Long userId);
}