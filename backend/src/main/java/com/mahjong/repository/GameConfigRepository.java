package com.mahjong.repository;

import com.mahjong.entity.GameConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 游戏配置Repository接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Repository
public interface GameConfigRepository extends JpaRepository<GameConfig, Long> {

    /**
     * 根据配置名称查找配置
     */
    Optional<GameConfig> findByConfigName(String configName);

    /**
     * 查找所有启用混牌的配置
     */
    List<GameConfig> findByMixedTileEnabledTrue();

    /**
     * 查找所有允许碰牌的配置
     */
    List<GameConfig> findByAllowPengTrue();

    /**
     * 查找所有允许杠牌的配置
     */
    List<GameConfig> findByAllowGangTrue();

    /**
     * 查找指定基础分范围的配置
     */
    @Query("SELECT gc FROM GameConfig gc WHERE gc.baseScore BETWEEN :minScore AND :maxScore")
    List<GameConfig> findByBaseScoreRange(@Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore);

    /**
     * 查找指定最大局数的配置
     */
    List<GameConfig> findByMaxRounds(Integer maxRounds);

    /**
     * 查找思考时间在指定范围内的配置
     */
    @Query("SELECT gc FROM GameConfig gc WHERE gc.thinkTime BETWEEN :minTime AND :maxTime")
    List<GameConfig> findByThinkTimeRange(@Param("minTime") Integer minTime, @Param("maxTime") Integer maxTime);

    /**
     * 查找自动出牌的配置
     */
    List<GameConfig> findByAutoDiscardTrue();

    /**
     * 查找指定类型规则允许的配置
     */
    @Query("SELECT gc FROM GameConfig gc WHERE (:allowPeng IS NULL OR gc.allowPeng = :allowPeng) " +
           "AND (:allowGang IS NULL OR gc.allowGang = :allowGang) " +
           "AND (:allowChi IS NULL OR gc.allowChi = :allowChi)")
    List<GameConfig> findByGameRules(@Param("allowPeng") Boolean allowPeng,
                                     @Param("allowGang") Boolean allowGang,
                                     @Param("allowChi") Boolean allowChi);

    /**
     * 检查配置名称是否已存在
     */
    boolean existsByConfigName(String configName);

    /**
     * 查找最近创建的配置
     */
    List<GameConfig> findTop10ByOrderByCreatedAtDesc();

    /**
     * 统计配置数量
     */
    @Query("SELECT COUNT(gc) FROM GameConfig gc WHERE gc.mixedTileEnabled = true")
    long countMixedTileEnabledConfigs();

    /**
     * 查找热门配置（使用次数最多）
     */
    @Query(value = "SELECT gc.*, COUNT(r.id) as usage_count FROM game_configs gc " +
                   "LEFT JOIN rooms r ON JSON_CONTAINS(r.game_config, JSON_QUOTE(gc.config_name), '$.configName') " +
                   "GROUP BY gc.id ORDER BY usage_count DESC LIMIT 10", nativeQuery = true)
    List<GameConfig> findPopularConfigs();

    /**
     * 查找配置的数量统计
     */
    @Query("SELECT gc.configName, COUNT(r.id) as usageCount FROM GameConfig gc " +
           "LEFT JOIN Room r ON JSON_CONTAINS(r.gameConfig, gc.configName) " +
           "GROUP BY gc.configName")
    List<Object[]> getConfigUsageStats();
}