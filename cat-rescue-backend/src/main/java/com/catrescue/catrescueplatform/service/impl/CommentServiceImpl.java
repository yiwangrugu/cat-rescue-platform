package com.catrescue.catrescueplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catrescue.catrescueplatform.entity.Comment;
import com.catrescue.catrescueplatform.entity.Notification;
import com.catrescue.catrescueplatform.entity.Post;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.mapper.CommentMapper;
import com.catrescue.catrescueplatform.repository.UserRepository;
import com.catrescue.catrescueplatform.service.CommentService;
import com.catrescue.catrescueplatform.service.NotificationService;
import com.catrescue.catrescueplatform.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;
    private final PostService postService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId)
                .orderByDesc("create_time");
        List<Comment> comments = list(queryWrapper);

        for (Comment comment : comments) {
            if (comment.getAuthorId() != null) {
                User user = userRepository.selectById(comment.getAuthorId());
                if (user != null) {
                    comment.setAuthorUsername(user.getUsername());
                    comment.setAuthorAvatar(user.getAvatar());
                }
            }

            fillCommentInfo(comment, postId);
        }
        return comments;
    }

    @Override
    public IPage<Comment> getCommentsByPostId(Long postId, int page, int size) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId)
                .orderByDesc("create_time");

        Page<Comment> pageParam = new Page<>(page, size);
        IPage<Comment> commentPage = page(pageParam, queryWrapper);
        if (commentPage != null && commentPage.getRecords() != null) {
            for (Comment comment : commentPage.getRecords()) {
                if (comment.getAuthorId() != null) {
                    User user = userRepository.selectById(comment.getAuthorId());
                    if (user != null) {
                        comment.setAuthorUsername(user.getUsername());
                        comment.setAuthorAvatar(user.getAvatar());
                    }
                }

                fillCommentInfo(comment, postId);
            }
        }
        return commentPage;
    }

    @Override
    @Transactional
    public Comment addComment(Comment comment) {
        // 保存评论
        save(comment);

        // 填充用户信息
        if (comment.getAuthorId() != null) {
            User user = userRepository.selectById(comment.getAuthorId());
            if (user != null) {
                comment.setAuthorUsername(user.getUsername());
                comment.setAuthorAvatar(user.getAvatar());
            }
        }

        // 处理回复通知
        if (comment.getParentId() != null) {
            // 获取被回复的评论
            Comment parentComment = getById(comment.getParentId());
            if (parentComment != null && parentComment.getAuthorId() != null) {
                // 避免给自己发通知
                if (!parentComment.getAuthorId().equals(comment.getAuthorId())) {
                    // 获取发送者信息
                    User sender = userRepository.selectById(comment.getAuthorId());
                    if (sender != null) {
                        // 创建通知
                        Notification notification = new Notification();
                        notification.setUserId(parentComment.getAuthorId());
                        notification.setPostId(comment.getPostId());
                        notification.setCommentId(comment.getId());
                        notification.setSenderId(comment.getAuthorId());
                        notification.setSenderUsername(sender.getUsername());
                        notification.setContent("@" + sender.getUsername() + " 回复了你的评论");
                        notificationService.createNotification(notification);
                    }
                }
            }
        } else {
            // 如果是根评论，给帖子作者发送通知
            Post post = postService.getPostById(comment.getPostId(), false);
            if (post != null && post.getAuthorId() != null) {
                // 避免给自己发通知
                if (!post.getAuthorId().equals(comment.getAuthorId())) {
                    // 获取发送者信息
                    User sender = userRepository.selectById(comment.getAuthorId());
                    if (sender != null) {
                        // 创建通知
                        Notification notification = new Notification();
                        notification.setUserId(post.getAuthorId());
                        notification.setPostId(comment.getPostId());
                        notification.setCommentId(comment.getId());
                        notification.setSenderId(comment.getAuthorId());
                        notification.setSenderUsername(sender.getUsername());
                        notification.setContent("@" + sender.getUsername() + " 评论了你的帖子");
                        notificationService.createNotification(notification);
                    }
                }
            }
        }

        // 更新对应帖子的评论数
        if (comment.getPostId() != null) {
            postService.updateCommentCount(comment.getPostId());
        }

        return comment;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        // 获取评论信息，用于后续更新帖子评论数
        Comment comment = getById(commentId);
        Long postId = null;
        if (comment != null) {
            postId = comment.getPostId();
        }

        // 删除评论
        removeById(commentId);

        // 更新对应帖子的评论数
        if (postId != null) {
            postService.updateCommentCount(postId);
        }
    }

    @Override
    @Transactional
    public void likeComment(Long commentId) {
        // 点赞功能已禁用
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId) {
        // 取消点赞功能已禁用
    }

    private void fillCommentInfo(Comment comment, Long postId) {
        if (comment == null) {
            return;
        }

        Post post = postService.getPostById(postId, false);
        if (post != null) {
            comment.setIsPostAuthor(comment.getAuthorId() != null && comment.getAuthorId().equals(post.getAuthorId()));
        }

        if (comment.getParentId() != null) {
            Comment parentComment = getById(comment.getParentId());
            if (parentComment != null && parentComment.getAuthorId() != null) {
                User parentAuthor = userRepository.selectById(parentComment.getAuthorId());
                if (parentAuthor != null) {
                    comment.setRepliedToUsername(parentAuthor.getUsername());
                    comment.setRepliedToAuthorId(parentComment.getAuthorId());
                }
            }
        }
    }
}