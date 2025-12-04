package com.mahjong.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_room_id", columnList = "room_id"),
    @Index(name = "idx_sender_id", columnList = "sender_id"),
    @Index(name = "idx_message_type", columnList = "message_type"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 消息ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属房间
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_room"))
    private Room room;

    /**
     * 发送者ID
     */
    @Column(name = "sender_id", nullable = false, length = 36)
    private String senderId;

    /**
     * 发送者昵称
     */
    @Column(name = "sender_name", nullable = false, length = 20)
    private String senderName;

    /**
     * 消息类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.TEXT;

    /**
     * 消息内容
     */
    @Column(name = "message_content", nullable = false, length = 500)
    private String messageContent;

    /**
     * 消息数据（JSON格式，用于表情、图片等）
     */
    @Column(name = "message_data", columnDefinition = "JSON")
    private String messageData;

    /**
     * 目标玩家ID（私聊时使用）
     */
    @Column(name = "target_player_id", length = 36)
    private String targetPlayerId;

    /**
     * 目标玩家昵称（私聊时使用）
     */
    @Column(name = "target_player_name", length = 20)
    private String targetPlayerName;

    /**
     * 是否系统消息
     */
    @Column(name = "bool_system", nullable = false)
    private Boolean boolSystem = false;

    /**
     * 是否已删除
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /**
         * 文本消息
         */
        TEXT("文本消息"),

        /**
         * 表情消息
         */
        EMOJI("表情消息"),

        /**
         * 系统消息
         */
        SYSTEM("系统消息"),

        /**
         * 游戏消息
         */
        GAME("游戏消息"),

        /**
         * 图片消息
         */
        IMAGE("图片消息"),

        /**
         * 语音消息
         */
        VOICE("语音消息");

        private final String description;

        MessageType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查是否为系统消息
     */
    public boolean isSystemMessage() {
        return boolSystem || messageType == MessageType.SYSTEM;
    }

    /**
     * 检查是否为游戏消息
     */
    public boolean isGameMessage() {
        return messageType == MessageType.GAME;
    }

    /**
     * 检查是否为私聊消息
     */
    public boolean isPrivateMessage() {
        return targetPlayerId != null && !targetPlayerId.isEmpty();
    }

    /**
     * 检查消息是否为公开消息（非私聊）
     */
    public boolean isPublicMessage() {
        return !isPrivateMessage();
    }

    /**
     * 软删除消息
     */
    public void softDelete() {
        this.deleted = true;
    }

    /**
     * 检查消息是否已删除
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * 创建文本消息
     */
    public static ChatMessage createTextMessage(Room room, String senderId, String senderName, String content) {
        return ChatMessage.builder()
                .room(room)
                .senderId(senderId)
                .senderName(senderName)
                .messageType(MessageType.TEXT)
                .messageContent(content)
                .build();
    }

    /**
     * 创建系统消息
     */
    public static ChatMessage createSystemMessage(Room room, String content) {
        return ChatMessage.builder()
                .room(room)
                .senderId("system")
                .senderName("系统")
                .messageType(MessageType.SYSTEM)
                .messageContent(content)
                .boolSystem(true)
                .build();
    }

    /**
     * 创建游戏消息
     */
    public static ChatMessage createGameMessage(Room room, String senderId, String senderName, String content) {
        return ChatMessage.builder()
                .room(room)
                .senderId(senderId)
                .senderName(senderName)
                .messageType(MessageType.GAME)
                .messageContent(content)
                .build();
    }

    /**
     * 创建私聊消息
     */
    public static ChatMessage createPrivateMessage(Room room, String senderId, String senderName,
                                                 String targetPlayerId, String targetPlayerName, String content) {
        return ChatMessage.builder()
                .room(room)
                .senderId(senderId)
                .senderName(senderName)
                .targetPlayerId(targetPlayerId)
                .targetPlayerName(targetPlayerName)
                .messageType(MessageType.TEXT)
                .messageContent(content)
                .build();
    }
}