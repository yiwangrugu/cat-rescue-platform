package com.catrescue.catrescueplatform.controller;

import com.catrescue.catrescueplatform.entity.Applicant;
import com.catrescue.catrescueplatform.service.ApplicantService;
import com.catrescue.catrescueplatform.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/applicants")
public class ApplicantController {

    @Autowired
    private ApplicantService applicantService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的申请人信息
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyApplicantInfo(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            Applicant applicant = applicantService.getApplicantByUserId(userId);

            if (applicant == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("hasInfo", false);
                response.put("message", "暂无申请人信息");
                return ResponseEntity.ok(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("hasInfo", true);
            response.put("applicant", applicant);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取申请人信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建或更新申请人信息
     */
    @PostMapping("/save")
    public ResponseEntity<?> saveApplicantInfo(@RequestBody Applicant applicant, HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            applicant.setUserId(userId);

            Applicant savedApplicant = applicantService.saveOrUpdateApplicant(applicant);
            return ResponseEntity.ok(savedApplicant);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("保存申请人信息失败：" + e.getMessage());
        }
    }

    /**
     * 检查当前用户是否已有申请人信息
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkApplicantInfo(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            boolean hasInfo = applicantService.hasApplicantInfo(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("hasInfo", hasInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("检查申请人信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取申请人信息（管理员使用）
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicantById(@PathVariable Long id) {
        try {
            Applicant applicant = applicantService.getApplicantById(id);
            if (applicant == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(applicant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取申请人信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有申请人信息（管理员使用）
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllApplicants() {
        try {
            return ResponseEntity.ok(applicantService.getAllApplicants());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取申请人列表失败：" + e.getMessage());
        }
    }
}