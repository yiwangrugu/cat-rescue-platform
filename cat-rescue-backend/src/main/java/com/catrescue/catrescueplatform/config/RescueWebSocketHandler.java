package com.catrescue.catrescueplatform.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class RescueWebSocketHandler extends TextWebSocketHandler {

    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private static final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        // 发送连接成功消息
        try {
            session.sendMessage(new TextMessage("CONNECTED"));
        } catch (IOException e) {
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        // 从用户会话映射中移除
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到WebSocket消息: " + payload);

        // 处理客户端消息
        if ("PING".equals(payload)) {
            session.sendMessage(new TextMessage("PONG"));
        } else if (payload.startsWith("USER_ID:")) {
            // 客户端发送用户ID进行身份识别
            String userIdStr = payload.substring(8);
            try {
                Long userId = Long.parseLong(userIdStr);
                userSessions.put(userId, session);
                System.out.println("用户ID " + userId + " 已注册到WebSocket会话");
            } catch (NumberFormatException e) {
                System.err.println("无效的用户ID格式: " + userIdStr);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误: " + session.getId());
        exception.printStackTrace();
    }

    // 向所有连接的客户端发送消息
    public static void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("发送WebSocket消息失败: " + e.getMessage());
                }
            }
        }
    }

    // 向特定用户发送消息
    public static void sendToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                System.out.println("向用户 " + userId + " 发送消息: " + message);
            } catch (IOException e) {
                System.err.println("向用户 " + userId + " 发送消息失败: " + e.getMessage());
            }
        } else {
            System.out.println("用户 " + userId + " 的WebSocket会话不存在或已关闭");
        }
    }

    // 向管理员发送消息（管理员用户ID为1）
    public static void sendToAdmin(String message) {
        sendToUser(1L, message);
    }

    // 发送救援数据更新通知
    public static void notifyRescueUpdate() {
        broadcast("RESCUE_DATA_UPDATED");
    }

    // 发送帖子数据更新通知
    public static void notifyPostUpdate() {
        // 发送给管理员，通知有帖子被删除
        sendToAdmin("POST_APPLICATION_UPDATE:{\"action\":\"DELETE\",\"message\":\"有帖子被删除\"}");
    }

    // 发送评论数据更新通知
    public static void notifyCommentUpdate() {
        broadcast("COMMENT_DATA_UPDATED");
    }

    // 发送帖子申请更新通知（发送给管理员）
    public static void notifyPostApplicationUpdate(String data) {
        sendToAdmin("POST_APPLICATION_UPDATE:" + data);
    }

    // 发送帖子审核更新通知（发送给管理员和帖子作者）
    public static void notifyPostReviewUpdate(String data) {
        try {
            System.out.println("开始发送帖子审核更新通知，数据: " + data);

            // 解析数据获取用户ID
            String jsonData = data.substring(data.indexOf("{"));
            String userIdStr = jsonData.substring(jsonData.indexOf("\"userId\":") + 8);
            userIdStr = userIdStr.substring(0, userIdStr.indexOf(","));
            Long userId = Long.parseLong(userIdStr);

            System.out.println("解析出的用户ID: " + userId);
            System.out.println("当前用户会话映射: " + userSessions);

            // 发送给帖子作者
            System.out.println("准备发送给帖子作者 " + userId);
            sendToUser(userId, "POST_REVIEW_UPDATE:" + data);

            // 发送给管理员
            System.out.println("准备发送给管理员");
            sendToAdmin("POST_REVIEW_UPDATE:" + data);

            System.out.println("帖子审核更新通知发送完成");

        } catch (Exception e) {
            System.err.println("解析帖子审核更新数据失败: " + e.getMessage());
            // 如果解析失败，回退到广播模式
            broadcast("POST_REVIEW_UPDATE:" + data);
        }
    }

    // 发送领养申请更新通知
    public static void notifyAdoptionApplicationUpdate(String data) {
        broadcast("ADOPTION_APPLICATION_UPDATE:" + data);
    }

    // 发送待办事务统计更新通知（发送给管理员）
    public static void notifyPendingCountsUpdate() {
        sendToAdmin("PENDING_COUNTS_UPDATED");
    }

    // 发送领养审核更新通知
    public static void notifyAdoptionReviewUpdate(String data) {
        broadcast("ADOPTION_REVIEW_UPDATE:" + data);
    }
}