package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catrescue.catrescueplatform.config.RescueWebSocketHandler;
import com.catrescue.catrescueplatform.entity.Adoption;
import com.catrescue.catrescueplatform.entity.Applicant;
import com.catrescue.catrescueplatform.entity.Cat;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.repository.AdoptionRepository;
import com.catrescue.catrescueplatform.repository.ApplicantRepository;
import com.catrescue.catrescueplatform.repository.CatRepository;
import com.catrescue.catrescueplatform.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdoptionService {

    private final AdoptionRepository adoptionRepository;
    private final CatRepository catRepository;
    private final UserRepository userRepository;
    private final ApplicantRepository applicantRepository;

    public IPage<Adoption> getAdoptions(int page, int size, String status, Long userId) {
        System.out.println("AdoptionService.getAdoptions: userId = " + userId + ", status = " + status);
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        // 只有当userId不为null时才添加过滤条件
        // 这样救援端就可以看到所有领养申请
        if (userId != null) {
            System.out.println("Adding user_id filter: " + userId);
            wrapper.eq("user_id", userId);
        }
        wrapper.orderByDesc("create_time");

        IPage<Adoption> result = adoptionRepository.selectPage(new Page<>(page, size), wrapper);
        System.out.println("Query result: " + result.getTotal() + " records");
        fillAdoptionDetails(result.getRecords());
        return result;
    }

    public Adoption getAdoptionById(Long id) {
        Adoption adoption = adoptionRepository.selectById(id);
        if (adoption != null) {
            fillAdoptionDetails(java.util.Collections.singletonList(adoption));
        }
        return adoption;
    }

    public Adoption createAdoption(Adoption adoption) {
        // 设置默认申请状态
        if (adoption.getStatus() == null) {
            adoption.setStatus("PENDING");
        }

        // 如果提供了applicant_id，直接使用；否则根据user_id和身份证信息查找或创建申请人记录
        if (adoption.getApplicantId() == null && adoption.getUserId() != null) {
            // 检查是否提供了申请人基本信息（姓名、身份证）
            boolean hasApplicantInfo = adoption.getRealName() != null && adoption.getIdCard() != null;

            if (hasApplicantInfo) {
                // 根据user_id和身份证查找是否已存在相同的申请人记录
                QueryWrapper<Applicant> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id", adoption.getUserId())
                        .eq("id_card", adoption.getIdCard());

                Applicant existingApplicant = applicantRepository.selectOne(wrapper);

                if (existingApplicant != null) {
                    // 使用已存在的申请人记录
                    adoption.setApplicantId(existingApplicant.getId());
                } else {
                    // 创建新的申请人记录
                    Applicant newApplicant = new Applicant();
                    newApplicant.setUserId(adoption.getUserId());
                    newApplicant.setRealName(adoption.getRealName());
                    newApplicant.setIdCard(adoption.getIdCard());
                    newApplicant.setPhone(adoption.getPhone());
                    newApplicant.setAddress(adoption.getAddress());
                    newApplicant.setApplicationReason(adoption.getApplicationReason());
                    newApplicant.setLivingCondition(adoption.getLivingCondition());
                    newApplicant.setExperience(adoption.getExperience());
                    newApplicant.setFamilyMembers(adoption.getFamilyMembers());
                    newApplicant.setFinancialStatus(adoption.getFinancialStatus());
                    newApplicant.setWorkSchedule(adoption.getWorkSchedule());
                    newApplicant.setAdditionalInfo(adoption.getAdditionalInfo());

                    applicantRepository.insert(newApplicant);
                    adoption.setApplicantId(newApplicant.getId());
                }
            } else {
                // 如果没有提供申请人基本信息，查找该用户是否有默认的申请人记录
                // 由于一个用户可能有多个申请人记录，我们取最新的一个
                QueryWrapper<Applicant> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id", adoption.getUserId())
                        .orderByDesc("created_at")
                        .last("LIMIT 1");

                Applicant defaultApplicant = applicantRepository.selectOne(wrapper);
                if (defaultApplicant != null) {
                    adoption.setApplicantId(defaultApplicant.getId());
                }
            }
        }

        adoptionRepository.insert(adoption);
        fillAdoptionDetails(java.util.Collections.singletonList(adoption));

        // 发送WebSocket通知 - 领养申请创建
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("action", "CREATE");
        notificationData.put("adoptionId", adoption.getId());
        notificationData.put("catId", adoption.getCatId());
        notificationData.put("userId", adoption.getUserId());
        notificationData.put("status", adoption.getStatus());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RescueWebSocketHandler.notifyAdoptionApplicationUpdate(objectMapper.writeValueAsString(notificationData));
        } catch (JsonProcessingException e) {
            System.err.println("JSON序列化失败: " + e.getMessage());
        }

        return adoption;
    }

    public Adoption updateAdoption(Adoption adoption) {
        // 更新adoptions表，当用户修改申请信息时更新create_time字段
        adoption.setCreateTime(LocalDateTime.now());
        adoptionRepository.updateById(adoption);

        // 如果提供了applicant_id，同时更新applicants表中的申请人信息
        if (adoption.getApplicantId() != null) {
            // 获取申请人信息
            Applicant applicant = applicantRepository.selectById(adoption.getApplicantId());
            if (applicant != null) {
                // 更新申请人基本信息
                if (adoption.getRealName() != null) {
                    applicant.setRealName(adoption.getRealName());
                }
                if (adoption.getIdCard() != null) {
                    applicant.setIdCard(adoption.getIdCard());
                }
                if (adoption.getPhone() != null) {
                    applicant.setPhone(adoption.getPhone());
                }
                if (adoption.getAddress() != null) {
                    applicant.setAddress(adoption.getAddress());
                }

                // 更新申请人详细信息
                applicant.setApplicationReason(adoption.getApplicationReason());
                applicant.setLivingCondition(adoption.getLivingCondition());
                applicant.setExperience(adoption.getExperience());
                applicant.setFamilyMembers(adoption.getFamilyMembers());
                applicant.setFinancialStatus(adoption.getFinancialStatus());
                applicant.setWorkSchedule(adoption.getWorkSchedule());
                applicant.setAdditionalInfo(adoption.getAdditionalInfo());

                // 保存更新后的申请人信息
                applicantRepository.updateById(applicant);
            }
        }

        fillAdoptionDetails(java.util.Collections.singletonList(adoption));

        // 发送WebSocket通知 - 领养申请更新
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("action", "UPDATE");
        notificationData.put("adoptionId", adoption.getId());
        notificationData.put("catId", adoption.getCatId());
        notificationData.put("userId", adoption.getUserId());
        notificationData.put("status", adoption.getStatus());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RescueWebSocketHandler.notifyAdoptionApplicationUpdate(objectMapper.writeValueAsString(notificationData));
        } catch (JsonProcessingException e) {
            System.err.println("JSON序列化失败: " + e.getMessage());
        }

        return adoption;
    }

    public void deleteAdoption(Long id) {
        adoptionRepository.deleteById(id);
    }

    public List<Adoption> getAdoptionsByCatId(Long catId) {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        wrapper.eq("cat_id", catId);
        wrapper.orderByDesc("create_time");
        List<Adoption> adoptions = adoptionRepository.selectList(wrapper);
        fillAdoptionDetails(adoptions);
        return adoptions;
    }

    public boolean hasPendingAdoption(Long catId, Long userId) {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        wrapper.eq("cat_id", catId);
        wrapper.eq("user_id", userId);
        wrapper.in("status", "PENDING", "UNDER_REVIEW");
        return adoptionRepository.selectCount(wrapper) > 0;
    }

    public IPage<Adoption> getMyApplications(int page, int size, String status) {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();

        // 获取当前用户ID
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("getMyApplications - Authentication: " + authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("getMyApplications - 用户未登录");
            throw new RuntimeException("用户未登录");
        }

        Long userId = null;
        Object principal = authentication.getPrincipal();
        System.out.println("getMyApplications - Principal type: " + principal.getClass().getName());
        System.out.println("getMyApplications - Principal: " + principal);

        if (principal instanceof User) {
            // 如果principal是User对象，直接获取用户ID
            userId = ((User) principal).getId();
            System.out.println("getMyApplications - 从User对象获取用户ID: " + userId);
        } else if (principal instanceof String) {
            // 如果principal是用户名，查询用户信息
            String username = (String) principal;
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("username", username)
                    .last("LIMIT 1");
            User user = userRepository.selectOne(userWrapper);
            if (user != null) {
                userId = user.getId();
                System.out.println("getMyApplications - 从用户名查询获取用户ID: " + userId);
            }
        }

        if (userId != null) {
            wrapper.eq("user_id", userId);
            System.out.println("getMyApplications - 添加用户ID过滤条件: user_id = " + userId);
        } else {
            System.out.println("getMyApplications - 无法获取用户ID，不添加过滤条件");
        }

        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
            System.out.println("getMyApplications - 添加状态过滤条件: status = " + status);
        }
        wrapper.orderByDesc("create_time");

        IPage<Adoption> result = adoptionRepository.selectPage(new Page<>(page, size), wrapper);
        System.out.println("getMyApplications - 查询结果: " + result.getTotal() + " 条记录");
        fillAdoptionDetails(result.getRecords());
        return result;
    }

    public IPage<Adoption> getPendingApplications(int page, int size) {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING");
        wrapper.orderByDesc("create_time");

        IPage<Adoption> result = adoptionRepository.selectPage(new Page<>(page, size), wrapper);
        fillAdoptionDetails(result.getRecords());
        return result;
    }

    public IPage<Adoption> getApprovedApplications(int page, int size) {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "APPROVED");
        wrapper.orderByDesc("create_time");

        IPage<Adoption> result = adoptionRepository.selectPage(new Page<>(page, size), wrapper);
        fillAdoptionDetails(result.getRecords());
        return result;
    }

    public IPage<Adoption> getRejectedApplications(int page, int size) {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "REJECTED");
        wrapper.orderByDesc("create_time");

        IPage<Adoption> result = adoptionRepository.selectPage(new Page<>(page, size), wrapper);
        fillAdoptionDetails(result.getRecords());
        return result;
    }

    public Adoption approveApplication(Long id) {
        Adoption adoption = adoptionRepository.selectById(id);
        if (adoption != null && "待审核".equals(adoption.getStatus())) {
            adoption.setStatus("已通过");
            adoption.setApproveTime(LocalDateTime.now());
            adoption.setUpdateTime(LocalDateTime.now());
            adoption.setProcessTime(LocalDateTime.now());
            adoptionRepository.updateById(adoption);

            // 更新对应猫咪的状态为已领养
            if (adoption.getCatId() != null) {
                Cat cat = catRepository.selectById(adoption.getCatId());
                if (cat != null) {
                    cat.setStatus("已领养");
                    catRepository.updateById(cat);
                }
            }

            // 更新用户的领养申请数
            if (adoption.getUserId() != null) {
                User user = userRepository.selectById(adoption.getUserId());
                if (user != null) {
                    user.setAdoptionCount((user.getAdoptionCount() != null ? user.getAdoptionCount() : 0) + 1);
                    userRepository.updateById(user);
                    System.out.println("用户领养申请数更新: " + user.getUsername() + " 申请数: " + user.getAdoptionCount());
                }
            }

            fillAdoptionDetails(java.util.Collections.singletonList(adoption));

            // 发送WebSocket通知 - 领养申请通过
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("action", "APPROVE");
            notificationData.put("adoptionId", adoption.getId());
            notificationData.put("catId", adoption.getCatId());
            notificationData.put("userId", adoption.getUserId());
            notificationData.put("status", adoption.getStatus());
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                RescueWebSocketHandler.notifyAdoptionReviewUpdate(objectMapper.writeValueAsString(notificationData));
            } catch (JsonProcessingException e) {
                System.err.println("JSON序列化失败: " + e.getMessage());
            }
        }
        return adoption;
    }

    public Adoption rejectApplication(Long id, String reviewComment) {
        Adoption adoption = adoptionRepository.selectById(id);
        if (adoption != null && "PENDING".equals(adoption.getStatus())) {
            adoption.setStatus("REJECTED");
            adoption.setReviewComment(reviewComment);
            adoption.setRejectTime(LocalDateTime.now());
            adoption.setUpdateTime(LocalDateTime.now());
            adoption.setProcessTime(LocalDateTime.now());
            adoptionRepository.updateById(adoption);
            fillAdoptionDetails(java.util.Collections.singletonList(adoption));

            // 发送WebSocket通知 - 领养申请拒绝
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("action", "REJECT");
            notificationData.put("adoptionId", adoption.getId());
            notificationData.put("catId", adoption.getCatId());
            notificationData.put("userId", adoption.getUserId());
            notificationData.put("status", adoption.getStatus());
            notificationData.put("reviewComment", reviewComment);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                RescueWebSocketHandler.notifyAdoptionReviewUpdate(objectMapper.writeValueAsString(notificationData));
            } catch (JsonProcessingException e) {
                System.err.println("JSON序列化失败: " + e.getMessage());
            }
        }
        return adoption;
    }

    private void fillAdoptionDetails(List<Adoption> adoptions) {
        for (Adoption adoption : adoptions) {
            // 处理 status 字段，确保不为 null
            if (adoption.getStatus() == null) {
                adoption.setStatus("PENDING");
            }

            // 确保时间字段不为null
            if (adoption.getCreateTime() == null) {
                adoption.setCreateTime(LocalDateTime.now());
            }
            if (adoption.getApproveTime() == null && "APPROVED".equals(adoption.getStatus())) {
                adoption.setApproveTime(LocalDateTime.now());
            }
            if (adoption.getRejectTime() == null && "REJECTED".equals(adoption.getStatus())) {
                adoption.setRejectTime(LocalDateTime.now());
            }

            if (adoption.getCatId() != null) {
                Cat cat = catRepository.selectById(adoption.getCatId());
                if (cat != null) {
                    adoption.setCatName(cat.getName());
                    adoption.setCatBreed(cat.getBreed());
                    adoption.setCatImage(cat.getImages());
                    adoption.setCatAge(cat.getAge());
                    adoption.setCatGender(cat.getGender());
                    adoption.setCatHealth(cat.getHealthStatus());
                    adoption.setCatLocation(cat.getLocation());
                    adoption.setCatPersonality(cat.getPersonality());
                    adoption.setCatWeight(cat.getWeight());
                    adoption.setCatDescription(cat.getDescription());
                    adoption.setCatMedical(cat.getMedicalInfo());
                    adoption.setCatStatus(cat.getStatus());
                }
            }

            if (adoption.getApplicantId() != null) {
                // 根据applicant_id获取申请人详细信息
                Applicant applicantInfo = applicantRepository.selectById(adoption.getApplicantId());
                if (applicantInfo != null) {
                    adoption.setApplicant(applicantInfo);

                    // 设置申请人详细信息字段（兼容前端代码）
                    adoption.setIdCard(applicantInfo.getIdCard());
                    adoption.setPhone(applicantInfo.getPhone());
                    adoption.setAddress(applicantInfo.getAddress());
                    adoption.setApplicationReason(applicantInfo.getApplicationReason());
                    adoption.setLivingCondition(applicantInfo.getLivingCondition());
                    adoption.setExperience(applicantInfo.getExperience());
                    adoption.setFamilyMembers(applicantInfo.getFamilyMembers());
                    adoption.setFinancialStatus(applicantInfo.getFinancialStatus());
                    adoption.setWorkSchedule(applicantInfo.getWorkSchedule());
                    adoption.setAdditionalInfo(applicantInfo.getAdditionalInfo());

                    // 同时设置用户基本信息（兼容现有代码）
                    User applicantUser = userRepository.selectById(applicantInfo.getUserId());
                    if (applicantUser != null) {
                        adoption.setApplicantName(applicantUser.getUsername());
                        adoption.setApplicantPhone(applicantUser.getPhone());
                        adoption.setApplicantAvatar(applicantUser.getAvatar());
                    }
                }
            }

            if (adoption.getReviewerId() != null) {
                User reviewer = userRepository.selectById(adoption.getReviewerId());
                if (reviewer != null) {
                    adoption.setReviewerName(reviewer.getUsername());
                }
            }
        }
    }

    /**
     * 取消领养申请
     * 直接删除申请记录，如果申请人没有其他申请记录，则删除申请人记录
     */
    public void cancelApplication(Long id) {
        Adoption adoption = adoptionRepository.selectById(id);
        if (adoption != null && "PENDING".equals(adoption.getStatus())) {
            Long applicantId = adoption.getApplicantId();

            // 删除申请记录
            adoptionRepository.deleteById(id);

            // 检查申请人是否还有其他申请记录
            if (applicantId != null) {
                QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
                wrapper.eq("applicant_id", applicantId);
                Long remainingApplications = adoptionRepository.selectCount(wrapper);

                // 如果没有其他申请记录，删除申请人记录
                if (remainingApplications != null && remainingApplications == 0) {
                    applicantRepository.deleteById(applicantId);
                }
            }

            // 发送WebSocket通知 - 领养申请取消
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("action", "DELETE");
            notificationData.put("adoptionId", adoption.getId());
            notificationData.put("catId", adoption.getCatId());
            notificationData.put("userId", adoption.getUserId());
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                RescueWebSocketHandler
                        .notifyAdoptionApplicationUpdate(objectMapper.writeValueAsString(notificationData));
            } catch (JsonProcessingException e) {
                System.err.println("JSON序列化失败: " + e.getMessage());
            }
        } else if (adoption != null && !"PENDING".equals(adoption.getStatus())) {
            throw new RuntimeException("只有待审核的申请才能取消");
        } else if (adoption == null) {
            throw new RuntimeException("申请记录不存在");
        }
    }

}
