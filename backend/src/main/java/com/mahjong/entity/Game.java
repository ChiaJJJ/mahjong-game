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
 * 游戏实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Entity
@Table(name = "games", indexes = {
    @Index(name = "idx_room_id", columnList = "roomId"),
    @Index(name = "idx_game_status", columnList = "gameStatus"),
    @Index(name = "idx_current_player", columnList = "currentPlayer"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    /**
     * 游戏ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属房间
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_game_room"))
    private Room room;

    /**
     * 游戏状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "game_status", nullable = false)
    private GameStatus gameStatus = GameStatus.WAITING;

    /**
     * 当前玩家位置（1-4）
     */
    @Column(name = "current_player")
    private Integer currentPlayer;

    /**
     * 当前回合数
     */
    @Column(name = "round_number", nullable = false)
    private Integer roundNumber = 1;

    /**
     * 庄家位置
     */
    @Column(name = "dealer_position")
    private Integer dealerPosition;

    /**
     * 游戏数据（JSON格式，包含手牌、弃牌堆等信息）
     */
    @Column(name = "game_data", columnDefinition = "JSON")
    private String gameData;

    /**
     * 混牌信息
     */
    @Column(name = "mixed_tiles", columnDefinition = "JSON")
    private String mixedTiles;

    /**
     * 弃牌堆（JSON格式）
     */
    @Column(name = "discard_pile", columnDefinition = "JSON")
    private String discardPile;

    /**
     * 最后操作信息（JSON格式）
     */
    @Column(name = "last_action", columnDefinition = "JSON")
    private String lastAction;

    /**
     * 游戏开始时间
     */
    @Column(name = "started_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    /**
     * 游戏结束时间
     */
    @Column(name = "ended_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endedAt;

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
     * 游戏状态枚举
     */
    public enum GameStatus {
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

        GameStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 关联的游戏回合
     */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GameRound> rounds;

    /**
     * 检查游戏是否正在进行
     */
    public boolean isPlaying() {
        return gameStatus == GameStatus.PLAYING;
    }

    /**
     * 检查游戏是否已结束
     */
    public boolean isFinished() {
        return gameStatus == GameStatus.FINISHED;
    }

    /**
     * 开始游戏
     */
    public void start() {
        this.gameStatus = GameStatus.PLAYING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 结束游戏
     */
    public void end() {
        this.gameStatus = GameStatus.FINISHED;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * 下一回合
     */
    public void nextRound() {
        this.roundNumber++;
    }

    /**
     * 设置当前玩家
     */
    public void setCurrentPlayer(Integer playerPosition) {
        this.currentPlayer = playerPosition;
    }

    /**
     * 下一个玩家
     */
    public void nextPlayer() {
        if (currentPlayer != null) {
            this.currentPlayer = (currentPlayer % 4) + 1;
        }
    }

    /**
     * 获取游戏持续时间（分钟）
     */
    public long getDurationInMinutes() {
        if (startedAt == null) {
            return 0;
        }
        LocalDateTime end = endedAt != null ? endedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, end).toMinutes();
    }
}