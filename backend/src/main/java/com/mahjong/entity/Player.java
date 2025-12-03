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

/**
 * 玩家实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Entity
@Table(name = "players",
    indexes = {
        @Index(name = "idx_room_id", columnList = "roomId"),
        @Index(name = "idx_player_name", columnList = "playerName"),
        @Index(name = "idx_player_status", columnList = "playerStatus"),
        @Index(name = "idx_last_active_at", columnList = "lastActiveAt")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_room_position", columnNames = {"roomId", "playerPosition"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    /**
     * 玩家唯一标识
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /**
     * 所属房间ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_player_room"))
    private Room room;

    /**
     * 玩家昵称
     */
    @Column(name = "player_name", nullable = false, length = 20)
    private String playerName;

    /**
     * 玩家头像URL
     */
    @Column(name = "player_avatar", length = 200)
    private String playerAvatar;

    /**
     * 玩家位置(1-4)
     */
    @Column(name = "player_position", nullable = false)
    private Integer playerPosition;

    /**
     * 玩家状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "player_status", nullable = false)
    private PlayerStatus playerStatus = PlayerStatus.ONLINE;

    /**
     * 是否为观战者
     */
    @Column(name = "spectator", nullable = false)
    private Boolean spectator = false;

    /**
     * 总分数
     */
    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    /**
     * 获胜次数
     */
    @Column(name = "wins_count", nullable = false)
    private Integer winsCount = 0;

    /**
     * 准备时间
     */
    @Column(name = "ready_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readyAt;

    /**
     * 加入时间
     */
    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedAt;

    /**
     * 最后活跃时间
     */
    @UpdateTimestamp
    @Column(name = "last_active_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;

    /**
     * 玩家状态枚举
     */
    public enum PlayerStatus {
        /**
         * 在线
         */
        ONLINE("在线"),

        /**
         * 离线
         */
        OFFLINE("离线"),

        /**
         * 准备
         */
        READY("准备"),

        /**
         * 游戏中
         */
        PLAYING("游戏中");

        private final String description;

        PlayerStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查玩家是否在线
     */
    public boolean isOnline() {
        return playerStatus == PlayerStatus.ONLINE || playerStatus == PlayerStatus.READY || playerStatus == PlayerStatus.PLAYING;
    }

    /**
     * 检查玩家是否已准备
     */
    public boolean isReady() {
        return playerStatus == PlayerStatus.READY;
    }

    /**
     * 检查玩家是否在游戏中
     */
    public boolean isPlaying() {
        return playerStatus == PlayerStatus.PLAYING;
    }

    /**
     * 设置玩家为准备状态
     */
    public void setReady() {
        this.playerStatus = PlayerStatus.READY;
        this.readyAt = LocalDateTime.now();
    }

    /**
     * 设置玩家为游戏中状态
     */
    public void setPlaying() {
        this.playerStatus = PlayerStatus.PLAYING;
    }

    /**
     * 设置玩家为离线状态
     */
    public void setOffline() {
        this.playerStatus = PlayerStatus.OFFLINE;
    }

    /**
     * 设置玩家为在线状态
     */
    public void setOnline() {
        this.playerStatus = PlayerStatus.ONLINE;
    }

    /**
     * 增加分数
     */
    public void addScore(int score) {
        this.totalScore += score;
    }

    /**
     * 减少分数
     */
    public void subtractScore(int score) {
        this.totalScore -= score;
    }

    /**
     * 增加胜利次数
     */
    public void addWin() {
        this.winsCount++;
    }

    /**
     * 重置玩家状态（新一局游戏）
     */
    public void resetForNewGame() {
        this.playerStatus = PlayerStatus.ONLINE;
        this.readyAt = null;
    }

    /**
     * 检查玩家是否长时间未活跃（超过5分钟）
     */
    public boolean isInactive() {
        return LocalDateTime.now().isAfter(lastActiveAt.plusMinutes(5));
    }
}