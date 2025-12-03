package com.mahjong.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebSocket配置类
 * 配置Socket.IO服务器
 */
@Configuration
public class WebSocketConfig {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Value("${websocket.port:9914}")
    private int websocketPort;

    @Bean
    public SocketIOServer socketIOServer() {
        // 配置选项
        com.corundumstudio.socketio.Configuration socketConfig = new com.corundumstudio.socketio.Configuration();
        socketConfig.setHostname("0.0.0.0");
        socketConfig.setPort(websocketPort);
        socketConfig.setOrigin("*"); // 允许跨域，生产环境应限制
        socketConfig.setPingTimeout(60000);
        socketConfig.setPingInterval(25000);

        SocketIOServer server = new SocketIOServer(socketConfig);
        log.info("Socket.IO服务器启动在端口: {}", websocketPort);
        return server;
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketIOServer) {
        return new SpringAnnotationScanner(socketIOServer);
    }
}