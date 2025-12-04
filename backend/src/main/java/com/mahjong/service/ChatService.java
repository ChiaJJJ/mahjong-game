package com.mahjong.service;

import com.mahjong.entity.Player;
import com.mahjong.entity.Room;
import com.mahjong.repository.PlayerRepository;
import com.mahjong.repository.RoomRepository;
import com.mahjong.service.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 聊天服务类
 * 处理游戏内聊天功能
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    private WebSocketService webSocketService;

    // 聊天消息内容验证正则表达式
    private static final Pattern CONTENT_PATTERN = Pattern.compile("^[\\s\\S]{1,100}$");

    // 敏感词过滤（简单示例）
    private static final String[] SENSITIVE_WORDS = {
        "政治", "色情", "暴力", "赌博", "毒品"
    };

    /**
     * 发送聊天消息
     *
     * @param userId    用户ID
     * @param roomId    房间号
     * @param content   消息内容
     * @return 发送结果
     */
    public ApiResponse<Map<String, Object>> sendChatMessage(Long userId, String roomId, String content) {
        try {
            log.info("用户 {} 在房间 {} 发送聊天消息: {}", userId, roomId, content);

            // 验证消息内容
            String validationResult = validateMessageContent(content);
            if (validationResult != null) {
                return ApiResponse.badRequest(validationResult);
            }

            // 检查用户是否存在
            Player player = playerRepository.findById(userId.toString()).orElse(null);
            if (player == null) {
                return ApiResponse.notFound("用户不存在");
            }

            // 检查房间是否存在
            Room room = roomRepository.findByRoomNumber(roomId).orElse(null);
            if (room == null) {
                return ApiResponse.notFound("房间不存在");
            }

            // 检查用户是否在房间中
            Player playerInRoom = playerRepository.findByRoomIdAndId(room.getId(), String.valueOf(userId)).orElse(null);
            if (playerInRoom == null) {
                return ApiResponse.badRequest("您不在此房间中");
            }

            // 过滤敏感词
            String filteredContent = filterSensitiveWords(content);

            // 发送消息
            webSocketService.handleChatMessage(userId, roomId, filteredContent);

            log.info("聊天消息发送成功: 用户={}, 房间={}", userId, roomId);
            return ApiResponse.success("消息发送成功", Map.of("status", "sent", "roomId", roomId));

        } catch (Exception e) {
            log.error("发送聊天消息失败", e);
            return ApiResponse.error("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 发送系统消息到房间
     *
     * @param roomId    房间号
     * @param content   消息内容
     * @return 发送结果
     */
    public ApiResponse<Map<String, Object>> sendSystemMessage(String roomId, String content) {
        try {
            log.info("向房间 {} 发送系统消息: {}", roomId, content);

            // 检查房间是否存在
            Room room = roomRepository.findByRoomNumber(roomId).orElse(null);
            if (room == null) {
                return ApiResponse.notFound("房间不存在");
            }

            // 发送系统消息
            webSocketService.sendSystemMessage(roomId, content);

            log.info("系统消息发送成功: 房间={}", roomId);
            return ApiResponse.success("系统消息发送成功", Map.of("status", "sent", "roomId", roomId));

        } catch (Exception e) {
            log.error("发送系统消息失败", e);
            return ApiResponse.error("发送系统消息失败: " + e.getMessage());
        }
    }

    /**
     * 广播系统消息
     *
     * @param content   消息内容
     * @return 发送结果
     */
    public ApiResponse<Map<String, Object>> broadcastSystemMessage(String content) {
        try {
            log.info("广播系统消息: {}", content);

            Map<String, Object> systemMessage = Map.of(
                "id", java.util.UUID.randomUUID().toString(),
                "sender", "系统",
                "content", content,
                "timestamp", System.currentTimeMillis(),
                "type", "system",
                "scope", "global"
            );

            webSocketService.broadcastMessage("chat_message", systemMessage);

            log.info("系统消息广播成功");
            return ApiResponse.success("系统消息广播成功", Map.of("status", "broadcast"));

        } catch (Exception e) {
            log.error("广播系统消息失败", e);
            return ApiResponse.error("广播系统消息失败: " + e.getMessage());
        }
    }

    /**
     * 验证消息内容
     *
     * @param content 消息内容
     * @return 验证错误信息，null表示验证通过
     */
    private String validateMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "消息内容不能为空";
        }

        if (content.length() > 100) {
            return "消息长度不能超过100个字符";
        }

        if (!CONTENT_PATTERN.matcher(content).matches()) {
            return "消息内容包含非法字符";
        }

        return null;
    }

    /**
     * 过滤敏感词
     *
     * @param content 原始内容
     * @return 过滤后的内容
     */
    private String filterSensitiveWords(String content) {
        String result = content;
        for (String word : SENSITIVE_WORDS) {
            result = result.replaceAll(word, "***");
        }
        return result;
    }

    /**
     * 发送玩家加入消息
     *
     * @param roomId    房间号
     * @param player    玩家信息
     */
    public void sendPlayerJoinMessage(String roomId, Player player) {
        String message = String.format("玩家 %s 加入了房间", player.getPlayerName());
        sendSystemMessage(roomId, message);
    }

    /**
     * 发送玩家离开消息
     *
     * @param roomId    房间号
     * @param player    玩家信息
     */
    public void sendPlayerLeaveMessage(String roomId, Player player) {
        String message = String.format("玩家 %s 离开了房间", player.getPlayerName());
        sendSystemMessage(roomId, message);
    }

    /**
     * 发送游戏状态变化消息
     *
     * @param roomId      房间号
     * @param gameStatus  游戏状态
     */
    public void sendGameStatusChangeMessage(String roomId, String gameStatus) {
        String message;
        switch (gameStatus.toLowerCase()) {
            case "waiting":
                message = "游戏等待开始";
                break;
            case "playing":
                message = "游戏已经开始";
                break;
            case "finished":
                message = "游戏已经结束";
                break;
            default:
                message = "游戏状态: " + gameStatus;
        }
        sendSystemMessage(roomId, message);
    }

    /**
     * 发送准备状态变化消息
     *
     * @param roomId    房间号
     * @param player    玩家信息
     * @param isReady   是否准备
     */
    public void sendReadyStatusChangeMessage(String roomId, Player player, boolean isReady) {
        String message = String.format("玩家 %s %s了准备",
            player.getPlayerName(),
            isReady ? "完成" : "取消");
        sendSystemMessage(roomId, message);
    }
}