package com.catrescue.catrescueplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SimpleWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 创建握手拦截器
        WebSocketHandshakeInterceptor handshakeInterceptor = new WebSocketHandshakeInterceptor();

        // 救援相关的WebSocket连接
        registry.addHandler(new RescueWebSocketHandler(), "/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        // 用户状态相关的WebSocket连接 - 使用简化的处理器
        registry.addHandler(new SimpleUserWebSocketHandler(), "/ws/user")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);
    }
}