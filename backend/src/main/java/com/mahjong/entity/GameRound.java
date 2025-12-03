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
 * 游戏回合实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Entity
@Table(name = "game_rounds", indexes = {
    @Index(name = "idx_game_id", columnList = "gameId"),
    @Index(name = "idx_round_number", columnList = "roundNumber"),
    @Index(name = "idx_round_status", columnList = "roundStatus"),
    @Index(name = "idx_winner_position", columnList = "winnerPosition"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRound {

    /**
     * 回合ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属游戏
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_round_game"))
    private Game game;

    /**
     * 回合号
     */
    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    /**
     * 回合状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "round_status", nullable = false)
    private RoundStatus roundStatus = RoundStatus.WAITING;

    /**
     * 庄家位置
     */
    @Column(name = "dealer_position")
    private Integer dealerPosition;

    /**
     * 胜者位置
     */
    @Column(name = "winner_position")
    private Integer winnerPosition;

    /**
     * 胜利类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "win_type")
    private WinType winType;

    /**
     * 胡牌番数
     */
    @Column(name = "fan_count")
    private Integer fanCount;

    /**
     * 得分（JSON格式，记录各玩家得分）
     */
    @Column(name = "scores", columnDefinition = "JSON")
    private String scores;

    /**
     * 回合数据（JSON格式，包含详细的游戏过程）
     */
    @Column(name = "round_data", columnDefinition = "JSON")
    private String roundData;

    /**
     * 特殊牌型
     */
    @Column(name = "special_pattern", length = 50)
    private String specialPattern;

    /**
     * 回合开始时间
     */
    @Column(name = "started_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    /**
     * 回合结束时间
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
     * 回合状态枚举
     */
    public enum RoundStatus {
        /**
         * 等待中
         */
        WAITING("等待中"),

        /**
         * 进行中
         */
        PLAYING("进行中"),

        /**
         * 已结束
         */
        FINISHED("已结束");

        private final String description;

        RoundStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 胜利类型枚举
     */
    public enum WinType {
        /**
         * 自摸
         */
        ZIMO("自摸"),

        /**
         * 点炮
         */
        DIANPAO("点炮"),

        /**
         * 抢杠胡
         */
        QIANGGANG("抢杠胡"),

        /**
         * 杠上炮
         */
        GANGSHANGPAO("杠上炮"),

        /**
         * 杠上开花
         */
        GANGSHANGKAI("杠上开花"),

        /**
         * 海底捞月
         */
        HAIDILAOYUE("海底捞月"),

        /**
         * 妙手回春
         */
        MIAOSHOUHUICHUN("妙手回春"),

        /**
         * 流局
         */
        LIUJU("流局");

        private final String description;

        WinType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 关联的玩家回合记录
     */
    @OneToMany(mappedBy = "gameRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PlayerRound> playerRounds;

    /**
     * 检查回合是否正在进行
     */
    public boolean isPlaying() {
        return roundStatus == RoundStatus.PLAYING;
    }

    /**
     * 检查回合是否已结束
     */
    public boolean isFinished() {
        return roundStatus == RoundStatus.FINISHED;
    }

    /**
     * 开始回合
     */
    public void start() {
        this.roundStatus = RoundStatus.PLAYING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 结束回合
     */
    public void end() {
        this.roundStatus = RoundStatus.FINISHED;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * 设置胜利者
     */
    public void setWinner(Integer winnerPosition, WinType winType, Integer fanCount) {
        this.winnerPosition = winnerPosition;
        this.winType = winType;
        this.fanCount = fanCount;
    }

    /**
     * 设置流局
     */
    public void setDraw() {
        this.winType = WinType.LIUJU;
        this.winnerPosition = null;
        this.fanCount = 0;
    }

    /**
     * 获取回合持续时间（分钟）
     */
    public long getDurationInMinutes() {
        if (startedAt == null) {
            return 0;
        }
        LocalDateTime end = endedAt != null ? endedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, end).toMinutes();
    }

    /**
     * 检查是否为特殊胜利
     */
    public boolean isSpecialWin() {
        return winType != null && winType != WinType.ZIMO && winType != WinType.DIANPAO;
    }
}