package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catrescue.catrescueplatform.entity.Volunteer;
import com.catrescue.catrescueplatform.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    /**
     * 获取所有志愿者列表
     */
    public List<Volunteer> getAllVolunteers() {
        LambdaQueryWrapper<Volunteer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Volunteer::getStatus, "空闲");
        wrapper.orderByDesc(Volunteer::getCreateTime);
        return volunteerRepository.selectList(wrapper);
    }

    /**
     * 分页获取志愿者列表
     */
    public Page<Volunteer> getVolunteers(int page, int size, String name, String phone) {
        Page<Volunteer> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Volunteer> wrapper = new LambdaQueryWrapper<>();

        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Volunteer::getName, name.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            wrapper.like(Volunteer::getPhone, phone.trim());
        }

        wrapper.orderByDesc(Volunteer::getCreateTime);
        return volunteerRepository.selectPage(pageInfo, wrapper);
    }

    /**
     * 根据ID获取志愿者
     */
    public Volunteer getVolunteerById(Long id) {
        return volunteerRepository.selectById(id);
    }

    /**
     * 创建志愿者
     */
    public Volunteer createVolunteer(Volunteer volunteer) {
        volunteer.setStatus("空闲");
        volunteerRepository.insert(volunteer);
        return volunteer;
    }

    /**
     * 更新志愿者
     */
    public Volunteer updateVolunteer(Long id, Volunteer volunteer) {
        volunteer.setId(id);
        volunteerRepository.updateById(volunteer);
        return volunteer;
    }

    /**
     * 删除志愿者
     */
    public void deleteVolunteer(Long id) {
        volunteerRepository.deleteById(id);
    }

    /**
     * 切换志愿者状态
     */
    public Volunteer toggleVolunteerStatus(Long id) {
        Volunteer volunteer = volunteerRepository.selectById(id);
        if (volunteer != null) {
            if ("空闲".equals(volunteer.getStatus())) {
                volunteer.setStatus("忙碌");
            } else {
                volunteer.setStatus("空闲");
            }
            volunteerRepository.updateById(volunteer);
        }
        return volunteer;
    }
}