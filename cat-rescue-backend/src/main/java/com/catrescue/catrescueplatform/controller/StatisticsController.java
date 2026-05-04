package com.catrescue.catrescueplatform.controller;

import com.catrescue.catrescueplatform.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取实时统计数据
     */
    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealTimeStatistics() {
        Map<String, Object> statistics = statisticsService.getRealTimeStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取日期区间统计数据
     */
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getDateRangeStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> statistics = statisticsService.getDateRangeStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/charts")
    public ResponseEntity<Map<String, Object>> getChartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> charts = statisticsService.getChartData(startDate, endDate);
        return ResponseEntity.ok(charts);
    }

    /**
     * 创建每日统计快照（管理员手动触发）
     */
    @PostMapping("/snapshot")
    public ResponseEntity<String> createDailySnapshot() {
        statisticsService.createDailySnapshot();
        return ResponseEntity.ok("统计快照创建成功");
    }

    /**
     * 获取系统概览数据（包含实时统计和趋势数据）
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        Map<String, Object> overview = statisticsService.getRealTimeStatistics();
        
        // 添加趋势数据（最近7天）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        Map<String, Object> trendData = statisticsService.getDateRangeStatistics(startDate, endDate);
        overview.put("trends", trendData);
        
        // 添加图表数据
        Map<String, Object> chartData = statisticsService.getChartData(startDate, endDate);
        overview.put("charts", chartData);
        
        return ResponseEntity.ok(overview);
    }
}