package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catrescue.catrescueplatform.entity.Comment;
import com.catrescue.catrescueplatform.entity.Favorite;
import com.catrescue.catrescueplatform.entity.Post;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.mapper.CommentMapper;
import com.catrescue.catrescueplatform.repository.FavoriteRepository;
import com.catrescue.catrescueplatform.repository.PostRepository;
import com.catrescue.catrescueplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;

    public IPage<Post> getPosts(int page, int size, String category) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        // 查询有效且状态为PUBLISHED或status为NULL的帖子
        wrapper.eq("is_valid", true);
        wrapper.and(w -> w.eq("status", "PUBLISHED").or().isNull("status"));
        wrapper.orderByDesc("create_time");

        IPage<Post> postPage = postRepository.selectPage(new Page<>(page, size), wrapper);
        if (postPage != null && postPage.getRecords() != null) {
            for (Post post : postPage.getRecords()) {
                if (post.getAuthorId() != null) {
                    User user = userRepository.selectById(post.getAuthorId());
                    if (user != null) {
                        post.setAuthorUsername(user.getUsername());
                        post.setAuthorAvatar(user.getAvatar());
                    }
                }
            }
        }
        return postPage;
    }

    public Post getPostById(Long id) {
        System.err.println("\n=== getPostById(Long id) called ===");
        System.err.println("Post ID: " + id);
        Post post = getPostById(id, true);
        System.err.println("=== getPostById(Long id) ended ===\n");
        return post;
    }

    public Post getPostById(Long id, boolean incrementViewCount) {
        System.err.println("\n=== getPostById(Long id, boolean incrementViewCount) called ===");
        System.err.println("Post ID: " + id + ", incrementViewCount: " + incrementViewCount);
        Post post = postRepository.selectById(id);
        if (post != null) {
            System.err.println("Before increment - Post ID: " + id + ", View Count: " + post.getViewCount()
                    + ", Comment Count: " + post.getCommentCount());
            if (incrementViewCount) {
                Integer oldViewCount = post.getViewCount() != null ? post.getViewCount() : 0;
                Integer newViewCount = oldViewCount + 1;
                System.err.println("Incrementing view count from " + oldViewCount + " to " + newViewCount);
                post.setViewCount(newViewCount);
                postRepository.updateById(post);
                // 重新从数据库中获取帖子对象，以确保日志输出的是数据库中的实际值
                post = postRepository.selectById(id);
                System.err.println("After update - Post ID: " + id + ", View Count: " + post.getViewCount()
                        + ", Comment Count: " + post.getCommentCount());
            }
            if (post.getAuthorId() != null) {
                User user = userRepository.selectById(post.getAuthorId());
                if (user != null) {
                    post.setAuthorUsername(user.getUsername());
                    post.setAuthorAvatar(user.getAvatar());
                }
            }
        }
        System.err.println("=== getPostById(Long id, boolean incrementViewCount) ended ===\n");
        return post;
    }

    public Post createPost(Post post) {
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        // 新创建的帖子默认有效
        post.setIsValid(true);
        // 如果前端没有设置状态，默认设置为已发布
        if (post.getStatus() == null || post.getStatus().isEmpty()) {
            post.setStatus("已发布");
        }
        postRepository.insert(post);

        // 如果帖子状态是已发布，更新用户的帖子数
        if (post.getAuthorId() != null && "已发布".equals(post.getStatus())) {
            User user = userRepository.selectById(post.getAuthorId());
            if (user != null) {
                user.setPostCount((user.getPostCount() != null ? user.getPostCount() : 0) + 1);
                userRepository.updateById(user);
                System.out.println("用户帖子数更新: " + user.getUsername() + " 帖子数: " + user.getPostCount());
            }
        }

        if (post.getAuthorId() != null) {
            User user = userRepository.selectById(post.getAuthorId());
            if (user != null) {
                post.setAuthorUsername(user.getUsername());
                post.setAuthorAvatar(user.getAvatar());
            }
        }
        return post;
    }

    public Post updatePost(Post post) {
        Post existingPost = postRepository.selectById(post.getId());
        if (existingPost != null) {
            post.setCreateTime(existingPost.getCreateTime());
        }

        postRepository.updateById(post);

        if (post.getAuthorId() != null) {
            User user = userRepository.selectById(post.getAuthorId());
            if (user != null) {
                post.setAuthorUsername(user.getUsername());
                post.setAuthorAvatar(user.getAvatar());
            }
        }

        return post;
    }

    public void deletePost(Long id) {
        // 改为标记为无效而不是删除
        Post post = postRepository.selectById(id);
        if (post != null) {
            post.setIsValid(false);
            postRepository.updateById(post);
        }
    }

    public List<Post> getPostsByAuthor(Long authorId) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("author_id", authorId);
        wrapper.eq("is_valid", true);
        wrapper.orderByDesc("create_time");
        List<Post> posts = postRepository.selectList(wrapper);
        for (Post post : posts) {
            if (post.getAuthorId() != null) {
                User user = userRepository.selectById(post.getAuthorId());
                if (user != null) {
                    post.setAuthorUsername(user.getUsername());
                    post.setAuthorAvatar(user.getAvatar());
                }
            }
        }
        return posts;
    }

    public void likePost(Long id) {
        Post post = postRepository.selectById(id);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.updateById(post);
        }
    }

    public void unlikePost(Long id) {
        Post post = postRepository.selectById(id);
        if (post != null && post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.updateById(post);
        }
    }

    public void favoritePost(Long postId) {
        System.out.println("收藏操作开始 - 帖子ID: " + postId);

        // 简化实现：直接增加收藏计数
        Post post = postRepository.selectById(postId);
        if (post != null) {
            int currentCount = post.getFavoriteCount() != null ? post.getFavoriteCount() : 0;
            post.setFavoriteCount(currentCount + 1);
            postRepository.updateById(post);
            System.out.println("收藏成功 - 帖子ID: " + postId + ", 当前收藏数: " + (currentCount + 1));
        }
    }

    public void unfavoritePost(Long postId) {
        System.out.println("取消收藏操作开始 - 帖子ID: " + postId);

        // 简化实现：直接减少收藏计数
        Post post = postRepository.selectById(postId);
        if (post != null) {
            int currentCount = post.getFavoriteCount() != null ? post.getFavoriteCount() : 0;
            if (currentCount > 0) {
                post.setFavoriteCount(currentCount - 1);
                postRepository.updateById(post);
                System.out.println("取消收藏成功 - 帖子ID: " + postId + ", 当前收藏数: " + (currentCount - 1));
            }
        }
    }

    public List<Post> getUserFavorites(Long userId) {
        // 获取用户收藏的记录（包含收藏时间）
        QueryWrapper<Favorite> favoriteWrapper = new QueryWrapper<>();
        favoriteWrapper.eq("user_id", userId);
        favoriteWrapper.orderByDesc("create_time"); // 按照收藏时间倒序排序
        List<Favorite> favorites = favoriteRepository.selectList(favoriteWrapper);

        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取帖子ID列表
        List<Long> postIds = favorites.stream()
                .map(Favorite::getPostId)
                .collect(java.util.stream.Collectors.toList());

        // 根据帖子ID列表获取帖子详情
        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        postWrapper.in("id", postIds);

        // 使用Map存储帖子，以便按照收藏时间顺序构建结果列表
        java.util.Map<Long, Post> postMap = new java.util.HashMap<>();
        List<Post> posts = postRepository.selectList(postWrapper);
        for (Post post : posts) {
            postMap.put(post.getId(), post);
        }

        // 按照收藏时间顺序构建结果列表
        List<Post> result = new ArrayList<>();
        for (Long postId : postIds) {
            Post post = postMap.get(postId);
            if (post != null) {
                fillAuthorInfo(post);
                result.add(post);
            }
        }

        return result;
    }

    public void updateCommentCount(Long postId) {
        // 获取帖子
        Post post = postRepository.selectById(postId);
        if (post != null) {
            // 计算评论数
            QueryWrapper<Comment> wrapper = new QueryWrapper<>();
            wrapper.eq("post_id", postId);
            int commentCount = Math.toIntExact(commentMapper.selectCount(wrapper));

            // 更新评论数
            post.setCommentCount(commentCount);
            postRepository.updateById(post);
        }
    }

    private void fillAuthorInfo(Post post) {
        if (post != null && post.getAuthorId() != null) {
            User user = userRepository.selectById(post.getAuthorId());
            if (user != null) {
                post.setAuthorUsername(user.getUsername());
                post.setAuthorAvatar(user.getAvatar());
            }
        }
    }

    private void fillAuthorInfo(List<Post> posts) {
        for (Post post : posts) {
            fillAuthorInfo(post);
        }
    }

    private void fillAuthorInfo(IPage<Post> page) {
        if (page != null && page.getRecords() != null) {
            fillAuthorInfo(page.getRecords());
        }
    }

    public List<String> getCategories() {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT category");
        wrapper.isNotNull("category");
        wrapper.ne("category", "");
        List<Post> posts = postRepository.selectList(wrapper);
        return posts.stream().map(Post::getCategory).toList();
    }

    public IPage<Post> getPendingPosts(int page, int size) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING_REVIEW");
        // 只显示有效的待审核帖子（isValid=true）
        wrapper.eq("is_valid", true);
        wrapper.orderByDesc("create_time");
        IPage<Post> postPage = postRepository.selectPage(new Page<>(page, size), wrapper);
        fillAuthorInfo(postPage);
        return postPage;
    }

    public Post approvePost(Long id, String remark) {
        System.out.println("开始审核通过帖子，ID: " + id + ", 备注: " + remark);
        Post post = postRepository.selectById(id);
        if (post != null) {
            System.out.println("找到帖子，当前状态: " + post.getStatus());

            // 只有在帖子状态不是已发布时才更新用户帖子数
            boolean shouldUpdatePostCount = !"已发布".equals(post.getStatus());

            post.setStatus("已发布");
            if (remark != null && !remark.trim().isEmpty()) {
                post.setAuditRemark(remark);
            }
            post.setAuditTime(LocalDateTime.now());
            System.out.println("更新帖子状态为: " + post.getStatus());
            int result = postRepository.updateById(post);
            System.out.println("数据库更新结果: " + result + " 行受影响");

            // 如果帖子状态从其他状态变为已发布，更新用户的帖子数
            if (shouldUpdatePostCount && post.getAuthorId() != null) {
                User user = userRepository.selectById(post.getAuthorId());
                if (user != null) {
                    user.setPostCount((user.getPostCount() != null ? user.getPostCount() : 0) + 1);
                    userRepository.updateById(user);
                    System.out.println("用户帖子数更新: " + user.getUsername() + " 帖子数: " + user.getPostCount());
                }
            }

            fillAuthorInfo(post);
        } else {
            System.out.println("未找到ID为 " + id + " 的帖子");
        }
        return post;
    }

    public Post rejectPost(Long id, String remark) {
        System.out.println("开始审核拒绝帖子，ID: " + id + ", 备注: " + remark);
        Post post = postRepository.selectById(id);
        if (post != null) {
            System.out.println("找到帖子，当前状态: " + post.getStatus());
            post.setStatus("REJECTED");
            if (remark != null && !remark.trim().isEmpty()) {
                post.setAuditRemark(remark);
            }
            post.setAuditTime(LocalDateTime.now());
            System.out.println("更新帖子状态为: " + post.getStatus());
            int result = postRepository.updateById(post);
            System.out.println("数据库更新结果: " + result + " 行受影响");
            fillAuthorInfo(post);
        } else {
            System.out.println("未找到ID为 " + id + " 的帖子");
        }
        return post;
    }

    /**
     * 获取已拒绝帖子列表
     */
    public IPage<Post> getRejectedPosts(int page, int size) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "REJECTED");
        wrapper.orderByDesc("audit_time");
        IPage<Post> postPage = postRepository.selectPage(new Page<>(page, size), wrapper);
        fillAuthorInfo(postPage);
        return postPage;
    }

}
