package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catrescue.catrescueplatform.entity.Applicant;
import com.catrescue.catrescueplatform.repository.ApplicantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicantService {

    @Autowired
    private ApplicantRepository applicantRepository;

    /**
     * 创建或更新申请人信息
     */
    public Applicant saveOrUpdateApplicant(Applicant applicant) {
        // 检查是否已存在相同用户ID和身份证号的申请人信息
        QueryWrapper<Applicant> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", applicant.getUserId())
                .eq("id_card", applicant.getIdCard());

        Applicant existingApplicant = applicantRepository.selectOne(wrapper);

        if (existingApplicant == null) {
            // 新增 - 不同身份证号创建新的申请人记录
            applicant.setCreatedAt(LocalDateTime.now());
            applicant.setUpdatedAt(LocalDateTime.now());
            applicantRepository.insert(applicant);
        } else {
            // 更新 - 相同身份证号更新现有记录
            applicant.setId(existingApplicant.getId());
            applicant.setUpdatedAt(LocalDateTime.now());
            applicantRepository.updateById(applicant);
        }
        return applicant;
    }

    /**
     * 根据用户ID获取申请人信息
     */
    public Applicant getApplicantByUserId(Long userId) {
        return applicantRepository.findByUserId(userId);
    }

    /**
     * 检查用户是否已有申请人信息
     */
    public boolean hasApplicantInfo(Long userId) {
        return applicantRepository.existsByUserId(userId) > 0;
    }

    /**
     * 根据ID获取申请人信息
     */
    public Applicant getApplicantById(Long id) {
        return applicantRepository.selectById(id);
    }

    /**
     * 删除申请人信息
     */
    public boolean deleteApplicant(Long id) {
        return applicantRepository.deleteById(id) > 0;
    }

    /**
     * 获取所有申请人信息
     */
    public List<Applicant> getAllApplicants() {
        return applicantRepository.selectList(new QueryWrapper<Applicant>().orderByDesc("created_at"));
    }
}