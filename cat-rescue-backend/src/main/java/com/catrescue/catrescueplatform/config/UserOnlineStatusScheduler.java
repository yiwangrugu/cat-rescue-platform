package com.catrescue.catrescueplatform.config;

import com.catrescue.catrescueplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class UserOnlineStatusScheduler {

    private final UserService userService;

    @Scheduled(cron = "0 0 * * * ?")
    public void updateUserOnlineStatus() {
        userService.setInactiveUsersOffline(3);
    }
}
