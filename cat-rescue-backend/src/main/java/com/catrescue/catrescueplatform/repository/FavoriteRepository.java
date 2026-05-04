package com.catrescue.catrescueplatform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catrescue.catrescueplatform.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteRepository extends BaseMapper<Favorite> {

    /**
     * 检查用户是否已收藏帖子
     */
    Integer countByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 根据用户ID获取收藏的帖子ID列表
     */
    List<Long> selectPostIdsByUserId(@Param("userId") Long userId);
}
