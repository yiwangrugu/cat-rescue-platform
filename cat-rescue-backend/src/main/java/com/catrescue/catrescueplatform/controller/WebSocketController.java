package com.catrescue.catrescueplatform.controller;

import com.catrescue.catrescueplatform.config.RescueWebSocketHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
public class WebSocketController {

    @MessageMapping("/rescue.update")
    @SendTo("/topic/rescue")
    public String handleRescueUpdate(String message) {
        return "RESCUE_DATA_UPDATED";
    }

    /**
     * 帖子申请实时更新
     */
    @PostMapping("/ws/broadcast/post-application")
    public String broadcastPostApplication(@RequestBody String message) {
        RescueWebSocketHandler.broadcast("POST_APPLICATION_UPDATE:" + message);
        return "帖子申请更新消息已发送";
    }

    /**
     * 帖子审核实时更新
     */
    @PostMapping("/ws/broadcast/post-review")
    public String broadcastPostReview(@RequestBody String message) {
        RescueWebSocketHandler.broadcast("POST_REVIEW_UPDATE:" + message);
        return "帖子审核更新消息已发送";
    }

    /**
     * 领养申请实时更新
     */
    @PostMapping("/ws/broadcast/adoption-application")
    public String broadcastAdoptionApplication(@RequestBody String message) {
        RescueWebSocketHandler.broadcast("ADOPTION_APPLICATION_UPDATE:" + message);
        return "领养申请更新消息已发送";
    }

    /**
     * 领养审核实时更新
     */
    @PostMapping("/ws/broadcast/adoption-review")
    public String broadcastAdoptionReview(@RequestBody String message) {
        RescueWebSocketHandler.broadcast("ADOPTION_REVIEW_UPDATE:" + message);
        return "领养审核更新消息已发送";
    }
}