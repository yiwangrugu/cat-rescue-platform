package com.catrescue.catrescueplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catrescue.catrescueplatform.entity.Volunteer;
import com.catrescue.catrescueplatform.entity.VolunteerSchedule;
import com.catrescue.catrescueplatform.repository.VolunteerScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VolunteerScheduleService {

    private final VolunteerScheduleRepository scheduleRepository;
    private final VolunteerService volunteerService;

    public List<VolunteerSchedule> getSchedulesByVolunteerId(Long volunteerId) {
        QueryWrapper<VolunteerSchedule> wrapper = new QueryWrapper<>();
        wrapper.eq("volunteer_id", volunteerId);
        wrapper.orderByAsc("day_of_week");
        return scheduleRepository.selectList(wrapper);
    }

    public Map<Integer, VolunteerSchedule> getSchedulesMapByVolunteerId(Long volunteerId) {
        List<VolunteerSchedule> schedules = getSchedulesByVolunteerId(volunteerId);
        return schedules.stream()
                .collect(Collectors.toMap(VolunteerSchedule::getDayOfWeek, s -> s));
    }

    public List<VolunteerSchedule> getSchedulesByDayOfWeek(Integer dayOfWeek) {
        QueryWrapper<VolunteerSchedule> wrapper = new QueryWrapper<>();
        wrapper.eq("day_of_week", dayOfWeek);
        wrapper.eq("is_on_duty", true);
        return scheduleRepository.selectList(wrapper);
    }

    @Transactional
    public void saveOrUpdateSchedules(Long volunteerId, List<VolunteerSchedule> schedules) {
        QueryWrapper<VolunteerSchedule> wrapper = new QueryWrapper<>();
        wrapper.eq("volunteer_id", volunteerId);
        scheduleRepository.delete(wrapper);

        for (VolunteerSchedule schedule : schedules) {
            schedule.setVolunteerId(volunteerId);
            scheduleRepository.insert(schedule);
        }
    }

    @Transactional
    public void initDefaultSchedules(Long volunteerId) {
        List<VolunteerSchedule> schedules = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            VolunteerSchedule schedule = new VolunteerSchedule();
            schedule.setVolunteerId(volunteerId);
            schedule.setDayOfWeek(i);
            schedule.setIsOnDuty(false);
            schedule.setStartTime("09:00");
            schedule.setEndTime("18:00");
            schedules.add(schedule);
        }
        saveOrUpdateSchedules(volunteerId, schedules);
    }

    @Transactional
    public void updateSchedule(VolunteerSchedule schedule) {
        scheduleRepository.updateById(schedule);
    }

    @Transactional
    public void deleteSchedulesByVolunteerId(Long volunteerId) {
        QueryWrapper<VolunteerSchedule> wrapper = new QueryWrapper<>();
        wrapper.eq("volunteer_id", volunteerId);
        scheduleRepository.delete(wrapper);
    }

    /**
     * 获取今日值班的志愿者详细信息列表
     */
    public List<Volunteer> getTodayOnDutyVolunteers() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        int dayOfWeekValue = dayOfWeek.getValue();

        List<VolunteerSchedule> todaySchedules = getSchedulesByDayOfWeek(dayOfWeekValue);

        List<Volunteer> volunteers = new ArrayList<>();
        for (VolunteerSchedule schedule : todaySchedules) {
            Volunteer volunteer = volunteerService.getVolunteerById(schedule.getVolunteerId());
            if (volunteer != null && "空闲".equals(volunteer.getStatus())) {
                volunteers.add(volunteer);
            }
        }
        return volunteers;
    }
}
