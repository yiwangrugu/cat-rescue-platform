package com.catrescue.catrescueplatform.controller;

import com.catrescue.catrescueplatform.entity.Volunteer;
import com.catrescue.catrescueplatform.entity.VolunteerSchedule;
import com.catrescue.catrescueplatform.service.VolunteerScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VolunteerScheduleController {

    private final VolunteerScheduleService scheduleService;

    /**
     * 获取今日值班志愿者列表（公开接口）
     */
    @GetMapping("/api/volunteer-schedules/today")
    public ResponseEntity<List<Volunteer>> getTodayOnDutyVolunteers() {
        return ResponseEntity.ok(scheduleService.getTodayOnDutyVolunteers());
    }

    @GetMapping("/api/admin/volunteer-schedules/volunteer/{volunteerId}")
    public ResponseEntity<List<VolunteerSchedule>> getSchedulesByVolunteerId(@PathVariable Long volunteerId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByVolunteerId(volunteerId));
    }

    @GetMapping("/api/admin/volunteer-schedules/volunteer/{volunteerId}/map")
    public ResponseEntity<Map<Integer, VolunteerSchedule>> getSchedulesMapByVolunteerId(
            @PathVariable Long volunteerId) {
        return ResponseEntity.ok(scheduleService.getSchedulesMapByVolunteerId(volunteerId));
    }

    @GetMapping("/api/admin/volunteer-schedules/day/{dayOfWeek}")
    public ResponseEntity<List<VolunteerSchedule>> getSchedulesByDayOfWeek(@PathVariable Integer dayOfWeek) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDayOfWeek(dayOfWeek));
    }

    @PostMapping("/api/admin/volunteer-schedules/volunteer/{volunteerId}")
    public ResponseEntity<Void> saveOrUpdateSchedules(
            @PathVariable Long volunteerId,
            @RequestBody List<VolunteerSchedule> schedules) {
        scheduleService.saveOrUpdateSchedules(volunteerId, schedules);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/admin/volunteer-schedules/volunteer/{volunteerId}/init")
    public ResponseEntity<Void> initDefaultSchedules(@PathVariable Long volunteerId) {
        scheduleService.initDefaultSchedules(volunteerId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/admin/volunteer-schedules/{id}")
    public ResponseEntity<Void> updateSchedule(@PathVariable Long id, @RequestBody VolunteerSchedule schedule) {
        schedule.setId(id);
        scheduleService.updateSchedule(schedule);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/admin/volunteer-schedules/volunteer/{volunteerId}")
    public ResponseEntity<Void> deleteSchedulesByVolunteerId(@PathVariable Long volunteerId) {
        scheduleService.deleteSchedulesByVolunteerId(volunteerId);
        return ResponseEntity.ok().build();
    }
}
