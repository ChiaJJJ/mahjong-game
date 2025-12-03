package com.mahjong.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 游戏配置实体类
 * 存储不同类型的游戏规则配置
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Entity
@Table(name = "game_configs", indexes = {
    @Index(name = "idx_config_name", columnList = "configName", unique = true),
    @Index(name = "idx_is_default", columnList = "isDefault"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameConfig {

    /**
     * 配置ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置名称（唯一标识）
     */
    @Column(name = "config_name", nullable = false, unique = true, length = 50)
    private String configName;

    /**
     * 配置描述
     */
    @Column(name = "config_description", length = 200)
    private String configDescription;

    /**
     * 基础分数
     */
    @Column(name = "base_score", nullable = false)
    private Integer baseScore = 1;

    /**
     * 最大回合数
     */
    @Column(name = "max_rounds", nullable = false)
    private Integer maxRounds = 8;

    /**
     * 玩家数量（2-4）
     */
    @Column(name = "player_count", nullable = false)
    private Integer playerCount = 4;

    /**
     * 是否启用混牌
     */
    @Column(name = "mixed_tile_enabled", nullable = false)
    private Boolean mixedTileEnabled = false;

    /**
     * 混牌数量
     */
    @Column(name = "mixed_tile_count")
    private Integer mixedTileCount;

    /**
     * 是否允许碰牌
     */
    @Column(name = "allow_peng", nullable = false)
    private Boolean allowPeng = true;

    /**
     * 是否允许杠牌
     */
    @Column(name = "allow_gang", nullable = false)
    private Boolean allowGang = true;

    /**
     * 是否允许吃牌
     */
    @Column(name = "allow_chi", nullable = false)
    private Boolean allowChi = true;

    /**
     * 是否允许七对
     */
    @Column(name = "allow_qidui", nullable = false)
    private Boolean allowQidui = false;

    /**
     * 是否允许十三幺
     */
    @Column(name = "allow_shisanyao", nullable = false)
    private Boolean allowShisanyao = false;

    /**
     * 是否允许清一色
     */
    @Column(name = "allow_qingyise", nullable = false)
    private Boolean allowQingyise = true;

    /**
     * 玩家思考时间（秒）
     */
    @Column(name = "think_time", nullable = false)
    private Integer thinkTime = 30;

    /**
     * 是否自动出牌超时
     */
    @Column(name = "auto_discard", nullable = false)
    private Boolean autoDiscard = true;

    /**
     * 是否允许观战
     */
    @Column(name = "allow_spectate", nullable = false)
    private Boolean allowSpectate = true;

    /**
     * 房间有效期（小时）
     */
    @Column(name = "room_expiry_hours")
    private Integer roomExpiryHours = 24;

    /**
     * 是否为默认配置
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * 是否启用
     */
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    /**
     * 创建者ID
     */
    @Column(name = "created_by", length = 36)
    private String createdBy;

    /**
     * 使用次数统计
     */
    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    /**
     * 额外配置（JSON格式）
     */
    @Column(name = "extra_config", columnDefinition = "JSON")
    private String extraConfig;

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
     * 关联的房间列表
     */
    @OneToMany(mappedBy = "gameConfig", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms;

    /**
     * 检查是否允许碰牌
     */
    public boolean canPeng() {
        return allowPeng != null && allowPeng;
    }

    /**
     * 检查是否允许杠牌
     */
    public boolean canGang() {
        return allowGang != null && allowGang;
    }

    /**
     * 检查是否允许吃牌
     */
    public boolean canChi() {
        return allowChi != null && allowChi;
    }

    /**
     * 检查是否启用混牌
     */
    public boolean hasMixedTiles() {
        return mixedTileEnabled != null && mixedTileEnabled;
    }

    /**
     * 检查是否允许七对
     */
    public boolean allowsQidui() {
        return allowQidui != null && allowQidui;
    }

    /**
     * 检查是否允许十三幺
     */
    public boolean allowsShisanyao() {
        return allowShisanyao != null && allowShisanyao;
    }

    /**
     * 检查是否允许清一色
     */
    public boolean allowsQingyise() {
        return allowQingyise != null && allowQingyise;
    }

    /**
     * 检查是否启用自动出牌
     */
    public boolean hasAutoDiscard() {
        return autoDiscard != null && autoDiscard;
    }

    /**
     * 检查是否允许观战
     */
    public boolean allowsSpectate() {
        return allowSpectate != null && allowSpectate;
    }

    /**
     * 检查是否为默认配置
     */
    public boolean isDefaultConfig() {
        return isDefault != null && isDefault;
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabledConfig() {
        return isEnabled != null && isEnabled;
    }

    /**
     * 增加使用次数
     */
    public void incrementUsage() {
        this.usageCount++;
    }

    /**
     * 重置使用次数
     */
    public void resetUsage() {
        this.usageCount = 0L;
    }

    /**
     * 获取实际玩家数量
     */
    public Integer getActualPlayerCount() {
        return playerCount != null ? Math.min(4, Math.max(2, playerCount)) : 4;
    }

    /**
     * 获取实际思考时间
     */
    public Integer getActualThinkTime() {
        return thinkTime != null ? Math.max(5, thinkTime) : 30;
    }

    /**
     * 获取实际基础分数
     */
    public Integer getActualBaseScore() {
        return baseScore != null ? Math.max(1, baseScore) : 1;
    }

    /**
     * 获取实际最大回合数
     */
    public Integer getActualMaxRounds() {
        return maxRounds != null ? Math.max(1, maxRounds) : 8;
    }

    /**
     * 验证配置有效性
     */
    public boolean isValid() {
        return configName != null && !configName.trim().isEmpty()
                && getActualPlayerCount() >= 2
                && getActualThinkTime() >= 5
                && getActualBaseScore() >= 1
                && getActualMaxRounds() >= 1;
    }
}