package com.catrescue.catrescueplatform.controller;

import com.catrescue.catrescueplatform.dto.LoginRequest;
import com.catrescue.catrescueplatform.dto.RegisterRequest;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.service.UserService;
import com.catrescue.catrescueplatform.service.SystemLogService;
import com.catrescue.catrescueplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final SystemLogService systemLogService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            String token = jwtUtil.generateToken(user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            User user = userService.login(request);
            String token = jwtUtil.generateToken(user.getUsername());

            // 记录登录日志
            systemLogService.log("登录", "认证", "用户 " + user.getUsername() + " 登录系统", httpRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件不能为空"));
            }

            String contentType = file.getContentType();
            if (contentType == null
                    || (!contentType.startsWith("image/jpeg") && !contentType.startsWith("image/png"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "只支持 JPG 或 PNG 格式的图片"));
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            String avatarUrl = "/uploads/" + newFilename;

            User user = userService.updateAvatar(userId, avatarUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("avatarUrl", avatarUrl);
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件不能为空"));
            }

            String contentType = file.getContentType();
            if (contentType == null
                    || (!contentType.startsWith("image/jpeg") && !contentType.startsWith("image/png"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "只支持 JPG 或 PNG 格式的图片"));
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "/uploads/" + newFilename;

            Map<String, Object> response = new HashMap<>();
            response.put("fileUrl", fileUrl);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates) {
        try {
            Long userId = Long.valueOf(updates.get("id").toString());
            User user = userService.updateProfile(userId, updates);

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            User user = userService.findById(userId);
            if (user != null) {
                userService.logout(userId);
                systemLogService.log("退出", "认证", "用户 " + user.getUsername() + " 退出系统", httpRequest);
            }
            return ResponseEntity.ok(Map.of("message", "退出成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}