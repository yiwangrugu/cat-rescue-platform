package com.catrescue.catrescueplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catrescue.catrescueplatform.entity.Volunteer;
import com.catrescue.catrescueplatform.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 志愿者控制器
 * 
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    /**
     * 获取所有志愿者列表（公开接口，不需要管理员权限）
     */
    @GetMapping("/api/volunteers")
    public ResponseEntity<List<Volunteer>> getAllVolunteersPublic() {
        List<Volunteer> volunteers = volunteerService.getAllVolunteers();
        return ResponseEntity.ok(volunteers);
    }

    /**
     * 获取所有志愿者列表（管理端）
     */
    @GetMapping("/api/admin/volunteers")
    public ResponseEntity<List<Volunteer>> getAllVolunteers() {
        List<Volunteer> volunteers = volunteerService.getAllVolunteers();
        return ResponseEntity.ok(volunteers);
    }

    /**
     * 分页获取志愿者列表（公开接口，不需要管理员权限）
     */
    @GetMapping("/api/volunteers/page")
    public ResponseEntity<Page<Volunteer>> getVolunteersPublic(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone) {
        Page<Volunteer> volunteers = volunteerService.getVolunteers(page, size, name, phone);
        return ResponseEntity.ok(volunteers);
    }

    /**
     * 分页获取志愿者列表（管理端）
     */
    @GetMapping("/api/admin/volunteers/page")
    public ResponseEntity<Page<Volunteer>> getVolunteers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone) {
        Page<Volunteer> volunteers = volunteerService.getVolunteers(page, size, name, phone);
        return ResponseEntity.ok(volunteers);
    }

    /**
     * 根据ID获取志愿者（公开接口，不需要管理员权限）
     */
    @GetMapping("/api/volunteers/{id}")
    public ResponseEntity<Volunteer> getVolunteerPublic(@PathVariable Long id) {
        Volunteer volunteer = volunteerService.getVolunteerById(id);
        return ResponseEntity.ok(volunteer);
    }

    /**
     * 根据ID获取志愿者（管理端）
     */
    @GetMapping("/api/admin/volunteers/{id}")
    public ResponseEntity<Volunteer> getVolunteer(@PathVariable Long id) {
        Volunteer volunteer = volunteerService.getVolunteerById(id);
        return ResponseEntity.ok(volunteer);
    }

    /**
     * 创建志愿者（管理端）
     */
    @PostMapping("/api/admin/volunteers")
    public ResponseEntity<Volunteer> createVolunteer(@RequestBody Volunteer volunteer) {
        Volunteer createdVolunteer = volunteerService.createVolunteer(volunteer);
        return ResponseEntity.ok(createdVolunteer);
    }

    /**
     * 更新志愿者（管理端）
     */
    @PutMapping("/api/admin/volunteers/{id}")
    public ResponseEntity<Volunteer> updateVolunteer(@PathVariable Long id, @RequestBody Volunteer volunteer) {
        Volunteer updatedVolunteer = volunteerService.updateVolunteer(id, volunteer);
        return ResponseEntity.ok(updatedVolunteer);
    }

    /**
     * 删除志愿者（管理端）
     */
    @DeleteMapping("/api/admin/volunteers/{id}")
    public ResponseEntity<Void> deleteVolunteer(@PathVariable Long id) {
        volunteerService.deleteVolunteer(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 切换志愿者状态（管理端）
     */
    @PutMapping("/api/admin/volunteers/{id}/toggle-status")
    public ResponseEntity<Volunteer> toggleVolunteerStatus(@PathVariable Long id) {
        Volunteer volunteer = volunteerService.toggleVolunteerStatus(id);
        return ResponseEntity.ok(volunteer);
    }
}