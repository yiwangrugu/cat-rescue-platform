package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catrescue.catrescueplatform.entity.Cat;
import com.catrescue.catrescueplatform.repository.CatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatService {

    private final CatRepository catRepository;

    public IPage<Cat> getCats(int page, int size, String status, String breed, String auditStatus) {
        QueryWrapper<Cat> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        if (breed != null && !breed.isEmpty()) {
            wrapper.like("breed", breed);
        }
        if (auditStatus != null && !auditStatus.isEmpty()) {
            wrapper.eq("audit_status", auditStatus);
        }
        wrapper.orderByDesc("create_time");

        return catRepository.selectPage(new Page<>(page, size), wrapper);
    }

    public Cat getCatById(Long id) {
        return catRepository.selectById(id);
    }

    public Cat createCat(Cat cat) {
        // 如果猫咪没有设置审核状态，默认设置为"APPROVED"
        if (cat.getAuditStatus() == null || cat.getAuditStatus().isEmpty()) {
            cat.setAuditStatus("APPROVED");
        }

        // 如果审核状态为APPROVED，设置审核时间为当前时间
        if ("APPROVED".equals(cat.getAuditStatus())) {
            cat.setAuditTime(LocalDateTime.now());
        }

        catRepository.insert(cat);
        return cat;
    }

    public Cat updateCat(Cat cat) {
        catRepository.updateById(cat);
        return cat;
    }

    public void deleteCat(Long id) {
        catRepository.deleteById(id);
    }

    public List<Cat> getCatsByRescuer(Long rescuerId) {
        QueryWrapper<Cat> wrapper = new QueryWrapper<>();
        wrapper.eq("rescuer_id", rescuerId);
        wrapper.orderByDesc("create_time");
        return catRepository.selectList(wrapper);
    }

    public List<String> getBreeds() {
        QueryWrapper<Cat> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT breed");
        wrapper.isNotNull("breed");
        wrapper.ne("breed", "");
        List<Cat> cats = catRepository.selectList(wrapper);
        return cats.stream().map(Cat::getBreed).toList();
    }

    /**
     * 根据审核状态获取猫咪列表
     */
    public IPage<Cat> getCatsByAuditStatus(String auditStatus, int page, int size) {
        QueryWrapper<Cat> wrapper = new QueryWrapper<>();
        wrapper.eq("audit_status", auditStatus);
        wrapper.orderByDesc("create_time");
        return catRepository.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 审核猫咪信息
     */
    public Cat auditCat(Long id, String status, String remark) {
        Cat cat = catRepository.selectById(id);
        if (cat == null) {
            throw new RuntimeException("猫咪信息不存在");
        }

        // 更新审核信息
        cat.setAuditStatus(status);
        cat.setAuditRemark(remark);
        cat.setAuditTime(java.time.LocalDateTime.now());

        // 如果是审核通过，处理图片路径
        if ("APPROVED".equals(status)) {
            // 直接将images字段复制到official_images
            if (cat.getOfficialImages() == null && cat.getImages() != null) {
                cat.setOfficialImages(cat.getImages());
            }
        }

        catRepository.updateById(cat);
        return cat;
    }

    /**
     * 获取CatRepository实例，供其他服务调用
     */
    public CatRepository getCatRepository() {
        return catRepository;
    }
}