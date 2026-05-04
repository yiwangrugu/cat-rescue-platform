package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catrescue.catrescueplatform.entity.Adoption;
import com.catrescue.catrescueplatform.entity.Cat;
import com.catrescue.catrescueplatform.entity.Rescue;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.entity.Volunteer;
import com.catrescue.catrescueplatform.repository.AdoptionRepository;
import com.catrescue.catrescueplatform.repository.CatRepository;
import com.catrescue.catrescueplatform.repository.RescueRepository;
import com.catrescue.catrescueplatform.repository.UserRepository;
import com.catrescue.catrescueplatform.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class RescueService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final RescueRepository rescueRepository;
    private final UserRepository userRepository;
    private final AdoptionRepository adoptionRepository;
    private final CatRepository catRepository;
    private final VolunteerRepository volunteerRepository;

    private void fillUserInfo(Rescue rescue) {
        if (rescue != null) {
            // 填充报告人信息
            if (rescue.getReporterId() != null) {
                User reporter = userRepository.selectById(rescue.getReporterId());
                if (reporter != null) {
                    rescue.setReporterUsername(reporter.getUsername());
                    rescue.setReporterAvatar(reporter.getAvatar());
                }
            }
            // 填充救助者信息
            if (rescue.getRescuerId() != null) {
                User rescuer = userRepository.selectById(rescue.getRescuerId());
                if (rescuer != null) {
                    rescue.setRescuerUsername(rescuer.getUsername());
                    rescue.setRescuerAvatar(rescuer.getAvatar());
                }
            }
        }
    }

    private void fillUserInfo(List<Rescue> rescues) {
        for (Rescue rescue : rescues) {
            fillUserInfo(rescue);
        }
    }

    private void fillUserInfo(IPage<Rescue> page) {
        if (page != null && page.getRecords() != null) {
            fillUserInfo(page.getRecords());
        }
    }

    public IPage<Rescue> getRescues(int page, int size, String status, String urgencyLevel, String search) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();

        // 处理状态筛选 - 支持中英文状态值
        if (status != null && !status.isEmpty()) {
            String dbStatus = convertStatusToDbValue(status);
            if (dbStatus != null) {
                wrapper.eq("status", dbStatus);
            }
        }

        // 处理紧急程度筛选 - 支持中英文紧急程度值
        if (urgencyLevel != null && !urgencyLevel.isEmpty()) {
            String dbUrgencyLevel = convertUrgencyLevelToDbValue(urgencyLevel);
            if (dbUrgencyLevel != null) {
                wrapper.eq("urgency_level", dbUrgencyLevel);
            }
        }

        // 处理搜索
        if (search != null && !search.isEmpty()) {
            wrapper.and(w -> w.like("title", search).or().like("location", search));
        }

        wrapper.orderByDesc("create_time");

        IPage<Rescue> rescuePage = rescueRepository.selectPage(new Page<>(page, size), wrapper);
        fillUserInfo(rescuePage);
        return rescuePage;
    }

    // 将前端状态值转换为数据库存储的值
    private String convertStatusToDbValue(String status) {
        switch (status) {
            case "待处理":
            case "PENDING":
                return "PENDING";
            case "进行中":
            case "IN_PROGRESS":
                return "IN_PROGRESS";
            case "已完成":
            case "COMPLETED":
                return "COMPLETED";
            case "已取消":
            case "CANCELLED":
                return "CANCELLED";
            default:
                return status; // 如果无法识别，直接使用原值
        }
    }

    // 将前端紧急程度值转换为数据库存储的值
    private String convertUrgencyLevelToDbValue(String urgencyLevel) {
        // 数据库存储的是中文值，直接返回中文值
        switch (urgencyLevel) {
            case "低":
            case "LOW":
                return "低";
            case "中":
            case "MEDIUM":
                return "中";
            case "高":
            case "HIGH":
                return "高";
            case "紧急":
            case "CRITICAL":
                return "紧急";
            default:
                return urgencyLevel; // 如果无法识别，直接使用原值
        }
    }

    public Rescue getRescueById(Long id) {
        Rescue rescue = rescueRepository.selectById(id);
        fillUserInfo(rescue);
        return rescue;
    }

    public Rescue createRescue(Rescue rescue) {
        rescueRepository.insert(rescue);

        // 更新用户的救助需求数
        if (rescue.getReporterId() != null) {
            User user = userRepository.selectById(rescue.getReporterId());
            if (user != null) {
                user.setRescueCount((user.getRescueCount() != null ? user.getRescueCount() : 0) + 1);
                userRepository.updateById(user);
                System.out.println("用户救助需求数更新: " + user.getUsername() + " 救助数: " + user.getRescueCount());
            }
        }

        fillUserInfo(rescue);
        return rescue;
    }

    public Rescue updateRescue(Rescue rescue) {
        rescueRepository.updateById(rescue);
        fillUserInfo(rescue);
        return rescue;
    }

    public void deleteRescue(Long id) {
        rescueRepository.deleteById(id);
    }

    public List<Rescue> getRescuesByReporter(Long reporterId) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        wrapper.eq("reporter_id", reporterId);
        wrapper.orderByDesc("create_time");
        List<Rescue> rescues = rescueRepository.selectList(wrapper);
        fillUserInfo(rescues);
        return rescues;
    }

    public List<Rescue> getRescuesByRescuer(Long rescuerId) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        if (rescuerId != null) {
            wrapper.eq("rescuer_id", rescuerId);
        }
        wrapper.orderByDesc("create_time");
        List<Rescue> rescues = rescueRepository.selectList(wrapper);
        fillUserInfo(rescues);
        return rescues;
    }

    public Rescue takeRescue(Long id, Long rescuerId) {
        Rescue rescue = rescueRepository.selectById(id);
        if (rescue != null && "PENDING".equals(rescue.getStatus())) {
            rescue.setStatus("IN_PROGRESS");
            if (rescuerId != null) {
                rescue.setRescuerId(rescuerId);
            }
            rescue.setAcceptTime(java.time.LocalDateTime.now());
            rescueRepository.updateById(rescue);
            fillUserInfo(rescue);

            // 打印更新后的救援信息
            System.out.println("更新后的救援信息:");
            System.out.println("状态: " + rescue.getStatus());
            System.out.println("负责人ID: " + rescue.getRescuerId());
            System.out.println("完成时间: " + rescue.getCompleteTime());
        } else {
            System.out.println("救援任务不存在或状态不是'进行中'");
            if (rescue == null) {
                System.out.println("救援任务不存在，ID: " + id);
            } else {
                System.out.println("救援任务状态: " + rescue.getStatus());
            }
        }
        return rescue;
    }

    public Rescue takeRescueWithVolunteer(Long id, com.catrescue.catrescueplatform.dto.TakeRescueRequest request) {
        Rescue rescue = rescueRepository.selectById(id);
        if (rescue != null && "待处理".equals(rescue.getStatus())) {
            rescue.setStatus("进行中");
            if (request.getRescuerId() != null) {
                rescue.setRescuerId(request.getRescuerId());
            }
            if (request.getVolunteerName() != null) {
                rescue.setVolunteerName(request.getVolunteerName());
            }
            if (request.getVolunteerPhone() != null) {
                rescue.setVolunteerPhone(request.getVolunteerPhone());
            }
            rescue.setAcceptTime(java.time.LocalDateTime.now());
            rescueRepository.updateById(rescue);
            fillUserInfo(rescue);

            // 如果选择了志愿者，将其状态设置为忙碌
            if (request.getVolunteerName() != null && request.getVolunteerPhone() != null) {
                // 根据志愿者姓名和电话查找志愿者
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Volunteer> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                wrapper.eq("name", request.getVolunteerName());
                wrapper.eq("phone", request.getVolunteerPhone());
                Volunteer volunteer = volunteerRepository.selectOne(wrapper);
                if (volunteer != null && "空闲".equals(volunteer.getStatus())) {
                    volunteer.setStatus("忙碌");
                    volunteerRepository.updateById(volunteer);
                    System.out.println("志愿者 " + volunteer.getName() + " 状态已设置为忙碌");
                }
            }
        }
        return rescue;
    }

    public Rescue completeRescue(Long id, Long rescuerId) {
        Rescue rescue = rescueRepository.selectById(id);
        if (rescue != null && "进行中".equals(rescue.getStatus())) {
            rescue.setStatus("已完成");
            rescue.setCompleteTime(java.time.LocalDateTime.now());

            // 设置负责人ID
            if (rescuerId != null) {
                rescue.setRescuerId(rescuerId);
            }

            rescueRepository.updateById(rescue);
            fillUserInfo(rescue);

            // 如果救援任务有志愿者信息，将其状态恢复为空闲
            if (rescue.getVolunteerName() != null && rescue.getVolunteerPhone() != null) {
                // 根据志愿者姓名和电话查找志愿者
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Volunteer> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                wrapper.eq("name", rescue.getVolunteerName());
                wrapper.eq("phone", rescue.getVolunteerPhone());
                Volunteer volunteer = volunteerRepository.selectOne(wrapper);
                if (volunteer != null && "忙碌".equals(volunteer.getStatus())) {
                    volunteer.setStatus("空闲");
                    volunteerRepository.updateById(volunteer);
                    System.out.println("志愿者 " + volunteer.getName() + " 状态已恢复为空闲");
                }
            }
        }
        return rescue;
    }

    public Rescue completeRescueWithImages(Long id,
            java.util.List<org.springframework.web.multipart.MultipartFile> rescueLogImages,
            Long rescuerId) {
        System.out.println("completeRescueWithImages - 接收到的参数:");
        System.out.println("救援ID: " + id);
        System.out.println("负责人ID: " + rescuerId);
        System.out.println("图片数量: " + (rescueLogImages != null ? rescueLogImages.size() : 0));

        Rescue rescue = rescueRepository.selectById(id);
        if (rescue != null && "进行中".equals(rescue.getStatus())) {
            System.out.println("救援任务状态: " + rescue.getStatus());
            System.out.println("原有负责人ID: " + rescue.getRescuerId());

            rescue.setStatus("已完成");
            rescue.setCompleteTime(java.time.LocalDateTime.now());

            // 设置负责人ID
            if (rescuerId != null) {
                System.out.println("设置新的负责人ID: " + rescuerId);
                rescue.setRescuerId(rescuerId);
            } else {
                System.out.println("负责人ID为空，使用原有负责人ID: " + rescue.getRescuerId());
            }

            // 处理救援日志图片上传
            if (rescueLogImages != null && !rescueLogImages.isEmpty()) {
                try {
                    java.util.List<String> imageUrls = new java.util.ArrayList<>();

                    // 实现真正的图片上传逻辑
                    for (org.springframework.web.multipart.MultipartFile image : rescueLogImages) {
                        if (!image.isEmpty()) {
                            // 检查文件类型
                            String contentType = image.getContentType();
                            if (contentType == null || (!contentType.startsWith("image/jpeg")
                                    && !contentType.startsWith("image/png"))) {
                                continue; // 跳过非图片文件
                            }

                            // 生成文件名
                            String originalFilename = image.getOriginalFilename();
                            String extension = "";
                            if (originalFilename != null && originalFilename.contains(".")) {
                                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                            }
                            String newFilename = "rescue_log_" + System.currentTimeMillis() + "_"
                                    + java.util.UUID.randomUUID().toString().substring(0, 8) + extension;

                            // 保存文件到服务器
                            Path uploadPath = Paths.get(uploadDir);
                            if (!Files.exists(uploadPath)) {
                                Files.createDirectories(uploadPath);
                            }

                            Path filePath = uploadPath.resolve(newFilename);
                            Files.copy(image.getInputStream(), filePath);

                            // 生成文件访问URL
                            String imageUrl = "/uploads/" + newFilename;
                            imageUrls.add(imageUrl);
                        }
                    }

                    // 将图片URL保存到数据库（JSON数组格式）
                    if (!imageUrls.isEmpty()) {
                        rescue.setRescueLogImages(
                                new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(imageUrls));
                    }
                } catch (Exception e) {
                    // 图片上传失败不影响救援完成操作
                    System.err.println("救援日志图片上传失败: " + e.getMessage());
                }
            }

            rescueRepository.updateById(rescue);
            fillUserInfo(rescue);

            // 如果救援任务有志愿者信息，将其状态恢复为空闲
            if (rescue.getVolunteerName() != null && rescue.getVolunteerPhone() != null) {
                // 根据志愿者姓名和电话查找志愿者
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Volunteer> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                wrapper.eq("name", rescue.getVolunteerName());
                wrapper.eq("phone", rescue.getVolunteerPhone());
                Volunteer volunteer = volunteerRepository.selectOne(wrapper);
                if (volunteer != null && "忙碌".equals(volunteer.getStatus())) {
                    volunteer.setStatus("空闲");
                    volunteerRepository.updateById(volunteer);
                    System.out.println("志愿者 " + volunteer.getName() + " 状态已恢复为空闲");
                }
            }
        }
        return rescue;
    }

    /**
     * 获取总救助任务数量
     */
    public long getTotalRescues(Long rescuerId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        // 统计总的救助需求数据，不按救援者ID筛选

        // 支持日期范围筛选
        if (startTime != null && endTime != null) {
            wrapper.ge("create_time", startTime);
            wrapper.le("create_time", endTime);
        }

        // 调试信息：打印查询条件和结果
        System.out.println("总救助任务查询条件: " + wrapper.getCustomSqlSegment());
        long count = rescueRepository.selectCount(wrapper);
        System.out.println("总救助任务数量: " + count);
        return count;
    }

    /**
     * 获取待处理救助数量
     */
    public long getPendingRescues(Long rescuerId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        // 数据库中可能同时存在中英文状态值，同时查询
        wrapper.and(w -> w.eq("status", "待处理").or().eq("status", "PENDING"));
        // 统计总的救助需求数据，不按救援者ID筛选

        // 支持日期范围筛选
        if (startTime != null && endTime != null) {
            wrapper.ge("create_time", startTime);
            wrapper.le("create_time", endTime);
        }

        // 调试信息：打印查询条件和结果
        System.out.println("待处理救助查询条件: " + wrapper.getCustomSqlSegment());
        long count = rescueRepository.selectCount(wrapper);
        System.out.println("待处理救助数量: " + count);
        return count;
    }

    /**
     * 获取进行中救助数量
     */
    public long getInProgressRescues(Long rescuerId, java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        // 数据库中可能同时存在中英文状态值，同时查询
        wrapper.and(w -> w.eq("status", "进行中").or().eq("status", "IN_PROGRESS"));
        // 统计总的救助需求数据，不按救援者ID筛选

        // 支持日期范围筛选
        if (startTime != null && endTime != null) {
            wrapper.ge("create_time", startTime);
            wrapper.le("create_time", endTime);
        }

        // 调试信息：打印查询条件和结果
        System.out.println("进行中救助查询条件: " + wrapper.getCustomSqlSegment());
        long count = rescueRepository.selectCount(wrapper);
        System.out.println("进行中救助数量: " + count);
        return count;
    }

    /**
     * 获取已完成救助数量
     */
    public long getCompletedRescues(Long rescuerId, java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        // 数据库中可能同时存在中英文状态值，同时查询
        wrapper.and(w -> w.eq("status", "已完成").or().eq("status", "COMPLETED"));
        // 统计总的救助需求数据，不按救援者ID筛选

        // 支持日期范围筛选
        if (startTime != null && endTime != null) {
            wrapper.ge("create_time", startTime);
            wrapper.le("create_time", endTime);
        }

        // 调试信息：打印查询条件和结果
        System.out.println("已完成救助查询条件: " + wrapper.getCustomSqlSegment());
        long count = rescueRepository.selectCount(wrapper);
        System.out.println("已完成救助数量: " + count);
        return count;
    }

    /**
     * 获取待审核领养申请数量
     */
    public long getPendingAdoptions(Long rescuerId) {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING");

        // 如果指定了救助者ID，需要关联查询猫咪表
        if (rescuerId != null) {
            // 这里需要根据业务逻辑实现，暂时返回所有待审核领养申请
            return adoptionRepository.selectCount(wrapper);
        }

        return adoptionRepository.selectCount(wrapper);
    }

    /**
     * 获取救助猫咪总数（已完成救助对应的猫咪）
     */
    public long getTotalCats(Long rescuerId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("status", "已完成").or().eq("status", "COMPLETED").or().eq("status", "RESCUED"));

        // 支持日期范围筛选
        if (startTime != null && endTime != null) {
            wrapper.ge("create_time", startTime);
            wrapper.le("create_time", endTime);
        }

        // 统计已完成救助的数量（每个救助对应一只猫咪）
        return rescueRepository.selectCount(wrapper);
    }

    /**
     * 按紧急程度获取救助数量
     */
    public long getRescuesByUrgencyLevel(Long rescuerId, String urgencyLevel, java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();

        // 转换紧急程度值为数据库存储值
        String dbUrgencyLevel = convertUrgencyLevelToDbValue(urgencyLevel);
        System.out.println("紧急程度统计 - 输入值: " + urgencyLevel + ", 转换后值: " + dbUrgencyLevel);

        if (dbUrgencyLevel != null) {
            wrapper.eq("urgency_level", dbUrgencyLevel);
        }

        // 支持日期范围筛选
        if (startTime != null && endTime != null) {
            wrapper.ge("create_time", startTime);
            wrapper.le("create_time", endTime);
        }

        // 调试：打印查询条件
        System.out.println("紧急程度统计查询条件: " + wrapper.getCustomSqlSegment());

        long count = rescueRepository.selectCount(wrapper);
        System.out.println("紧急程度统计结果 - " + urgencyLevel + ": " + count);

        return count;
    }

    /**
     * 获取救助统计详细信息（包含日期分析）
     */
    public Map<String, Object> getRescueStatisticsWithDetails(Long rescuerId, java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();

        // 调试：查询数据库中的所有状态值
        QueryWrapper<Rescue> statusWrapper = new QueryWrapper<>();
        statusWrapper.select("DISTINCT status");
        List<Rescue> statusList = rescueRepository.selectList(statusWrapper);
        System.out.println("数据库中存在的状态值:");
        for (Rescue rescue : statusList) {
            System.out.println("状态: " + rescue.getStatus());
        }

        // 基础统计（支持日期范围筛选）
        statistics.put("totalRescues", getTotalRescues(rescuerId, startTime, endTime));
        statistics.put("pendingRescues", getPendingRescues(rescuerId, startTime, endTime));
        statistics.put("inProgressRescues", getInProgressRescues(rescuerId, startTime, endTime));
        statistics.put("completedRescues", getCompletedRescues(rescuerId, startTime, endTime));
        statistics.put("pendingAdoptions", getPendingAdoptions(rescuerId));
        statistics.put("totalCats", getTotalCats(rescuerId, startTime, endTime));

        // 紧急程度统计
        statistics.put("criticalRescues", getRescuesByUrgencyLevel(rescuerId, "紧急", startTime, endTime));
        statistics.put("highRescues", getRescuesByUrgencyLevel(rescuerId, "高", startTime, endTime));
        statistics.put("mediumRescues", getRescuesByUrgencyLevel(rescuerId, "中", startTime, endTime));
        statistics.put("lowRescues", getRescuesByUrgencyLevel(rescuerId, "低", startTime, endTime));

        // 日期分析 - 根据日期范围统计
        QueryWrapper<Rescue> dateWrapper = new QueryWrapper<>();

        // 如果指定了日期范围，按指定范围统计；否则统计最近30天
        java.time.LocalDateTime actualStartTime;
        java.time.LocalDateTime actualEndTime;

        if (startTime != null && endTime != null) {
            actualStartTime = startTime;
            actualEndTime = endTime;
            dateWrapper.ge("create_time", actualStartTime);
            dateWrapper.le("create_time", actualEndTime);
        } else {
            actualEndTime = java.time.LocalDateTime.now();
            actualStartTime = actualEndTime.minusDays(30);
            dateWrapper.ge("create_time", actualStartTime);
            dateWrapper.le("create_time", actualEndTime);
        }

        // 救助趋势按总数据分析，不按救援者ID筛选

        List<Rescue> recentRescues = rescueRepository.selectList(dateWrapper);

        // 按日期分组统计 - 使用create_time而不是complete_time
        Map<String, Long> dailyStats = new HashMap<>();

        // 初始化日期范围内的所有日期为0
        for (java.time.LocalDate date = actualStartTime.toLocalDate(); !date
                .isAfter(actualEndTime.toLocalDate()); date = date.plusDays(1)) {
            dailyStats.put(date.toString(), 0L);
        }

        // 统计每天的救助数量
        for (Rescue rescue : recentRescues) {
            if (rescue.getCreateTime() != null) {
                String dateKey = rescue.getCreateTime().toLocalDate().toString();
                dailyStats.put(dateKey, dailyStats.getOrDefault(dateKey, 0L) + 1);
            }
        }

        statistics.put("recentDailyStats", dailyStats);

        // 救助耗时分析
        statistics.put("rescueDurationStats", getRescueDurationStats(rescuerId, startTime, endTime));

        // 救助人员工作量统计
        statistics.put("rescuerWorkloadStats", getRescuerWorkloadStats(rescuerId, startTime, endTime));

        return statistics;
    }

    /**
     * 根据救助者ID获取救助任务（分页）
     */
    public IPage<Rescue> getRescuesByRescuerWithPaging(int page, int size, Long rescuerId) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");

        IPage<Rescue> result = rescueRepository.selectPage(new Page<>(page, size), wrapper);
        fillUserInfo(result.getRecords());
        return result;
    }

    /**
     * 获取救助耗时分析统计
     */
    private Map<String, Long> getRescueDurationStats(Long rescuerId, java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();

        // 日期范围筛选
        if (startTime != null && endTime != null) {
            wrapper.ge("create_time", startTime);
            wrapper.le("create_time", endTime);
        }

        // 只统计已完成的救助
        wrapper.and(w -> w.eq("status", "已完成").or().eq("status", "COMPLETED"));
        wrapper.isNotNull("complete_time");

        List<Rescue> completedRescues = rescueRepository.selectList(wrapper);

        Map<String, Long> durationStats = new HashMap<>();
        durationStats.put("0-1小时", 0L);
        durationStats.put("1-4小时", 0L);
        durationStats.put("4-12小时", 0L);
        durationStats.put("12-24小时", 0L);
        durationStats.put("1-3天", 0L);
        durationStats.put("3天以上", 0L);

        for (Rescue rescue : completedRescues) {
            if (rescue.getCompleteTime() != null) {
                // 优先使用acceptTime，如果为空则使用createTime
                java.time.LocalDateTime startTimeForCalc = rescue.getAcceptTime() != null ? rescue.getAcceptTime()
                        : rescue.getCreateTime();

                if (startTimeForCalc != null) {
                    long hours = java.time.Duration.between(startTimeForCalc, rescue.getCompleteTime()).toHours();

                    if (hours < 1) {
                        durationStats.put("0-1小时", durationStats.get("0-1小时") + 1);
                    } else if (hours < 4) {
                        durationStats.put("1-4小时", durationStats.get("1-4小时") + 1);
                    } else if (hours < 12) {
                        durationStats.put("4-12小时", durationStats.get("4-12小时") + 1);
                    } else if (hours < 24) {
                        durationStats.put("12-24小时", durationStats.get("12-24小时") + 1);
                    } else if (hours < 72) {
                        durationStats.put("1-3天", durationStats.get("1-3天") + 1);
                    } else {
                        durationStats.put("3天以上", durationStats.get("3天以上") + 1);
                    }
                }
            }
        }

        return durationStats;
    }

    /**
     * 获取志愿者工作量统计（同时包括救助者和志愿者）
     */
    private Map<String, Long> getRescuerWorkloadStats(Long rescuerId, java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime) {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();

        // 日期范围筛选
        if (startTime != null && endTime != null) {
            wrapper.ge("create_time", startTime);
            wrapper.le("create_time", endTime);
        }

        List<Rescue> rescues = rescueRepository.selectList(wrapper);

        Map<String, Long> workloadStats = new HashMap<>();

        for (Rescue rescue : rescues) {
            // 处理志愿者姓名（优先使用志愿者姓名）
            if (rescue.getVolunteerName() != null && !rescue.getVolunteerName().trim().isEmpty()) {
                String volunteerName = rescue.getVolunteerName().trim();
                workloadStats.put(volunteerName, workloadStats.getOrDefault(volunteerName, 0L) + 1);
            }
            // 如果没有志愿者姓名，使用救助者用户名
            else if (rescue.getRescuerId() != null) {
                User rescuer = userRepository.selectById(rescue.getRescuerId());
                if (rescuer != null) {
                    String rescuerName = rescuer.getUsername();
                    workloadStats.put(rescuerName, workloadStats.getOrDefault(rescuerName, 0L) + 1);
                }
            }
        }

        // 如果没有数据，返回默认数据
        if (workloadStats.isEmpty()) {
            return workloadStats;
        }

        // 只保留前5名，其他合并为"其他"
        List<Map.Entry<String, Long>> sortedList = new java.util.ArrayList<>(workloadStats.entrySet());
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        Map<String, Long> resultStats = new HashMap<>();
        long otherCount = 0;

        for (int i = 0; i < sortedList.size(); i++) {
            Map.Entry<String, Long> entry = sortedList.get(i);
            if (i < 5) {
                resultStats.put(entry.getKey(), entry.getValue());
            } else {
                otherCount += entry.getValue();
            }
        }

        if (otherCount > 0) {
            resultStats.put("其他", otherCount);
        }

        return resultStats;
    }
}