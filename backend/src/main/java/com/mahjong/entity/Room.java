package com.mahjong.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Entity
@Table(name = "rooms", indexes = {
    @Index(name = "idx_room_number", columnList = "room_number"),
    @Index(name = "idx_creator_id", columnList = "creator_id"),
    @Index(name = "idx_room_status", columnList = "room_status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    /**
     * 房间ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 房间号 - 唯一标识
     */
    @Column(name = "room_number", nullable = false, unique = true, length = 8)
    private String roomNumber;

    /**
     * 房间名称
     */
    @Column(name = "room_name", length = 50)
    private String roomName;

    /**
     * 房间密码（可选）
     */
    @Column(name = "password", length = 20)
    private String password;

    /**
     * 创建者ID
     */
    @Column(name = "creator_id", nullable = false, length = 36)
    private String creatorId;

    /**
     * 最大玩家数
     */
    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers = 4;

    /**
     * 当前玩家数
     */
    @Column(name = "current_players", nullable = false)
    private Integer currentPlayers = 0;

    /**
     * 房间状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false)
    private RoomStatus roomStatus = RoomStatus.WAITING;

    /**
     * 游戏配置
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_config_id", foreignKey = @ForeignKey(name = "fk_room_game_config"))
    private GameConfig gameConfig;

    /**
     * 是否允许观战
     */
    @Column(name = "allow_spectate", nullable = false)
    private Boolean allowSpectate = true;

    /**
     * 观战人数
     */
    @Column(name = "spectator_count", nullable = false)
    private Integer spectatorCount = 0;

    /**
     * 过期时间
     */
    @Column(name = "expires_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 房间状态枚举
     */
    public enum RoomStatus {
        /**
         * 等待中
         */
        WAITING("等待中"),

        /**
         * 游戏中
         */
        PLAYING("游戏中"),

        /**
         * 已结束
         */
        FINISHED("已结束");

        private final String description;

        RoomStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 关联的玩家列表
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Player> players;

    /**
     * 关联的游戏列表
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Game> games;

    /**
     * 关联的聊天消息
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChatMessage> chatMessages;

    /**
     * 检查房间是否已满
     */
    public boolean isFull() {
        return currentPlayers >= maxPlayers;
    }

    /**
     * 检查是否可以加入游戏
     */
    public boolean canJoin() {
        return roomStatus == RoomStatus.WAITING && !isFull();
    }

    /**
     * 检查是否可以开始游戏
     */
    public boolean canStart() {
        return roomStatus == RoomStatus.WAITING && currentPlayers >= 2;
    }

    /**
     * 增加玩家数量
     */
    public void incrementPlayerCount() {
        this.currentPlayers++;
    }

    /**
     * 减少玩家数量
     */
    public void decrementPlayerCount() {
        if (this.currentPlayers > 0) {
            this.currentPlayers--;
        }
    }

    /**
     * 增加观战人数
     */
    public void incrementSpectatorCount() {
        this.spectatorCount++;
    }

    /**
     * 减少观战人数
     */
    public void decrementSpectatorCount() {
        if (this.spectatorCount > 0) {
            this.spectatorCount--;
        }
    }

    /**
     * 设置房间状态为游戏中
     */
    public void startGame() {
        this.roomStatus = RoomStatus.PLAYING;
    }

    /**
     * 设置房间状态为等待中
     */
    public void endGame() {
        this.roomStatus = RoomStatus.WAITING;
    }

    /**
     * 检查房间是否过期
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 检查是否允许观战
     */
    public boolean allowsSpectate() {
        return allowSpectate != null && allowSpectate;
    }
}