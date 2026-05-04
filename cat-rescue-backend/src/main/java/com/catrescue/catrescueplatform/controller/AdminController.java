package com.catrescue.catrescueplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catrescue.catrescueplatform.entity.Cat;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.entity.Rescue;
import com.catrescue.catrescueplatform.entity.Adoption;
import com.catrescue.catrescueplatform.entity.Post;
import com.catrescue.catrescueplatform.entity.Applicant;
import com.catrescue.catrescueplatform.config.RescueWebSocketHandler;
import com.catrescue.catrescueplatform.service.CatService;
import com.catrescue.catrescueplatform.service.UserService;
import com.catrescue.catrescueplatform.service.RescueService;
import com.catrescue.catrescueplatform.service.AdoptionService;
import com.catrescue.catrescueplatform.service.PostService;
import com.catrescue.catrescueplatform.service.SystemLogService;
import com.catrescue.catrescueplatform.repository.UserRepository;
import com.catrescue.catrescueplatform.repository.CatRepository;
import com.catrescue.catrescueplatform.repository.PostRepository;
import com.catrescue.catrescueplatform.repository.RescueRepository;
import com.catrescue.catrescueplatform.repository.AdoptionRepository;
import com.catrescue.catrescueplatform.config.RescueWebSocketHandler;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端控制器
 * 提供管理后台相关的API接口
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CatService catService;
    private final SystemLogService systemLogService;
    private final RescueService rescueService;
    private final AdoptionService adoptionService;
    private final PostService postService;
    private final UserRepository userRepository;
    private final CatRepository catRepository;
    private final PostRepository postRepository;
    private final RescueRepository rescueRepository;
    private final AdoptionRepository adoptionRepository;

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("username", keyword)
                    .or().like("email", keyword)
                    .or().like("real_name", keyword);
        }
        queryWrapper.orderByDesc("create_time");

        IPage<User> userPage = userService.getUserRepository()
                .selectPage(new Page<>(page, size), queryWrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("users", userPage.getRecords());
        response.put("total", userPage.getTotal());
        response.put("page", page);
        response.put("size", size);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取待办事务统计
     */
    @GetMapping("/pending-counts")
    public ResponseEntity<Map<String, Object>> getPendingCounts() {
        try {
            Map<String, Object> counts = getPendingCountsData();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", counts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取待办事务统计失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取待办事务统计数据（内部方法）
     */
    private Map<String, Object> getPendingCountsData() {
        Map<String, Object> counts = new HashMap<>();

        // 统计待审核猫咪数量
        QueryWrapper<Cat> catWrapper = new QueryWrapper<>();
        catWrapper.eq("status", "PENDING_REVIEW");
        counts.put("cats", catRepository.selectCount(catWrapper));

        // 统计待审核帖子数量
        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        postWrapper.eq("status", "PENDING_REVIEW");
        counts.put("posts", postRepository.selectCount(postWrapper));

        // 统计待处理救助数量
        QueryWrapper<Rescue> rescueWrapper = new QueryWrapper<>();
        // 数据库中可能同时存在中英文状态值，同时查询
        rescueWrapper.and(w -> w.eq("status", "待处理").or().eq("status", "PENDING"));
        counts.put("rescues", rescueRepository.selectCount(rescueWrapper));

        // 统计待审核领养数量
        QueryWrapper<Adoption> adoptionWrapper = new QueryWrapper<>();
        adoptionWrapper.eq("status", "PENDING");
        counts.put("adoptions", adoptionRepository.selectCount(adoptionWrapper));

        // 计算总待办事务数量
        long total = (long) counts.get("cats") + (long) counts.get("posts") +
                (long) counts.get("rescues") + (long) counts.get("adoptions");
        counts.put("total", total);

        return counts;
    }

    /**
     * 推送待办事务统计更新
     */
    private void pushPendingCountsUpdate() {
        try {
            RescueWebSocketHandler.notifyPendingCountsUpdate();
            System.out.println("待办事务统计更新已推送");
        } catch (Exception e) {
            System.err.println("推送待办事务统计更新失败: " + e.getMessage());
        }
    }

    /**
     * 添加用户
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody User user, HttpServletRequest request) {
        try {
            // 检查用户名是否已存在
            QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
            usernameWrapper.eq("username", user.getUsername())
                    .last("LIMIT 1");
            User existingUser = userService.getUserRepository().selectOne(usernameWrapper);
            if (existingUser != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户名已存在"));
            }

            // 检查邮箱是否已存在
            QueryWrapper<User> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", user.getEmail())
                    .last("LIMIT 1");
            existingUser = userService.getUserRepository().selectOne(emailWrapper);
            if (existingUser != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "邮箱已存在"));
            }

            // 设置默认值
            if (user.getStatus() == null) {
                user.setStatus("ACTIVE");
            }
            if (user.getRole() == null) {
                user.setRole("USER");
            }

            // 保存用户
            userService.getUserRepository().insert(user);

            // 记录系统日志
            systemLogService.log("创建用户", "用户管理", "管理员添加用户: " + user.getUsername(), request);

            return ResponseEntity.ok(Map.of(
                    "message", "用户添加成功",
                    "userId", user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "添加用户失败: " + e.getMessage()));
        }
    }

    /**
     * 编辑用户
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long userId, @RequestBody User user,
            HttpServletRequest request) {
        try {
            User existingUser = userService.getUserRepository().selectById(userId);
            if (existingUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
            }

            // 检查用户名是否被其他用户使用
            QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
            usernameWrapper.eq("username", user.getUsername())
                    .ne("id", userId)
                    .last("LIMIT 1");
            User usernameUser = userService.getUserRepository().selectOne(usernameWrapper);
            if (usernameUser != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户名已被其他用户使用"));
            }

            // 检查邮箱是否被其他用户使用
            QueryWrapper<User> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", user.getEmail())
                    .ne("id", userId)
                    .last("LIMIT 1");
            User emailUser = userService.getUserRepository().selectOne(emailWrapper);
            if (emailUser != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "邮箱已被其他用户使用"));
            }

            // 更新用户信息
            user.setId(userId);
            userService.getUserRepository().updateById(user);

            // 记录系统日志
            systemLogService.log("更新用户", "用户管理", "管理员更新用户信息: " + existingUser.getUsername(), request);

            return ResponseEntity.ok(Map.of(
                    "message", "用户信息更新成功",
                    "userId", userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "更新用户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 切换用户状态
     */
    @PostMapping("/users/{userId}/toggle-status")
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long userId,
            @RequestParam(required = false) String duration, HttpServletRequest request) {
        User user = userService.getUserRepository().selectById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
        }

        // 检查是否为管理员账号（如果是禁用操作）
        if ("ACTIVE".equals(user.getStatus()) && "ADMIN".equals(user.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("error", "不能禁用管理员账号"));
        }

        String newStatus = "ACTIVE".equals(user.getStatus()) ? "INACTIVE" : "ACTIVE";
        user.setStatus(newStatus);

        // 设置解禁时间（如果被禁用且有指定时长）
        if ("INACTIVE".equals(newStatus) && duration != null && !duration.isEmpty()) {
            LocalDateTime banEndTime;
            if ("1minute".equals(duration)) {
                // 禁用1分钟（测试用）
                banEndTime = LocalDateTime.now().plusMinutes(1);
            } else {
                // 按天数禁用
                try {
                    int days = Integer.parseInt(duration);
                    banEndTime = LocalDateTime.now().plusDays(days);
                } catch (NumberFormatException e) {
                    // 如果无法解析为数字，默认禁用7天
                    banEndTime = LocalDateTime.now().plusDays(7);
                }
            }
            user.setBanEndTime(banEndTime);
        } else {
            user.setBanEndTime(null); // 启用用户或永久禁用时清除解禁时间
        }

        userService.getUserRepository().updateById(user);

        // 发送WebSocket通知
        if ("INACTIVE".equals(newStatus)) {
            // 用户被禁用，发送禁用通知
            String endTime = user.getBanEndTime() != null ? user.getBanEndTime().toString()
                    : new java.util.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L).toInstant().toString();

            System.out.println("🔴 管理员禁用用户 - 用户ID: " + userId + ", 解禁时间: " + endTime);
            System.out.println("🔴 调用WebSocket通知: notifyUserBanned(" + userId + ", " + endTime + ")");

            // 双向通知：
            // 1. 向管理员发送状态更新通知（更新界面）
            com.catrescue.catrescueplatform.config.SimpleUserWebSocketHandler.sendToAll("USER_STATUS_UPDATED");

            // 2. 向被禁用用户发送禁用通知（强制登出）
            com.catrescue.catrescueplatform.config.SimpleUserWebSocketHandler.sendToUser(userId,
                    "USER_BANNED|" + userId + "|" + endTime);
        } else {
            // 用户被启用，发送启用通知
            System.out.println("🟢 管理员启用用户 - 用户ID: " + userId);

            // 双向通知：
            // 1. 向管理员发送状态更新通知（更新界面）
            com.catrescue.catrescueplatform.config.SimpleUserWebSocketHandler.sendToAll("USER_STATUS_UPDATED");

            // 2. 向被启用用户发送启用通知
            com.catrescue.catrescueplatform.config.SimpleUserWebSocketHandler.sendToUser(userId,
                    "USER_UNBANNED|" + userId);
        }

        // 记录系统日志
        String action = "ACTIVE".equals(newStatus) ? "启用用户" : "禁用用户";
        String description = action + ": " + user.getUsername();
        if (user.getBanEndTime() != null) {
            description += " (解禁时间: " + user.getBanEndTime() + ")";
        }
        systemLogService.log(action, "用户管理", description, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "用户状态更新成功");
        response.put("userId", userId);
        response.put("newStatus", newStatus);
        if (user.getBanEndTime() != null) {
            response.put("banEndTime", user.getBanEndTime());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        User user = userService.getUserRepository().selectById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
        }

        userService.getUserRepository().deleteById(userId);

        // 记录系统日志
        systemLogService.log("删除用户", "用户管理", "管理员删除用户: " + user.getUsername(), request);

        return ResponseEntity.ok(Map.of(
                "message", "用户删除成功",
                "userId", userId));
    }

    /**
     * 获取猫咪列表
     */
    @GetMapping("/cats")
    public ResponseEntity<Map<String, Object>> getCats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        QueryWrapper<Cat> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("name", keyword)
                    .or().like("breed", keyword)
                    .or().like("location", keyword);
        }
        queryWrapper.orderByDesc("create_time");

        IPage<Cat> catPage = catService.getCatRepository()
                .selectPage(new Page<>(page, size), queryWrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("cats", catPage.getRecords());
        response.put("total", catPage.getTotal());
        response.put("page", page);
        response.put("size", size);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(HttpServletRequest request) {
        // 获取用户统计
        long totalUsers = userService.getUserRepository().selectCount(new QueryWrapper<>());
        long activeUsers = userService.getUserRepository()
                .selectCount(new QueryWrapper<User>().eq("status", "ACTIVE"));
        long rescuers = userService.getUserRepository()
                .selectCount(new QueryWrapper<User>().eq("role", "RESCUER"));
        long admins = userService.getUserRepository()
                .selectCount(new QueryWrapper<User>().eq("role", "ADMIN"));

        // 获取猫咪统计
        long totalCats = catService.getCatRepository().selectCount(new QueryWrapper<>());
        long availableCats = catService.getCatRepository()
                .selectCount(new QueryWrapper<Cat>().eq("status", "待领养"));
        long adoptedCats = catService.getCatRepository()
                .selectCount(new QueryWrapper<Cat>().eq("status", "已领养"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("rescuers", rescuers);
        stats.put("admins", admins);
        stats.put("totalCats", totalCats);
        stats.put("availableCats", availableCats);
        stats.put("adoptedCats", adoptedCats);

        // 记录系统日志
        systemLogService.log("查看统计", "数据统计", "管理员查看系统统计数据", request);

        return ResponseEntity.ok(stats);
    }

    /**
     * 导出猫咪数据
     */
    @GetMapping("/cats/export")
    public ResponseEntity<byte[]> exportCats(HttpServletRequest request) {
        try {
            // 获取所有猫咪数据
            List<Cat> cats = catRepository.selectList(new QueryWrapper<Cat>());

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("猫咪数据");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "猫咪名称", "品种", "年龄", "体重", "性别", "健康状况", "医疗信息", "性格", "描述", "位置", "状态", "审核状态",
                    "审核时间", "创建时间", "更新时间" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            int rowNum = 1;
            for (Cat cat : cats) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(cat.getId());
                row.createCell(1).setCellValue(cat.getName() != null ? cat.getName() : "");
                row.createCell(2).setCellValue(cat.getBreed() != null ? cat.getBreed() : "");
                row.createCell(3).setCellValue(cat.getAge() != null ? cat.getAge() : 0);
                row.createCell(4).setCellValue(cat.getWeight() != null ? cat.getWeight() : 0);
                row.createCell(5).setCellValue(cat.getGender() != null ? cat.getGender() : "");
                row.createCell(6).setCellValue(cat.getHealthStatus() != null ? cat.getHealthStatus() : "");
                row.createCell(7).setCellValue(cat.getMedicalInfo() != null ? cat.getMedicalInfo() : "");
                row.createCell(8).setCellValue(cat.getPersonality() != null ? cat.getPersonality() : "");
                row.createCell(9).setCellValue(cat.getDescription() != null ? cat.getDescription() : "");
                row.createCell(10).setCellValue(cat.getLocation() != null ? cat.getLocation() : "");
                row.createCell(11).setCellValue(cat.getStatus() != null ? cat.getStatus() : "");
                row.createCell(12).setCellValue(cat.getAuditStatus() != null ? cat.getAuditStatus() : "");
                row.createCell(13).setCellValue(cat.getAuditTime() != null ? cat.getAuditTime().toString() : "");
                row.createCell(14).setCellValue(cat.getCreateTime() != null ? cat.getCreateTime().toString() : "");
                row.createCell(15).setCellValue(cat.getUpdateTime() != null ? cat.getUpdateTime().toString() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // 设置响应头
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment",
                    "猫咪数据_" + LocalDate.now().toString() + ".xlsx");

            // 记录系统日志
            systemLogService.log("导出数据", "数据管理", "管理员导出用户数据报表", request);

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new byte[0]);
        }
    }

    /**
     * 导出用户数据
     */
    @GetMapping("/users/export")
    public ResponseEntity<byte[]> exportUsers(HttpServletRequest request) {
        try {
            // 获取所有用户数据
            List<User> users = userRepository.selectList(new QueryWrapper<User>());

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("用户数据");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "用户名", "邮箱", "电话", "角色", "状态", "创建时间", "更新时间" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getPhone());
                row.createCell(4).setCellValue(user.getRole());
                row.createCell(5).setCellValue(user.getStatus());
                row.createCell(6).setCellValue(user.getCreateTime() != null ? user.getCreateTime().toString() : "");
                row.createCell(7).setCellValue(user.getUpdateTime() != null ? user.getUpdateTime().toString() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // 设置响应头
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment",
                    "用户数据_" + LocalDate.now().toString() + ".xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new byte[0]);
        }
    }

    /**
     * 获取待审核猫咪列表
     */
    @GetMapping("/pending-cats")
    public ResponseEntity<Map<String, Object>> getPendingCats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 从数据库查询待审核猫咪
            IPage<Cat> pendingCats = catService.getCatsByAuditStatus("PENDING", page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("data", pendingCats.getRecords());
            response.put("total", pendingCats.getTotal());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取待审核猫咪列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取已通过猫咪列表
     */
    @GetMapping("/approved-cats")
    public ResponseEntity<Map<String, Object>> getApprovedCats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 从数据库查询已通过猫咪
            IPage<Cat> approvedCats = catService.getCatsByAuditStatus("APPROVED", page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("data", approvedCats.getRecords());
            response.put("total", approvedCats.getTotal());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取已通过猫咪列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取已拒绝猫咪列表
     */
    @GetMapping("/rejected-cats")
    public ResponseEntity<Map<String, Object>> getRejectedCats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 从数据库查询已拒绝猫咪
            IPage<Cat> rejectedCats = catService.getCatsByAuditStatus("REJECTED", page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("data", rejectedCats.getRecords());
            response.put("total", rejectedCats.getTotal());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取已拒绝猫咪列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取待审核帖子列表
     */
    @GetMapping("/pending-posts")
    public ResponseEntity<Map<String, Object>> getPendingPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 调用实际的帖子审核服务获取待审核帖子
            IPage<Post> pendingPosts = postService.getPendingPosts(page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("data", pendingPosts.getRecords());
            response.put("total", pendingPosts.getTotal());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取待审核帖子列表失败: " + e.getMessage()));
        }
    }

    /**
     * 审核通过帖子
     */
    @PostMapping("/posts/{postId}/approve")
    public ResponseEntity<Map<String, Object>> approvePost(@PathVariable Long postId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String remark = request != null ? request.get("remark") : null;
            Post approvedPost = postService.approvePost(postId, remark);

            // 发送帖子审核通过通知
            String messageData = String.format(
                    "{\"postId\":%d,\"action\":\"APPROVE\",\"userId\":%d,\"status\":\"APPROVED\",\"reviewComment\":\"%s\"}",
                    approvedPost.getId(), approvedPost.getAuthorId(), remark != null ? remark : "");
            RescueWebSocketHandler.notifyPostReviewUpdate(messageData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "帖子审核通过成功");
            response.put("data", approvedPost);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审核通过帖子失败: " + e.getMessage()));
        }
    }

    /**
     * 审核拒绝帖子
     */
    @PostMapping("/posts/{postId}/reject")
    public ResponseEntity<Map<String, Object>> rejectPost(@PathVariable Long postId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String remark = request != null ? request.get("remark") : null;
            Post rejectedPost = postService.rejectPost(postId, remark);

            // 发送帖子审核拒绝通知
            String messageData = String.format(
                    "{\"postId\":%d,\"action\":\"REJECT\",\"userId\":%d,\"status\":\"REJECTED\",\"reviewComment\":\"%s\"}",
                    rejectedPost.getId(), rejectedPost.getAuthorId(), remark != null ? remark : "");
            RescueWebSocketHandler.notifyPostReviewUpdate(messageData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "帖子审核拒绝成功");
            response.put("data", rejectedPost);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审核拒绝帖子失败: " + e.getMessage()));
        }
    }

    /**
     * 获取帖子统计信息
     */
    @GetMapping("/posts/statistics")
    public ResponseEntity<Map<String, Object>> getPostStatistics() {
        try {
            // 获取待审核帖子数量
            long pendingCount = postService.getPendingPosts(1, 1).getTotal();

            // 获取已通过帖子数量 - 通过查询所有帖子然后过滤
            IPage<Post> allPosts = postService.getPosts(1, 1000, null);
            long approvedCount = allPosts.getRecords().stream()
                    .filter(post -> "APPROVED".equals(post.getStatus()))
                    .count();

            // 获取已拒绝帖子数量
            long rejectedCount = allPosts.getRecords().stream()
                    .filter(post -> "REJECTED".equals(post.getStatus()))
                    .count();

            // 获取总帖子数量
            long totalCount = allPosts.getTotal();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("pendingCount", pendingCount);
            statistics.put("approvedCount", approvedCount);
            statistics.put("rejectedCount", rejectedCount);
            statistics.put("totalCount", totalCount);

            Map<String, Object> response = new HashMap<>();
            response.put("data", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取帖子统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取救助需求列表
     */
    @GetMapping("/pending-rescues")
    public ResponseEntity<Map<String, Object>> getPendingRescues(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 使用现有的RescueService获取所有救助需求 - 不限制状态
            IPage<Rescue> allRescues = rescueService.getRescues(page, size, null, null, null);

            // 转换为前端需要的格式
            List<Map<String, Object>> rescues = new ArrayList<>();
            for (Rescue rescue : allRescues.getRecords()) {
                Map<String, Object> rescueData = new HashMap<>();
                rescueData.put("id", rescue.getId());
                rescueData.put("title", rescue.getTitle());
                rescueData.put("location", rescue.getLocation());
                rescueData.put("description", rescue.getDescription());
                rescueData.put("urgencyLevel", rescue.getUrgencyLevel());
                rescueData.put("status", rescue.getStatus());
                rescueData.put("reporterUsername", rescue.getReporterUsername());
                rescueData.put("contactPhone", rescue.getContactPhone());
                rescueData.put("createTime", rescue.getCreateTime());
                rescueData.put("volunteerName", rescue.getVolunteerName());
                rescueData.put("volunteerPhone", rescue.getVolunteerPhone());
                rescueData.put("time", formatTime(rescue.getCreateTime()));

                // 添加详情对话框需要的字段
                rescueData.put("images", rescue.getImages());
                rescueData.put("rescueLogImages", rescue.getRescueLogImages());
                rescueData.put("rescuerId", rescue.getRescuerId());
                rescueData.put("rescuerUsername", rescue.getRescuerUsername());
                rescueData.put("acceptTime", rescue.getAcceptTime());
                rescueData.put("completeTime", rescue.getCompleteTime());
                rescueData.put("catCondition", rescue.getCatCondition());

                rescues.add(rescueData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", rescues);
            response.put("total", allRescues.getTotal());
            response.put("current", allRescues.getCurrent());
            response.put("size", allRescues.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取救助需求列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取待审核领养列表
     */
    @GetMapping("/pending-adoptions")
    public ResponseEntity<Map<String, Object>> getPendingAdoptions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 使用现有的AdoptionService获取待审核领养列表
            IPage<Adoption> pendingAdoptions = adoptionService.getPendingApplications(page, size);

            // 转换为前端需要的格式
            List<Map<String, Object>> adoptions = new ArrayList<>();
            for (Adoption adoption : pendingAdoptions.getRecords()) {
                Map<String, Object> adoptionData = new HashMap<>();
                adoptionData.put("id", adoption.getId());
                adoptionData.put("catName", adoption.getCatName());
                adoptionData.put("applicant", adoption.getApplicantName());
                adoptionData.put("userId", adoption.getUserId()); // 账号ID
                adoptionData.put("applicantId", adoption.getApplicantId()); // 申请人ID
                adoptionData.put("catId", adoption.getCatId());
                // 使用现有的申请理由字段
                adoptionData.put("reason", adoption.getApplicationReason());
                adoptionData.put("createTime", adoption.getCreateTime()); // 返回原始时间，由前端格式化

                // 添加猫咪详细信息
                adoptionData.put("catAge", adoption.getCatAge());
                adoptionData.put("catGender", adoption.getCatGender());
                adoptionData.put("catWeight", adoption.getCatWeight());
                adoptionData.put("catHealth", adoption.getCatHealth());
                adoptionData.put("catLocation", adoption.getCatLocation());
                adoptionData.put("catPersonality", adoption.getCatPersonality());
                adoptionData.put("catDescription", adoption.getCatDescription());
                adoptionData.put("catImage", adoption.getCatImage());

                // 添加申请人详细信息
                if (adoption.getApplicant() != null) {
                    Applicant applicant = adoption.getApplicant();
                    adoptionData.put("applicantIdCard", applicant.getIdCard());
                    adoptionData.put("applicantPhone", applicant.getPhone());
                    adoptionData.put("applicantAddress", applicant.getAddress());
                    adoptionData.put("livingCondition", applicant.getLivingCondition());
                    adoptionData.put("experience", applicant.getExperience());
                    adoptionData.put("familyMembers", applicant.getFamilyMembers());
                    adoptionData.put("financialStatus", applicant.getFinancialStatus());
                    adoptionData.put("workTime", applicant.getWorkSchedule());
                    adoptionData.put("otherInfo", applicant.getAdditionalInfo());
                }

                adoptions.add(adoptionData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", adoptions);
            response.put("total", pendingAdoptions.getTotal());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取待审核领养列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取已通过领养列表
     */
    @GetMapping("/approved-adoptions")
    public ResponseEntity<Map<String, Object>> getApprovedAdoptions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 使用现有的AdoptionService获取已通过领养列表
            IPage<Adoption> approvedAdoptions = adoptionService.getApprovedApplications(page, size);

            // 转换为前端需要的格式
            List<Map<String, Object>> adoptions = new ArrayList<>();
            for (Adoption adoption : approvedAdoptions.getRecords()) {
                Map<String, Object> adoptionData = new HashMap<>();
                adoptionData.put("id", adoption.getId());
                adoptionData.put("catName", adoption.getCatName());
                adoptionData.put("applicant", adoption.getApplicantName());
                adoptionData.put("userId", adoption.getUserId()); // 账号ID
                adoptionData.put("applicantId", adoption.getApplicantId()); // 申请人ID
                adoptionData.put("catId", adoption.getCatId());
                adoptionData.put("reason", adoption.getApplicationReason());
                adoptionData.put("createTime", adoption.getCreateTime()); // 返回原始时间，由前端格式化
                adoptionData.put("approveTime", adoption.getProcessTime()); // 使用processTime作为审核时间
                adoptions.add(adoptionData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", adoptions);
            response.put("total", approvedAdoptions.getTotal());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取已通过领养列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取已拒绝领养列表
     */
    @GetMapping("/rejected-adoptions")
    public ResponseEntity<Map<String, Object>> getRejectedAdoptions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 使用现有的AdoptionService获取已拒绝领养列表
            IPage<Adoption> rejectedAdoptions = adoptionService.getRejectedApplications(page, size);

            // 转换为前端需要的格式
            List<Map<String, Object>> adoptions = new ArrayList<>();
            for (Adoption adoption : rejectedAdoptions.getRecords()) {
                Map<String, Object> adoptionData = new HashMap<>();
                adoptionData.put("id", adoption.getId());
                adoptionData.put("catName", adoption.getCatName());
                adoptionData.put("applicant", adoption.getApplicantName());
                adoptionData.put("userId", adoption.getUserId()); // 账号ID
                adoptionData.put("applicantId", adoption.getApplicantId()); // 申请人ID
                adoptionData.put("catId", adoption.getCatId());
                adoptionData.put("reason", adoption.getApplicationReason());
                adoptionData.put("createTime", adoption.getCreateTime()); // 返回原始时间，由前端格式化
                adoptionData.put("rejectReason", adoption.getReviewComment()); // 使用reviewComment作为拒绝理由
                adoptionData.put("rejectTime", adoption.getProcessTime()); // 使用processTime作为拒绝时间
                adoptions.add(adoptionData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", adoptions);
            response.put("total", rejectedAdoptions.getTotal());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取已拒绝领养列表失败: " + e.getMessage()));
        }
    }

    /**
     * 审核猫咪
     */
    @PostMapping("/cats/{catId}/review")
    public ResponseEntity<Map<String, Object>> reviewCat(@PathVariable Long catId,
            @RequestBody Map<String, String> request) {
        try {
            String action = request.get("action");
            String remark = request.get("remark");

            // 根据action确定审核状态
            String auditStatus = "approve".equals(action) ? "APPROVED" : "REJECTED";

            // 调用服务层审核猫咪
            Cat cat = catService.auditCat(catId, auditStatus, remark);

            return ResponseEntity.ok(Map.of(
                    "message", "猫咪审核" + ("approve".equals(action) ? "通过" : "拒绝") + "成功",
                    "catId", catId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "猫咪审核失败: " + e.getMessage()));
        }
    }

    /**
     * 审核帖子
     */
    @PostMapping("/posts/{postId}/review")
    public ResponseEntity<Map<String, Object>> reviewPost(@PathVariable Long postId,
            @RequestBody Map<String, String> request) {
        try {
            String action = request.get("action");
            String remark = request.get("remark");

            Post post;
            if ("approve".equals(action)) {
                post = postService.approvePost(postId, remark);

                // 发送帖子审核通过通知
                System.out.println("开始发送帖子审核通过WebSocket消息...");
                String messageData = String.format(
                        "{\"postId\":%d,\"action\":\"APPROVE\",\"userId\":%d,\"status\":\"APPROVED\",\"reviewComment\":\"%s\"}",
                        post.getId(), post.getAuthorId(), remark != null ? remark : "");
                System.out.println("帖子审核通过消息数据: " + messageData);
                RescueWebSocketHandler.notifyPostReviewUpdate(messageData);
                System.out.println("帖子审核通过WebSocket消息发送完成");

            } else if ("reject".equals(action)) {
                post = postService.rejectPost(postId, remark);

                // 发送帖子审核拒绝通知
                System.out.println("开始发送帖子审核拒绝WebSocket消息...");
                String messageData = String.format(
                        "{\"postId\":%d,\"action\":\"REJECT\",\"userId\":%d,\"status\":\"REJECTED\",\"reviewComment\":\"%s\"}",
                        post.getId(), post.getAuthorId(), remark != null ? remark : "");
                System.out.println("帖子审核拒绝消息数据: " + messageData);
                RescueWebSocketHandler.notifyPostReviewUpdate(messageData);
                System.out.println("帖子审核拒绝WebSocket消息发送完成");

            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的操作类型"));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "帖子审核" + ("approve".equals(action) ? "通过" : "拒绝") + "成功",
                    "postId", postId,
                    "data", post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "帖子审核失败: " + e.getMessage()));
        }
    }

    /**
     * 获取已拒绝帖子列表
     */
    @GetMapping("/rejected-posts")
    public ResponseEntity<Map<String, Object>> getRejectedPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Post> rejectedPosts = postService.getRejectedPosts(page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("data", rejectedPosts.getRecords());
            response.put("total", rejectedPosts.getTotal());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取已拒绝帖子列表失败: " + e.getMessage()));
        }
    }

    /**
     * 处理救助
     */
    @PostMapping("/rescues/{rescueId}/process")
    public ResponseEntity<Map<String, Object>> processRescue(@PathVariable Long rescueId,
            @RequestBody Map<String, String> request) {
        try {
            String action = request.get("action");
            // 实际项目中需要实现处理逻辑
            return ResponseEntity.ok(Map.of(
                    "message", "救助" + ("accept".equals(action) ? "受理" : "分配") + "成功",
                    "rescueId", rescueId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "救助处理失败: " + e.getMessage()));
        }
    }

    /**
     * 获取领养申请详情
     */
    @GetMapping("/adoptions/{adoptionId}")
    public ResponseEntity<Map<String, Object>> getAdoptionDetail(@PathVariable Long adoptionId) {
        try {
            // 使用现有的AdoptionService获取领养申请详情
            Adoption adoption = adoptionService.getAdoptionById(adoptionId);

            if (adoption == null) {
                return ResponseEntity.notFound().build();
            }

            // 转换为前端需要的格式
            Map<String, Object> adoptionData = new HashMap<>();
            adoptionData.put("id", adoption.getId());
            adoptionData.put("catName", adoption.getCatName());
            adoptionData.put("applicant", adoption.getApplicantName());
            adoptionData.put("userId", adoption.getUserId()); // 账号ID
            adoptionData.put("applicantId", adoption.getApplicantId()); // 申请人ID
            adoptionData.put("catId", adoption.getCatId());
            adoptionData.put("reason", adoption.getApplicationReason());
            adoptionData.put("time", formatTime(adoption.getCreateTime()));

            // 猫咪详细信息
            adoptionData.put("catAge", adoption.getCatAge());
            adoptionData.put("catGender", adoption.getCatGender());
            adoptionData.put("catWeight", adoption.getCatWeight());
            adoptionData.put("catHealth", adoption.getCatHealth());
            adoptionData.put("catLocation", adoption.getCatLocation());
            adoptionData.put("catPersonality", adoption.getCatPersonality());
            adoptionData.put("catDescription", adoption.getCatDescription());
            adoptionData.put("catMedical", adoption.getCatMedical()); // 医疗信息
            adoptionData.put("catImage", adoption.getCatImage()); // 猫咪图片

            // 申请人详细信息
            adoptionData.put("idCard", adoption.getIdCard());
            adoptionData.put("phone", adoption.getPhone());
            adoptionData.put("address", adoption.getAddress());
            adoptionData.put("livingCondition", adoption.getLivingCondition());
            adoptionData.put("experience", adoption.getExperience());
            adoptionData.put("familyMembers", adoption.getFamilyMembers());
            adoptionData.put("financialStatus", adoption.getFinancialStatus());
            adoptionData.put("workSchedule", adoption.getWorkSchedule());
            adoptionData.put("additionalInfo", adoption.getAdditionalInfo());

            // 审核信息
            adoptionData.put("status", adoption.getStatus());
            adoptionData.put("processTime", adoption.getProcessTime());
            adoptionData.put("approveTime", adoption.getApproveTime());
            adoptionData.put("reviewComment", adoption.getReviewComment());

            Map<String, Object> response = new HashMap<>();
            response.put("data", adoptionData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取领养申请详情失败: " + e.getMessage()));
        }
    }

    /**
     * 审核领养
     */
    @PostMapping("/adoptions/{adoptionId}/review")
    public ResponseEntity<Map<String, Object>> reviewAdoption(@PathVariable Long adoptionId,
            @RequestBody Map<String, String> request) {
        try {
            String action = request.get("action");

            if ("approve".equals(action)) {
                // 使用现有的AdoptionService审核通过
                Adoption approvedAdoption = adoptionService.approveApplication(adoptionId);
                return ResponseEntity.ok(Map.of(
                        "message", "领养审核通过成功",
                        "adoptionId", adoptionId));
            } else if ("reject".equals(action)) {
                // 使用现有的AdoptionService审核拒绝
                String reviewComment = request.get("reviewComment");
                if (reviewComment == null) {
                    reviewComment = "审核未通过";
                }
                Adoption rejectedAdoption = adoptionService.rejectApplication(adoptionId, reviewComment);
                return ResponseEntity.ok(Map.of(
                        "message", "领养审核拒绝成功",
                        "adoptionId", adoptionId));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的操作类型"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "领养审核失败: " + e.getMessage()));
        }
    }

    /**
     * 时间格式化方法
     */
    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "未知时间";
        }

        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(dateTime, now);
        long diffMinutes = duration.toMinutes();
        long diffHours = duration.toHours();
        long diffDays = duration.toDays();

        if (diffMinutes < 1) { // 1分钟内
            return "刚刚";
        } else if (diffMinutes < 60) { // 1小时内
            return diffMinutes + "分钟前";
        } else if (diffHours < 24) { // 24小时内
            return diffHours + "小时前";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }
}
