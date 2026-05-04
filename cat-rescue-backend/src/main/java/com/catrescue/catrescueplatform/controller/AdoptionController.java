package com.catrescue.catrescueplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catrescue.catrescueplatform.config.BusinessLog;
import com.catrescue.catrescueplatform.entity.Adoption;
import com.catrescue.catrescueplatform.entity.Applicant;
import com.catrescue.catrescueplatform.service.AdoptionService;
import com.catrescue.catrescueplatform.service.ApplicantService;
import com.catrescue.catrescueplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/adoptions")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;
    private final ApplicantService applicantService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<IPage<Adoption>> getAdoptions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {

        IPage<Adoption> adoptions = adoptionService.getAdoptions(page, size, status, null);
        return ResponseEntity.ok(adoptions);
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {

        IPage<Adoption> adoptions = adoptionService.getMyApplications(page, size, status);
        Map<String, Object> response = new HashMap<>();
        response.put("records", adoptions.getRecords());
        response.put("total", adoptions.getTotal());
        response.put("page", page);
        response.put("size", size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<IPage<Adoption>> getPendingApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        IPage<Adoption> adoptions = adoptionService.getPendingApplications(page, size);
        return ResponseEntity.ok(adoptions);
    }

    @GetMapping("/cat/{catId}")
    public ResponseEntity<List<Adoption>> getAdoptionsByCat(@PathVariable Long catId) {
        List<Adoption> adoptions = adoptionService.getAdoptionsByCatId(catId);
        return ResponseEntity.ok(adoptions);
    }

    @GetMapping("/check-pending")
    public ResponseEntity<Boolean> checkPendingAdoption(
            @RequestParam Long catId,
            @RequestParam Long userId) {

        boolean hasPending = adoptionService.hasPendingAdoption(catId, userId);
        return ResponseEntity.ok(hasPending);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Adoption> getAdoption(@PathVariable Long id) {
        Adoption adoption = adoptionService.getAdoptionById(id);
        return ResponseEntity.ok(adoption);
    }

    @PostMapping
    @BusinessLog(action = "提交领养申请", module = "领养管理", description = "提交了领养申请（猫咪ID: {adoption.catId}）")
    public ResponseEntity<?> createAdoption(@RequestBody Adoption adoption, HttpServletRequest request) {
        try {
            // 获取当前用户ID
            Long userId = jwtUtil.getUserIdFromRequest(request);

            // 检查用户是否已有申请人信息
            Applicant existingApplicant = applicantService.getApplicantByUserId(userId);
            if (existingApplicant == null) {
                return ResponseEntity.badRequest().body("请先完善申请人信息");
            }

            // 设置用户ID
            adoption.setUserId(userId);

            Adoption createdAdoption = adoptionService.createAdoption(adoption);
            return ResponseEntity.ok(createdAdoption);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建领养申请失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @BusinessLog(action = "更新领养申请", module = "领养管理", description = "更新了领养申请（ID: {id}）")
    public ResponseEntity<Adoption> updateAdoption(@PathVariable Long id, @RequestBody Adoption adoption) {
        adoption.setId(id);
        Adoption updatedAdoption = adoptionService.updateAdoption(adoption);
        return ResponseEntity.ok(updatedAdoption);
    }

    @DeleteMapping("/{id}")
    @BusinessLog(action = "删除领养申请", module = "领养管理", description = "删除了领养申请（ID: {id}）")
    public ResponseEntity<?> deleteAdoption(@PathVariable Long id) {
        adoptionService.deleteAdoption(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    @BusinessLog(action = "通过领养申请", module = "领养管理", description = "通过了领养申请（ID: {id}）")
    public ResponseEntity<Adoption> approveApplication(@PathVariable Long id) {
        Adoption adoption = adoptionService.approveApplication(id);
        return ResponseEntity.ok(adoption);
    }

    @PostMapping("/{id}/reject")
    @BusinessLog(action = "拒绝领养申请", module = "领养管理", description = "拒绝了领养申请（ID: {id}）")
    public ResponseEntity<Adoption> rejectApplication(
            @PathVariable Long id,
            @RequestBody(required = false) String reviewComment) {

        Adoption adoption = adoptionService.rejectApplication(id, reviewComment);
        return ResponseEntity.ok(adoption);
    }

    @PostMapping("/{id}/cancel")
    @BusinessLog(action = "取消领养申请", module = "领养管理", description = "取消了领养申请（ID: {id}）")
    public ResponseEntity<?> cancelApplication(@PathVariable Long id) {
        adoptionService.cancelApplication(id);
        return ResponseEntity.ok().build();
    }

}
