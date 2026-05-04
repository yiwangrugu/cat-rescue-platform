package com.catrescue.catrescueplatform.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.catrescue.catrescueplatform.service.UserService;
import com.catrescue.catrescueplatform.entity.User;
import com.catrescue.catrescueplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@RequiredArgsConstructor
public class UserWebSocketHandler extends TextWebSocketHandler {

    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private static final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        // 从session属性中获取token
        String token = (String) session.getAttributes().get("token");
        Long userId = extractUserIdFromToken(token);

        if (userId != null) {
            userSessions.put(userId, session);
            System.out.println("用户状态WebSocket连接建立: " + session.getId() + ", 用户ID: " + userId + ", 当前连接数: "
                    + sessions.size() + ", 已连接用户: " + userSessions.keySet());
        } else {
            System.out.println("用户状态WebSocket连接建立: " + session.getId() + ", 无法识别用户, 当前连接数: " + sessions.size());
        }

        // 发送连接成功消息
        try {
            session.sendMessage(new TextMessage("USER_CONNECTED"));
        } catch (IOException e) {
            System.err.println("发送用户状态连接成功消息失败: " + e.getMessage());
        }
    }

    // 从token中提取用户ID（使用JWT解析）
    private Long extractUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            System.out.println("WebSocket token: " + token);

            // 使用JWT工具类解析用户名
            String username = jwtUtil.extractUsername(token);
            System.out.println("从token中解析出的用户名: " + username);

            if (username != null && !username.isEmpty()) {
                // 根据用户名查询用户ID
                User user = userService.findByUsername(username);
                if (user != null) {
                    System.out.println("找到用户: " + user.getUsername() + ", ID: " + user.getId());
                    return user.getId();
                } else {
                    System.err.println("未找到用户: " + username);
                }
            } else {
                System.err.println("无法从token中解析用户名");
            }

            return null;

        } catch (Exception e) {
            System.err.println("解析token失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);

        // 从userSessions中移除对应的session
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));

        System.out.println("用户状态WebSocket连接关闭: " + session.getId() + ", 状态: " + status + ", 当前连接数: " + sessions.size());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到用户状态WebSocket消息: " + payload);

        // 处理客户端消息
        if ("PING".equals(payload)) {
            session.sendMessage(new TextMessage("PONG"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("用户状态WebSocket传输错误: " + session.getId());
        exception.printStackTrace();
    }

    // 向所有连接的用户客户端发送消息
    public static void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("发送用户状态WebSocket消息失败: " + e.getMessage());
                }
            }
        }
    }

    // 向指定用户发送消息
    public static void sendToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                System.out.println("向用户 " + userId + " 发送WebSocket消息: " + message);
            } catch (IOException e) {
                System.err.println("向用户 " + userId + " 发送WebSocket消息失败: " + e.getMessage());
            }
        } else {
            System.out.println("用户 " + userId + " 的WebSocket会话不存在或已关闭，当前已连接用户: " + userSessions.keySet());
        }
    }

    // 发送用户状态更新通知
    public static void notifyUserStatusUpdate() {
        broadcast("USER_STATUS_UPDATED");
    }

    // 发送用户被禁用通知（定向发送）
    public static void notifyUserBanned(Long userId, String endTime) {
        String message = String.format("USER_BANNED|%s|%s", userId, endTime);
        System.out.println("🔴 发送用户禁用通知 - 用户ID: " + userId + ", 解禁时间: " + endTime);
        System.out.println("🔴 当前已连接的WebSocket会话: " + userSessions.keySet());
        sendToUser(userId, message);
    }

    // 发送用户启用通知（定向发送）
    public static void notifyUserUnbanned(Long userId) {
        String message = String.format("USER_UNBANNED|%s", userId);
        sendToUser(userId, message);
    }
}