package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.catrescue.catrescueplatform.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    
    /**
     * 获取帖子的评论列表
     */
    List<Comment> getCommentsByPostId(Long postId);
    
    /**
     * 分页获取帖子的评论
     */
    IPage<Comment> getCommentsByPostId(Long postId, int page, int size);
    
    /**
     * 添加评论
     */
    Comment addComment(Comment comment);
    
    /**
     * 删除评论
     */
    void deleteComment(Long commentId);
    
    /**
     * 点赞评论
     */
    void likeComment(Long commentId);
    
    /**
     * 取消点赞评论
     */
    void unlikeComment(Long commentId);
}