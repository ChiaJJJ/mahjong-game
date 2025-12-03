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
 * 玩家回合实体类
 * 记录每个玩家在每个回合中的详细信息
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Entity
@Table(name = "player_rounds", indexes = {
    @Index(name = "idx_game_round_id", columnList = "gameRoundId"),
    @Index(name = "idx_player_id", columnList = "playerId"),
    @Index(name = "idx_player_position", columnList = "playerPosition"),
    @Index(name = "idx_is_winner", columnList = "isWinner")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRound {

    /**
     * 记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属游戏回合
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_round_id", nullable = false, foreignKey = @ForeignKey(name = "fk_player_round_game_round"))
    private GameRound gameRound;

    /**
     * 玩家ID
     */
    @Column(name = "player_id", nullable = false, length = 36)
    private String playerId;

    /**
     * 玩家昵称
     */
    @Column(name = "player_name", nullable = false, length = 20)
    private String playerName;

    /**
     * 玩家位置
     */
    @Column(name = "player_position", nullable = false)
    private Integer playerPosition;

    /**
     * 是否为胜者
     */
    @Column(name = "winner", nullable = false)
    private Boolean winner = false;

    /**
     * 是否为庄家
     */
    @Column(name = "dealer", nullable = false)
    private Boolean dealer = false;

    /**
     * 得分
     */
    @Column(name = "score", nullable = false)
    private Integer score = 0;

    /**
     * 番数
     */
    @Column(name = "fan_count")
    private Integer fanCount = 0;

    /**
     * 手牌（JSON格式，记录最终手牌）
     */
    @Column(name = "hand_tiles", columnDefinition = "JSON")
    private String handTiles;

    /**
     * 吃牌组合（JSON格式）
     */
    @Column(name = "chi_combinations", columnDefinition = "JSON")
    private String chiCombinations;

    /**
     * 碰牌组合（JSON格式）
     */
    @Column(name = "peng_combinations", columnDefinition = "JSON")
    private String pengCombinations;

    /**
     * 杠牌组合（JSON格式）
     */
    @Column(name = "gang_combinations", columnDefinition = "JSON")
    private String gangCombinations;

    /**
     * 特殊牌型
     */
    @Column(name = "special_pattern", length = 50)
    private String specialPattern;

    /**
     * 详细统计（JSON格式，包含各种操作次数）
     */
    @Column(name = "statistics", columnDefinition = "JSON")
    private String statistics;

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
     * 检查是否为胜者
     */
    public boolean isWinner() {
        return isWinner != null && isWinner;
    }

    /**
     * 检查是否为庄家
     */
    public boolean isDealer() {
        return isDealer != null && isDealer;
    }

    /**
     * 设置为胜者
     */
    public void setAsWinner(Integer fanCount) {
        this.isWinner = true;
        this.fanCount = fanCount;
    }

    /**
     * 设置为庄家
     */
    public void setAsDealer() {
        this.isDealer = true;
    }

    /**
     * 增加得分
     */
    public void addScore(int points) {
        this.score += points;
    }

    /**
     * 减少得分
     */
    public void subtractScore(int points) {
        this.score -= points;
    }

    /**
     * 获取正分
     */
    public int getPositiveScore() {
        return Math.max(0, score);
    }

    /**
     * 获取负分
     */
    public int getNegativeScore() {
        return Math.min(0, score);
    }

    /**
     * 初始化统计信息
     */
    public void initializeStatistics() {
        this.statistics = "{\"drawCount\":0,\"discardCount\":0,\"chiCount\":0,\"pengCount\":0,\"gangCount\":0,\"passCount\":0}";
    }
}