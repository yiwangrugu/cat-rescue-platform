package com.catrescue.catrescueplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.catrescue.catrescueplatform.entity.Comment;
import com.catrescue.catrescueplatform.service.CommentService;
import com.catrescue.catrescueplatform.config.RescueWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community/posts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取帖子的评论列表
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getPostComments(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 分页获取帖子的评论
     */
    @GetMapping("/{postId}/comments/page")
    public ResponseEntity<IPage<Comment>> getPostCommentsPage(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Comment> comments = commentService.getCommentsByPostId(postId, page, size);
        return ResponseEntity.ok(comments);
    }

    /**
     * 添加评论
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody Comment comment) {
        comment.setPostId(postId);
        Comment createdComment = commentService.addComment(comment);

        // 发送WebSocket通知
        RescueWebSocketHandler.notifyCommentUpdate();

        return ResponseEntity.ok(createdComment);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        commentService.deleteComment(commentId);

        // 发送WebSocket通知
        RescueWebSocketHandler.notifyCommentUpdate();

        return ResponseEntity.ok().build();
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{postId}/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long postId, @PathVariable Long commentId) {
        commentService.likeComment(commentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 取消点赞评论
     */
    @PostMapping("/{postId}/comments/{commentId}/unlike")
    public ResponseEntity<?> unlikeComment(@PathVariable Long postId, @PathVariable Long commentId) {
        commentService.unlikeComment(commentId);
        return ResponseEntity.ok().build();
    }
}