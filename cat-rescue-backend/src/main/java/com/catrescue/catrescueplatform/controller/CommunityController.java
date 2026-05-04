package com.catrescue.catrescueplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.catrescue.catrescueplatform.config.BusinessLog;
import com.catrescue.catrescueplatform.entity.Post;
import com.catrescue.catrescueplatform.service.PostService;
import com.catrescue.catrescueplatform.config.RescueWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final PostService postService;

    @GetMapping("/posts")
    public ResponseEntity<IPage<Post>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {

        IPage<Post> posts = postService.getPosts(page, size, category);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts")
    @BusinessLog(action = "提交帖子审核", module = "帖子管理", description = "提交了帖子审核：{post.title}")
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        Post createdPost = postService.createPost(post);

        // 发送帖子申请更新通知
        String messageData = String.format("{\"postId\":%d,\"action\":\"CREATE\",\"userId\":%d,\"status\":\"PENDING\"}",
                createdPost.getId(), post.getAuthorId());
        RescueWebSocketHandler.notifyPostApplicationUpdate(messageData);

        return ResponseEntity.ok(createdPost);
    }

    @PutMapping("/posts/{id}")
    @BusinessLog(action = "更新帖子", module = "帖子管理", description = "更新了帖子（ID: {id}）")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post post) {
        post.setId(id);
        Post updatedPost = postService.updatePost(post);

        // 如果帖子状态变为PENDING或PENDING_REVIEW（重新提交审核），发送更新通知
        if ("PENDING".equals(updatedPost.getStatus()) || "PENDING_REVIEW".equals(updatedPost.getStatus())) {
            String messageData = String.format(
                    "{\"postId\":%d,\"action\":\"UPDATE\",\"userId\":%d,\"status\":\"PENDING\"}",
                    updatedPost.getId(), updatedPost.getAuthorId());
            RescueWebSocketHandler.notifyPostApplicationUpdate(messageData);
        }

        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/posts/{id}")
    @BusinessLog(action = "删除帖子", module = "帖子管理", description = "删除了帖子（ID: {id}）")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deletePost(id);

        // 发送WebSocket通知
        RescueWebSocketHandler.notifyPostUpdate();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/author/{authorId}")
    public ResponseEntity<List<Post>> getPostsByAuthor(@PathVariable Long authorId) {
        List<Post> posts = postService.getPostsByAuthor(authorId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/pending")
    public ResponseEntity<IPage<Post>> getPendingPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Post> posts = postService.getPendingPosts(page, size);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/posts/{id}/approve")
    @BusinessLog(action = "审核通过帖子", module = "帖子管理", description = "审核通过了帖子（ID: {id}）")
    public ResponseEntity<Post> approvePost(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String remark = request != null ? request.get("remark") : null;
        Post approvedPost = postService.approvePost(id, remark);

        // 发送帖子审核通过通知
        String messageData = String.format(
                "{\"postId\":%d,\"action\":\"APPROVE\",\"userId\":%d,\"status\":\"APPROVED\",\"reviewComment\":\"%s\"}",
                approvedPost.getId(), approvedPost.getAuthorId(), remark != null ? remark : "");
        RescueWebSocketHandler.notifyPostReviewUpdate(messageData);

        return ResponseEntity.ok(approvedPost);
    }

    @PostMapping("/posts/{id}/reject")
    @BusinessLog(action = "审核拒绝帖子", module = "帖子管理", description = "审核拒绝了帖子（ID: {id}）")
    public ResponseEntity<Post> rejectPost(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String remark = request != null ? request.get("remark") : null;
        Post rejectedPost = postService.rejectPost(id, remark);

        // 发送帖子审核拒绝通知
        String messageData = String.format(
                "{\"postId\":%d,\"action\":\"REJECT\",\"userId\":%d,\"status\":\"REJECTED\",\"reviewComment\":\"%s\"}",
                rejectedPost.getId(), rejectedPost.getAuthorId(), remark != null ? remark : "");
        RescueWebSocketHandler.notifyPostReviewUpdate(messageData);

        return ResponseEntity.ok(rejectedPost);
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id) {
        postService.likePost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{id}/unlike")
    public ResponseEntity<?> unlikePost(@PathVariable Long id) {
        postService.unlikePost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{id}/favorite")
    public ResponseEntity<?> favoritePost(@PathVariable Long id) {
        postService.favoritePost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{id}/unfavorite")
    public ResponseEntity<?> unfavoritePost(@PathVariable Long id) {
        postService.unfavoritePost(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/favorites")
    public ResponseEntity<List<Post>> getUserFavorites(@PathVariable Long userId) {
        List<Post> favorites = postService.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = postService.getCategories();
        return ResponseEntity.ok(categories);
    }
}