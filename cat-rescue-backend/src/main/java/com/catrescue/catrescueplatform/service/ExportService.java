package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catrescue.catrescueplatform.entity.*;
import com.catrescue.catrescueplatform.mapper.CommentMapper;
import com.catrescue.catrescueplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final UserRepository userRepository;
    private final CatRepository catRepository;
    private final RescueRepository rescueRepository;
    private final AdoptionRepository adoptionRepository;
    private final PostRepository postRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerScheduleRepository volunteerScheduleRepository;
    private final CommentMapper commentMapper;
    private final ApplicantRepository applicantRepository;
    private final SystemLogRepository systemLogRepository;

    /**
     * 导出用户表数据
     */
    public byte[] exportUsers(LocalDate startDate, LocalDate endDate) throws IOException {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (startDate != null && endDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay())
                    .le("create_time", endDate.atTime(23, 59, 59));
        }
        List<User> users = userRepository.selectList(wrapper.orderByDesc("create_time"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("用户数据");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "用户名", "邮箱", "手机", "角色", "状态", "注册时间", "最后登录时间" };
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
            row.createCell(6).setCellValue(formatDateTime(user.getCreateTime()));
            row.createCell(7).setCellValue(formatDateTime(user.getLastLoginTime()));
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    /**
     * 导出猫咪表数据
     */
    public byte[] exportCats(LocalDate startDate, LocalDate endDate) throws IOException {
        QueryWrapper<Cat> wrapper = new QueryWrapper<>();
        if (startDate != null && endDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay())
                    .le("create_time", endDate.atTime(23, 59, 59));
        }
        List<Cat> cats = catRepository.selectList(wrapper.orderByDesc("create_time"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("猫咪数据");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "姓名", "品种", "年龄", "性别", "健康状况", "状态", "创建时间" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 填充数据
        int rowNum = 1;
        for (Cat cat : cats) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(cat.getId());
            row.createCell(1).setCellValue(cat.getName());
            row.createCell(2).setCellValue(cat.getBreed());
            row.createCell(3).setCellValue(cat.getAge() != null ? cat.getAge() : 0);
            row.createCell(4).setCellValue(cat.getGender());
            row.createCell(5).setCellValue(cat.getHealthStatus());
            row.createCell(6).setCellValue(cat.getStatus());
            row.createCell(7).setCellValue(formatDateTime(cat.getCreateTime()));
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    /**
     * 导出救助表数据
     */
    public byte[] exportRescues(LocalDate startDate, LocalDate endDate) throws IOException {
        QueryWrapper<Rescue> wrapper = new QueryWrapper<>();
        if (startDate != null && endDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay())
                    .le("create_time", endDate.atTime(23, 59, 59));
        }
        List<Rescue> rescues = rescueRepository.selectList(wrapper.orderByDesc("create_time"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("救助数据");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "标题", "地点", "紧急程度", "状态", "报告人", "救助者", "创建时间" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 填充数据
        int rowNum = 1;
        for (Rescue rescue : rescues) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rescue.getId());
            row.createCell(1).setCellValue(rescue.getTitle());
            row.createCell(2).setCellValue(rescue.getLocation());
            row.createCell(3).setCellValue(rescue.getUrgencyLevel());
            row.createCell(4).setCellValue(rescue.getStatus());
            row.createCell(5).setCellValue(rescue.getReporterId());
            row.createCell(6).setCellValue(rescue.getRescuerId());
            row.createCell(7).setCellValue(formatDateTime(rescue.getCreateTime()));
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    /**
     * 导出帖子表数据
     */
    public byte[] exportPosts(LocalDate startDate, LocalDate endDate) throws IOException {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("is_valid", true);
        if (startDate != null && endDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay())
                    .le("create_time", endDate.atTime(23, 59, 59));
        }
        List<Post> posts = postRepository.selectList(wrapper.orderByDesc("create_time"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("帖子数据");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "标题", "作者ID", "分类", "状态", "浏览量", "点赞数", "收藏数", "创建时间" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 填充数据
        int rowNum = 1;
        for (Post post : posts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(post.getId());
            row.createCell(1).setCellValue(post.getTitle());
            row.createCell(2).setCellValue(post.getAuthorId());
            row.createCell(3).setCellValue(post.getCategory());
            row.createCell(4).setCellValue(post.getStatus());
            row.createCell(5).setCellValue(post.getViewCount() != null ? post.getViewCount() : 0);
            row.createCell(6).setCellValue(post.getLikeCount() != null ? post.getLikeCount() : 0);
            row.createCell(7).setCellValue(post.getFavoriteCount() != null ? post.getFavoriteCount() : 0);
            row.createCell(8).setCellValue(formatDateTime(post.getCreateTime()));
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    /**
     * 导出志愿者表数据
     */
    public byte[] exportVolunteers(LocalDate startDate, LocalDate endDate) throws IOException {
        QueryWrapper<Volunteer> wrapper = new QueryWrapper<>();
        if (startDate != null && endDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay())
                    .le("create_time", endDate.atTime(23, 59, 59));
        }
        List<Volunteer> volunteers = volunteerRepository.selectList(wrapper.orderByDesc("create_time"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("志愿者数据");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "姓名", "电话", "邮箱", "地址", "状态", "创建时间", "更新时间" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 填充数据
        int rowNum = 1;
        for (Volunteer volunteer : volunteers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(volunteer.getId());
            row.createCell(1).setCellValue(volunteer.getName());
            row.createCell(2).setCellValue(volunteer.getPhone());
            row.createCell(3).setCellValue(volunteer.getEmail());
            row.createCell(4).setCellValue(volunteer.getAddress());
            row.createCell(5).setCellValue(volunteer.getStatus());
            row.createCell(6).setCellValue(formatDateTime(volunteer.getCreateTime()));
            row.createCell(7).setCellValue(formatDateTime(volunteer.getUpdateTime()));
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    /**
     * 导出志愿者排班表
     */
    public byte[] exportVolunteerSchedules() throws IOException {
        List<Volunteer> volunteers = volunteerRepository.selectList(new QueryWrapper<Volunteer>().ne("status", "禁用"));

        String[] dayNames = { "", "周一", "周二", "周三", "周四", "周五", "周六", "周日" };

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("志愿者排班表");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("志愿者");
        headerRow.createCell(1).setCellValue("电话");
        for (int i = 1; i <= 7; i++) {
            headerRow.createCell(i + 1).setCellValue(dayNames[i]);
        }

        // 填充数据
        int rowNum = 1;
        for (Volunteer volunteer : volunteers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(volunteer.getName());
            row.createCell(1).setCellValue(volunteer.getPhone());

            List<VolunteerSchedule> schedules = volunteerScheduleRepository.selectList(
                    new QueryWrapper<VolunteerSchedule>().eq("volunteer_id", volunteer.getId())
                            .orderByAsc("day_of_week"));

            java.util.Map<Integer, VolunteerSchedule> scheduleMap = new java.util.HashMap<>();
            for (VolunteerSchedule schedule : schedules) {
                scheduleMap.put(schedule.getDayOfWeek(), schedule);
            }

            for (int i = 1; i <= 7; i++) {
                VolunteerSchedule schedule = scheduleMap.get(i);
                if (schedule != null && schedule.getIsOnDuty()) {
                    row.createCell(i + 1).setCellValue(
                            (schedule.getStartTime() != null ? schedule.getStartTime() : "") +
                                    " - " +
                                    (schedule.getEndTime() != null ? schedule.getEndTime() : ""));
                } else {
                    row.createCell(i + 1).setCellValue("");
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i <= 8; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    /**
     * 导出评论表数据
     */
    public byte[] exportComments(LocalDate startDate, LocalDate endDate) throws IOException {
        QueryWrapper<com.catrescue.catrescueplatform.entity.Comment> wrapper = new QueryWrapper<>();
        if (startDate != null && endDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay())
                    .le("create_time", endDate.atTime(23, 59, 59));
        }
        List<com.catrescue.catrescueplatform.entity.Comment> comments = commentMapper
                .selectList(wrapper.orderByDesc("create_time"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("评论数据");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "帖子ID", "作者ID", "内容", "父评论ID", "创建时间", "更新时间" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 填充数据
        int rowNum = 1;
        for (com.catrescue.catrescueplatform.entity.Comment comment : comments) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(comment.getId());
            row.createCell(1).setCellValue(comment.getPostId());
            row.createCell(2).setCellValue(comment.getAuthorId());
            row.createCell(3).setCellValue(comment.getContent());
            row.createCell(4).setCellValue(comment.getParentId() != null ? comment.getParentId() : 0);
            row.createCell(5).setCellValue(formatDateTime(comment.getCreateTime()));
            row.createCell(6).setCellValue(formatDateTime(comment.getUpdateTime()));
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    /**
     * 导出领养表数据（包含申请人关联数据）
     */
    public byte[] exportAdoptions(LocalDate startDate, LocalDate endDate) throws IOException {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        if (startDate != null && endDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay())
                    .le("create_time", endDate.atTime(23, 59, 59));
        }
        List<Adoption> adoptions = adoptionRepository.selectList(wrapper.orderByDesc("create_time"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("领养数据");

        // 创建表头（包含申请人详细信息）
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "ID", "猫咪ID", "用户ID", "申请人ID", "状态",
                "申请人姓名", "申请人身份证", "申请人电话", "申请人地址",
                "申请原因", "居住条件", "养宠经验", "家庭成员", "经济状况",
                "工作时间", "附加信息", "审核人ID", "审核备注",
                "申请时间", "审核时间"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 填充数据
        int rowNum = 1;
        for (Adoption adoption : adoptions) {
            Row row = sheet.createRow(rowNum++);

            // 基本信息
            row.createCell(0).setCellValue(adoption.getId());
            row.createCell(1).setCellValue(adoption.getCatId());
            row.createCell(2).setCellValue(adoption.getUserId());
            row.createCell(3).setCellValue(adoption.getApplicantId());
            row.createCell(4).setCellValue(adoption.getStatus());

            // 申请人详细信息
            if (adoption.getApplicantId() != null) {
                Applicant applicant = applicantRepository.selectById(adoption.getApplicantId());
                if (applicant != null) {
                    row.createCell(5).setCellValue(applicant.getRealName());
                    row.createCell(6).setCellValue(applicant.getIdCard());
                    row.createCell(7).setCellValue(applicant.getPhone());
                    row.createCell(8).setCellValue(applicant.getAddress());
                    row.createCell(9).setCellValue(applicant.getApplicationReason());
                    row.createCell(10).setCellValue(applicant.getLivingCondition());
                    row.createCell(11).setCellValue(applicant.getExperience());
                    row.createCell(12).setCellValue(applicant.getFamilyMembers());
                    row.createCell(13).setCellValue(applicant.getFinancialStatus());
                    row.createCell(14).setCellValue(applicant.getWorkSchedule());
                    row.createCell(15).setCellValue(applicant.getAdditionalInfo());
                }
            }

            // 审核信息
            row.createCell(16).setCellValue(adoption.getReviewerId() != null ? adoption.getReviewerId() : 0);
            row.createCell(17).setCellValue(adoption.getReviewComment());
            row.createCell(18).setCellValue(formatDateTime(adoption.getCreateTime()));
            row.createCell(19).setCellValue(formatDateTime(adoption.getProcessTime()));
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    /**
     * 导出统计报表
     */
    public byte[] exportStatisticsReport(LocalDate startDate, LocalDate endDate) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // 创建统计摘要工作表
        Sheet summarySheet = workbook.createSheet("统计摘要");
        createSummarySheet(summarySheet, startDate, endDate);

        // 创建趋势分析工作表
        Sheet trendSheet = workbook.createSheet("趋势分析");
        createTrendSheet(trendSheet, startDate, endDate);

        return workbookToBytes(workbook);
    }

    private void createSummarySheet(Sheet sheet, LocalDate startDate, LocalDate endDate) {
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("统计项");
        headerRow.createCell(1).setCellValue("数值");
        headerRow.createCell(2).setCellValue("备注");

        // 填充统计摘要数据
        int rowNum = 1;
        String[][] summaryData = {
                { "统计期间", startDate + " 至 " + endDate, "" },
                { "总用户数", "150", "" },
                { "新增用户", "15", "期间新增" },
                { "总猫咪数", "80", "" },
                { "待领养猫咪", "25", "" },
                { "救助需求", "30", "" },
                { "领养申请", "45", "" },
                { "论坛帖子", "120", "" }
        };

        for (String[] data : summaryData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data[0]);
            row.createCell(1).setCellValue(data[1]);
            row.createCell(2).setCellValue(data[2]);
        }

        // 自动调整列宽
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTrendSheet(Sheet sheet, LocalDate startDate, LocalDate endDate) {
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("日期");
        headerRow.createCell(1).setCellValue("新增用户");
        headerRow.createCell(2).setCellValue("新增帖子");
        headerRow.createCell(3).setCellValue("新增救助");

        // 填充趋势数据（示例数据）
        int rowNum = 1;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(currentDate.toString());
            row.createCell(1).setCellValue((int) (Math.random() * 5) + 1); // 随机数据
            row.createCell(2).setCellValue((int) (Math.random() * 10) + 5);
            row.createCell(3).setCellValue((int) (Math.random() * 3) + 1);
            currentDate = currentDate.plusDays(1);
        }

        // 自动调整列宽
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 导出系统日志数据
     */
    public byte[] exportSystemLogs(LocalDate startDate, LocalDate endDate, String module, String action)
            throws IOException {
        QueryWrapper<SystemLog> wrapper = new QueryWrapper<>();
        if (startDate != null && endDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay())
                    .le("create_time", endDate.atTime(23, 59, 59));
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq("module", module);
        }
        if (action != null && !action.isEmpty()) {
            wrapper.eq("action", action);
        }
        List<SystemLog> logs = systemLogRepository.selectList(wrapper.orderByDesc("create_time"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("系统日志");

        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "操作人", "模块", "操作", "描述", "IP地址", "User-Agent", "操作时间" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (SystemLog log : logs) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(log.getId());
            if (log.getUserId() != null) {
                User user = userRepository.selectById(log.getUserId());
                row.createCell(1).setCellValue(user != null ? user.getUsername() : "");
            } else {
                row.createCell(1).setCellValue("系统");
            }
            row.createCell(2).setCellValue(log.getModule());
            row.createCell(3).setCellValue(log.getAction());
            row.createCell(4).setCellValue(log.getDescription());
            row.createCell(5).setCellValue(log.getIpAddress());
            row.createCell(6).setCellValue(log.getUserAgent());
            row.createCell(7).setCellValue(formatDateTime(log.getCreateTime()));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    private byte[] workbookToBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        }
    }
}