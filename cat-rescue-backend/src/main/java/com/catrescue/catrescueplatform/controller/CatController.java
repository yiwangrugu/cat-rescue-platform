package com.catrescue.catrescueplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.catrescue.catrescueplatform.config.BusinessLog;
import com.catrescue.catrescueplatform.entity.Cat;
import com.catrescue.catrescueplatform.service.CatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cats")
@RequiredArgsConstructor
public class CatController {

    private final CatService catService;

    @GetMapping
    public ResponseEntity<IPage<Cat>> getCats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String auditStatus) {

        IPage<Cat> cats = catService.getCats(page, size, status, breed, auditStatus);
        return ResponseEntity.ok(cats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cat> getCat(@PathVariable Long id) {
        Cat cat = catService.getCatById(id);
        return ResponseEntity.ok(cat);
    }

    @PostMapping
    @BusinessLog(action = "创建", module = "猫咪管理", description = "添加了猫咪：{cat.name}")
    public ResponseEntity<Cat> createCat(@RequestBody Cat cat) {
        Cat createdCat = catService.createCat(cat);
        return ResponseEntity.ok(createdCat);
    }

    @PutMapping("/{id}")
    @BusinessLog(action = "更新", module = "猫咪管理", description = "更新了猫咪信息（ID: {id}）")
    public ResponseEntity<Cat> updateCat(@PathVariable Long id, @RequestBody Cat cat) {
        cat.setId(id);
        Cat updatedCat = catService.updateCat(cat);
        return ResponseEntity.ok(updatedCat);
    }

    @DeleteMapping("/{id}")
    @BusinessLog(action = "删除", module = "猫咪管理", description = "删除了猫咪（ID: {id}）")
    public ResponseEntity<?> deleteCat(@PathVariable Long id) {
        catService.deleteCat(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rescuer/{rescuerId}")
    public ResponseEntity<List<Cat>> getCatsByRescuer(@PathVariable Long rescuerId) {
        List<Cat> cats = catService.getCatsByRescuer(rescuerId);
        return ResponseEntity.ok(cats);
    }

    @GetMapping("/breeds")
    public ResponseEntity<List<String>> getBreeds() {
        List<String> breeds = catService.getBreeds();
        return ResponseEntity.ok(breeds);
    }

    /**
     * 获取待审核的猫咪列表（管理端使用）
     */
    @GetMapping("/pending")
    public ResponseEntity<IPage<Cat>> getPendingCats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Cat> cats = catService.getCatsByAuditStatus("PENDING", page, size);
        return ResponseEntity.ok(cats);
    }

    /**
     * 审核猫咪信息
     */
    @PostMapping("/{id}/audit")
    @BusinessLog(action = "审核", module = "猫咪管理", description = "审核了猫咪信息（ID: {id}）")
    public ResponseEntity<Cat> auditCat(
            @PathVariable Long id,
            @RequestParam String status, // APPROVED/REJECTED
            @RequestParam(required = false) String remark) {

        Cat auditedCat = catService.auditCat(id, status, remark);
        return ResponseEntity.ok(auditedCat);
    }

    /**
     * 救助端提交猫咪信息（自动设置为待审核状态）
     */
    @PostMapping("/rescuer/submit")
    @BusinessLog(action = "提交", module = "猫咪管理", description = "提交了猫咪审核：{cat.name}")
    public ResponseEntity<Cat> submitCat(@RequestBody Cat cat) {
        // 设置审核状态为待审核
        cat.setAuditStatus("PENDING");
        Cat submittedCat = catService.createCat(cat);
        return ResponseEntity.ok(submittedCat);
    }

    /**
     * 测试接口：获取猫咪详细信息（用于调试图片显示问题）
     */
    @GetMapping("/test/details")
    public ResponseEntity<?> getCatDetails() {
        // 获取所有审核通过的猫咪
        List<Cat> cats = catService.getCatsByAuditStatus("APPROVED", 1, 10).getRecords();

        // 打印猫咪数据详情
        for (Cat cat : cats) {
            System.out.println("=== 猫咪详情 ===");
            System.out.println("ID: " + cat.getId());
            System.out.println("名称: " + cat.getName());
            System.out.println("officialImages: " + cat.getOfficialImages());
            System.out.println("images: " + cat.getImages());
            System.out.println("================");
        }

        return ResponseEntity.ok(cats);
    }
}