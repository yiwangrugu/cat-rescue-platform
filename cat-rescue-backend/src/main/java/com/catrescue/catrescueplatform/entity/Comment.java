package com.catrescue.catrescueplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论实体类
 * 对应数据库表: comments
 */
@Data
@TableName("comments")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("author_id")
    private Long authorId;

    @TableField("content")
    private String content;

    @TableField("parent_id")
    private Long parentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 作者用户名，非数据库字段 */
    @TableField(exist = false)
    private String authorUsername;

    /** 作者头像，非数据库字段 */
    @TableField(exist = false)
    private String authorAvatar;

    /** 是否是帖主回复，非数据库字段 */
    @TableField(exist = false)
    private Boolean isPostAuthor;

    /** 被回复的评论作者用户名，非数据库字段 */
    @TableField(exist = false)
    private String repliedToUsername;

    /** 被回复的评论作者ID，非数据库字段 */
    @TableField(exist = false)
    private Long repliedToAuthorId;

}