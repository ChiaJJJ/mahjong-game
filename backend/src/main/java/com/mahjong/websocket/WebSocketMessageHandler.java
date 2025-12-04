package com.mahjong.websocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.mahjong.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WebSocket消息处理器
 * 处理客户端的WebSocket连接和消息
 */
@Component
public class WebSocketMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageHandler.class);

    @Autowired
    private SocketIOServer socketIOServer;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * 客户端连接事件
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        log.info("客户端连接: {}", client.getSessionId());

        // 从连接参数中获取用户ID
        String userIdStr = client.getHandshakeData().getSingleUrlParam("userId");
        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                webSocketService.handleConnect(client, userId);
            } catch (NumberFormatException e) {
                log.error("无效的用户ID: {}", userIdStr);
                client.disconnect();
            }
        } else {
            log.warn("客户端连接缺少用户ID参数");
            client.disconnect();
        }
    }

    /**
     * 客户端断开连接事件
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info("客户端断开连接: {}", client.getSessionId());

        // 从连接参数中获取用户ID
        String userIdStr = client.getHandshakeData().getSingleUrlParam("userId");
        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                webSocketService.handleDisconnect(client, userId);
            } catch (NumberFormatException e) {
                log.error("无效的用户ID: {}", userIdStr);
            }
        }
    }

    /**
     * 加入房间事件
     */
    @OnEvent(value = "join_room")
    public void onJoinRoom(SocketIOClient client, AckRequest ackRequest, Object data) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) data;

            String userIdStr = client.getHandshakeData().getSingleUrlParam("userId");
            String roomId = (String) params.get("roomId");

            if (userIdStr != null && roomId != null) {
                Long userId = Long.parseLong(userIdStr);
                webSocketService.joinRoom(client, userId, roomId);

                if (ackRequest.isAckRequested()) {
                    ackRequest.sendAckData(Map.of(
                        "success", true,
                        "message", "成功加入房间频道"
                    ));
                }
            } else {
                log.warn("加入房间参数不完整: userId={}, roomId={}", userIdStr, roomId);
                sendErrorAck(ackRequest, "参数不完整");
            }
        } catch (Exception e) {
            log.error("处理加入房间事件失败", e);
            sendErrorAck(ackRequest, "服务器内部错误");
        }
    }

    /**
     * 离开房间事件
     */
    @OnEvent(value = "leave_room")
    public void onLeaveRoom(SocketIOClient client, AckRequest ackRequest, Object data) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) data;

            String userIdStr = client.getHandshakeData().getSingleUrlParam("userId");
            String roomId = (String) params.get("roomId");

            if (userIdStr != null && roomId != null) {
                Long userId = Long.parseLong(userIdStr);
                webSocketService.leaveRoom(userId, roomId);

                if (ackRequest.isAckRequested()) {
                    ackRequest.sendAckData(Map.of(
                        "success", true,
                        "message", "成功离开房间频道"
                    ));
                }
            } else {
                log.warn("离开房间参数不完整: userId={}, roomId={}", userIdStr, roomId);
                sendErrorAck(ackRequest, "参数不完整");
            }
        } catch (Exception e) {
            log.error("处理离开房间事件失败", e);
            sendErrorAck(ackRequest, "服务器内部错误");
        }
    }

    /**
     * 发送聊天消息事件
     */
    @OnEvent(value = "chat_message")
    public void onChatMessage(SocketIOClient client, AckRequest ackRequest, Object data) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = (Map<String, Object>) data;

            String userIdStr = client.getHandshakeData().getSingleUrlParam("userId");
            String roomId = (String) messageData.get("roomId");
            String content = (String) messageData.get("content");

            if (userIdStr != null && roomId != null && content != null) {
                Long userId = Long.parseLong(userIdStr);

                // 检查消息长度
                if (content.length() > 100) {
                    sendErrorAck(ackRequest, "消息长度不能超过100个字符");
                    return;
                }

                webSocketService.handleChatMessage(userId, roomId, content);

                if (ackRequest.isAckRequested()) {
                    ackRequest.sendAckData(Map.of(
                        "success", true,
                        "message", "消息发送成功"
                    ));
                }
            } else {
                log.warn("聊天消息参数不完整: userId={}, roomId={}, content={}",
                        userIdStr, roomId, content);
                sendErrorAck(ackRequest, "参数不完整");
            }
        } catch (Exception e) {
            log.error("处理聊天消息事件失败", e);
            sendErrorAck(ackRequest, "服务器内部错误");
        }
    }

    /**
     * 心跳检测事件
     */
    @OnEvent(value = "ping")
    public void onPing(SocketIOClient client, AckRequest ackRequest) {
        try {
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of(
                    "pong", System.currentTimeMillis()
                ));
            }
        } catch (Exception e) {
            log.error("处理心跳事件失败", e);
        }
    }

    /**
     * 获取房间在线状态事件
     */
    @OnEvent(value = "get_room_status")
    public void onGetRoomStatus(SocketIOClient client, AckRequest ackRequest, Object data) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) data;

            String roomId = (String) params.get("roomId");

            if (roomId != null) {
                int onlineCount = webSocketService.getRoomOnlineCount(roomId);
                var onlineUsers = webSocketService.getRoomOnlineUsers(roomId);

                Map<String, Object> status = Map.of(
                    "roomId", roomId,
                    "onlineCount", onlineCount,
                    "onlineUsers", onlineUsers,
                    "timestamp", System.currentTimeMillis()
                );

                if (ackRequest.isAckRequested()) {
                    ackRequest.sendAckData(Map.of(
                        "success", true,
                        "data", status
                    ));
                }
            } else {
                sendErrorAck(ackRequest, "房间ID不能为空");
            }
        } catch (Exception e) {
            log.error("处理获取房间状态事件失败", e);
            sendErrorAck(ackRequest, "服务器内部错误");
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorAck(AckRequest ackRequest, String errorMessage) {
        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(Map.of(
                "success", false,
                "message", errorMessage
            ));
        }
    }
}