package com.mahjong.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.dto.response.RoomResponse;
import com.mahjong.entity.Player;
import com.mahjong.entity.Room;
import com.mahjong.repository.PlayerRepository;
import com.mahjong.service.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务类
 * 处理房间状态同步和实时通信
 */
@Service
public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    @Autowired
    private SocketIOServer socketIOServer;

    @Autowired
    private RoomService roomService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 存储用户ID到客户端连接的映射
    private final Map<Long, SocketIOClient> userClientMap = new ConcurrentHashMap<>();

    // 存储房间ID到客户端集合的映射
    private final Map<String, Map<Long, SocketIOClient>> roomClientMap = new ConcurrentHashMap<>();

    /**
     * 用户连接WebSocket
     */
    public void handleConnect(SocketIOClient client, Long userId) {
        log.info("用户 {} 建立WebSocket连接", userId);

        // 存储客户端连接
        userClientMap.put(userId, client);

        // 获取用户信息
        Player player = playerRepository.findById(userId.toString()).orElse(null);
        if (player != null && player.getRoom() != null) {
            // 如果用户已在房间中，加入房间频道
            joinRoom(client, userId, player.getRoom().getRoomNumber());
        }

        // 发送连接成功消息
        sendMessageToUser(userId, "connect_success", Map.of(
            "userId", userId,
            "message", "WebSocket连接成功"
        ));
    }

    /**
     * 用户断开WebSocket连接
     */
    public void handleDisconnect(SocketIOClient client, Long userId) {
        log.info("用户 {} 断开WebSocket连接", userId);

        // 从所有房间中移除用户
        userClientMap.remove(userId);

        // 从房间映射中移除用户
        roomClientMap.forEach((roomId, clients) -> {
            if (clients.remove(userId) != null) {
                log.debug("用户 {} 离开房间 {}", userId, roomId);
            }
        });
    }

    /**
     * 用户加入房间频道
     */
    public void joinRoom(SocketIOClient client, Long userId, String roomId) {
        // 加入房间频道
        client.joinRoom(roomId);

        // 更新房间客户端映射
        roomClientMap.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(userId, client);

        log.debug("用户 {} 加入房间频道 {}", userId, roomId);
    }

    /**
     * 用户离开房间频道
     */
    public void leaveRoom(Long userId, String roomId) {
        SocketIOClient client = userClientMap.get(userId);
        if (client != null) {
            client.leaveRoom(roomId);
        }

        // 从房间映射中移除用户
        Map<Long, SocketIOClient> clients = roomClientMap.get(roomId);
        if (clients != null) {
            clients.remove(userId);
            if (clients.isEmpty()) {
                roomClientMap.remove(roomId);
            }
        }

        log.debug("用户 {} 离开房间频道 {}", userId, roomId);
    }

    /**
     * 向指定用户发送消息
     */
    public void sendMessageToUser(Long userId, String event, Object data) {
        SocketIOClient client = userClientMap.get(userId);
        if (client != null) {
            try {
                client.sendEvent(event, data);
                log.debug("向用户 {} 发送消息 {}: {}", userId, event, data);
            } catch (Exception e) {
                log.error("向用户 {} 发送消息失败: {}", userId, e.getMessage(), e);
            }
        }
    }

    /**
     * 向房间内所有用户发送消息
     */
    public void sendMessageToRoom(String roomId, String event, Object data) {
        try {
            socketIOServer.getRoomOperations(roomId).sendEvent(event, data);
            log.debug("向房间 {} 发送消息 {}: {}", roomId, event, data);
        } catch (Exception e) {
            log.error("向房间 {} 发送消息失败: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * 向房间内除指定用户外的所有用户发送消息
     */
    public void sendMessageToRoomExcept(String roomId, Long excludeUserId, String event, Object data) {
        Map<Long, SocketIOClient> clients = roomClientMap.get(roomId);
        if (clients != null) {
            clients.forEach((userId, client) -> {
                if (!userId.equals(excludeUserId)) {
                    try {
                        client.sendEvent(event, data);
                    } catch (Exception e) {
                        log.error("向用户 {} 发送房间消息失败: {}", userId, e.getMessage(), e);
                    }
                }
            });
        }
    }

    /**
     * 广播消息给所有连接的客户端
     */
    public void broadcastMessage(String event, Object data) {
        try {
            socketIOServer.getBroadcastOperations().sendEvent(event, data);
            log.debug("广播消息 {}: {}", event, data);
        } catch (Exception e) {
            log.error("广播消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 同步房间状态变化
     */
    public void syncRoomStateChange(String roomId, String changeType, Object data) {
        Room room = null;
        ApiResponse<Room> response = roomService.getRoomInfo(roomId);
        if (response.isSuccess() && response.getData() != null) {
            room = response.getData();
        }
        if (room != null) {
            RoomResponse roomResponse = roomService.convertToRoomResponse(room);

            Map<String, Object> message = Map.of(
                "changeType", changeType,
                "data", data,
                "roomState", roomResponse
            );

            sendMessageToRoom(roomId, "room_state_change", message);
            log.info("同步房间 {} 状态变化: {}", roomId, changeType);
        }
    }

    /**
     * 通知用户加入房间
     */
    public void notifyPlayerJoined(String roomId, Player player) {
        Map<String, Object> data = Map.of(
            "player", player,
            "timestamp", System.currentTimeMillis()
        );

        sendMessageToRoomExcept(roomId, Long.parseLong(player.getId()), "player_joined", data);
        log.info("通知房间 {} 用户 {} 加入", roomId, player.getId());
    }

    /**
     * 通知用户离开房间
     */
    public void notifyPlayerLeft(String roomId, Long playerId) {
        Map<String, Object> data = Map.of(
            "playerId", playerId,
            "timestamp", System.currentTimeMillis()
        );

        sendMessageToRoom(roomId, "player_left", data);
        log.info("通知房间 {} 用户 {} 离开", roomId, playerId);
    }

    /**
     * 通知房间游戏状态变化
     */
    public void notifyGameStatusChange(String roomId, String gameStatus) {
        Map<String, Object> data = Map.of(
            "gameStatus", gameStatus,
            "timestamp", System.currentTimeMillis()
        );

        sendMessageToRoom(roomId, "game_status_change", data);
        log.info("通知房间 {} 游戏状态变化: {}", roomId, gameStatus);
    }

    /**
     * 通知用户准备状态变化
     */
    public void notifyPlayerReadyChange(String roomId, Long playerId, boolean isReady) {
        Map<String, Object> data = Map.of(
            "playerId", playerId,
            "isReady", isReady,
            "timestamp", System.currentTimeMillis()
        );

        sendMessageToRoom(roomId, "player_ready_change", data);
        log.info("通知房间 {} 用户 {} 准备状态变化: {}", roomId, playerId, isReady);
    }

    /**
     * 处理聊天消息
     */
    public void handleChatMessage(Long userId, String roomId, String content) {
        Player player = playerRepository.findById(userId.toString()).orElse(null);
        if (player != null) {
            Map<String, Object> chatMessage = Map.of(
                "id", UUID.randomUUID().toString(),
                "senderId", userId,
                "sender", player.getPlayerName(),
                "avatarUrl", player.getPlayerAvatar(),
                "content", content,
                "timestamp", System.currentTimeMillis(),
                "type", "user"
            );

            sendMessageToRoom(roomId, "chat_message", chatMessage);
            log.info("用户 {} 在房间 {} 发送聊天消息: {}", userId, roomId, content);
        }
    }

    /**
     * 处理系统消息
     */
    public void sendSystemMessage(String roomId, String content) {
        Map<String, Object> systemMessage = Map.of(
            "id", UUID.randomUUID().toString(),
            "sender", "系统",
            "content", content,
            "timestamp", System.currentTimeMillis(),
            "type", "system"
        );

        sendMessageToRoom(roomId, "chat_message", systemMessage);
        log.info("房间 {} 系统消息: {}", roomId, content);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return userClientMap.containsKey(userId);
    }

    /**
     * 获取房间在线用户数
     */
    public int getRoomOnlineCount(String roomId) {
        Map<Long, SocketIOClient> clients = roomClientMap.get(roomId);
        return clients != null ? clients.size() : 0;
    }

    /**
     * 获取房间在线用户列表
     */
    public java.util.Set<Long> getRoomOnlineUsers(String roomId) {
        Map<Long, SocketIOClient> clients = roomClientMap.get(roomId);
        return clients != null ? clients.keySet() : java.util.Collections.emptySet();
    }
}