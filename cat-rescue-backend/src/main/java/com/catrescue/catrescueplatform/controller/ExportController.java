package com.catrescue.catrescueplatform.controller;

import com.catrescue.catrescueplatform.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
public class ExportController {

        private final ExportService exportService;

        /**
         * 导出用户表数据
         */
        @GetMapping("/users")
        public ResponseEntity<byte[]> exportUsers(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
                        throws IOException {
                byte[] excelData = exportService.exportUsers(startDate, endDate);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=users_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出猫咪表数据
         */
        @GetMapping("/cats")
        public ResponseEntity<byte[]> exportCats(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
                        throws IOException {
                byte[] excelData = exportService.exportCats(startDate, endDate);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=cats_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出救助表数据
         */
        @GetMapping("/rescues")
        public ResponseEntity<byte[]> exportRescues(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
                        throws IOException {
                byte[] excelData = exportService.exportRescues(startDate, endDate);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=rescues_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出领养表数据
         */
        @GetMapping("/adoptions")
        public ResponseEntity<byte[]> exportAdoptions(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
                        throws IOException {
                byte[] excelData = exportService.exportAdoptions(startDate, endDate);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=adoptions_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出帖子表数据
         */
        @GetMapping("/posts")
        public ResponseEntity<byte[]> exportPosts(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
                        throws IOException {
                byte[] excelData = exportService.exportPosts(startDate, endDate);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=posts_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出志愿者表数据
         */
        @GetMapping("/volunteers")
        public ResponseEntity<byte[]> exportVolunteers(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
                        throws IOException {
                byte[] excelData = exportService.exportVolunteers(startDate, endDate);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=volunteers_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出志愿者排班表
         */
        @GetMapping("/volunteer-schedules")
        public ResponseEntity<byte[]> exportVolunteerSchedules() throws IOException {
                byte[] excelData = exportService.exportVolunteerSchedules();

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=volunteer_schedules_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出评论表数据
         */
        @GetMapping("/comments")
        public ResponseEntity<byte[]> exportComments(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
                        throws IOException {
                byte[] excelData = exportService.exportComments(startDate, endDate);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=comments_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出统计报表
         */
        @GetMapping("/statistics")
        public ResponseEntity<byte[]> exportStatisticsReport(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
                        throws IOException {

                byte[] excelData = exportService.exportStatisticsReport(startDate, endDate);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=statistics_" + startDate + "_to_" + endDate
                                                                + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出系统日志数据
         */
        @GetMapping("/system-logs")
        public ResponseEntity<byte[]> exportSystemLogs(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) String module,
                        @RequestParam(required = false) String action)
                        throws IOException {
                byte[] excelData = exportService.exportSystemLogs(startDate, endDate, module, action);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=system_logs_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }

        /**
         * 导出所有数据（打包下载）
         */
        @GetMapping("/all")
        public ResponseEntity<byte[]> exportAllData() throws IOException {
                // 这里可以创建一个包含所有工作表的Excel文件
                byte[] excelData = exportService.exportUsers(null, null);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=cat_rescue_data_" + LocalDate.now() + ".xlsx")
                                .body(excelData);
        }
}