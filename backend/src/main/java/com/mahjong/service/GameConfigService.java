package com.mahjong.service;

import com.mahjong.entity.GameConfig;
import com.mahjong.repository.GameConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 游戏配置服务
 * 处理游戏配置的创建、更新、查询和管理
 *
 * @author Mahjong Game Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameConfigService {

    private final GameConfigRepository gameConfigRepository;

    /**
     * 创建默认游戏配置
     *
     * @param configName 配置名称
     * @param creatorId 创建者ID
     * @return 创建的游戏配置
     */
    @Transactional
    public GameConfig createDefaultConfig(String configName, String creatorId) {
        log.info("创建默认游戏配置: configName={}, creatorId={}", configName, creatorId);

        GameConfig config = GameConfig.builder()
                .configName(configName)
                .createdBy(creatorId)
                .baseScore(1)
                .maxRounds(8)
                .allowPeng(true)
                .allowGang(true)
                .mixedTileEnabled(true)
                .thinkTime(30)
                .boolDefault(false)
                .enabled(true)
                .usageCount(0L)
                .configDescription("默认河南麻将配置")
                .build();

        config = gameConfigRepository.save(config);
        log.info("默认游戏配置创建成功: configId={}", config.getId());
        return config;
    }

    /**
     * 获取配置详情
     *
     * @param configId 配置ID
     * @return 配置信息
     */
    @Transactional(readOnly = true)
    public Optional<GameConfig> getConfigById(Long configId) {
        return gameConfigRepository.findById(configId);
    }

    /**
     * 根据配置名称查找配置
     *
     * @param configName 配置名称
     * @return 配置列表
     */
    @Transactional(readOnly = true)
    public List<GameConfig> getConfigByName(String configName) {
        return gameConfigRepository.findByConfigNameContaining(configName);
    }

    /**
     * 获取用户创建的配置列表
     *
     * @param creatorId 创建者ID
     * @return 配置列表
     */
    @Transactional(readOnly = true)
    public List<GameConfig> getConfigsByCreator(String creatorId) {
        return gameConfigRepository.findByCreatedBy(creatorId);
    }

    /**
     * 获取公共配置列表（按使用次数排序）
     *
     * @param limit 限制数量
     * @return 配置列表
     */
    @Transactional(readOnly = true)
    public List<GameConfig> getPublicConfigs(int limit) {
        return gameConfigRepository.findByEnabledOrderByUsageCountDesc(true);
    }

    /**
     * 获取所有活跃配置
     *
     * @return 配置列表
     */
    @Transactional(readOnly = true)
    public List<GameConfig> getActiveConfigs() {
        return gameConfigRepository.findByEnabledOrderByCreatedAtDesc(true);
    }

    /**
     * 更新游戏配置
     *
     * @param configId 配置ID
     * @param creatorId 创建者ID（用于权限验证）
     * @param config 新的配置信息
     * @return 更新后的配置
     * @throws IllegalArgumentException 当配置不存在或权限不足时
     */
    @Transactional
    public GameConfig updateConfig(Long configId, String creatorId, GameConfig config) {
        log.info("更新游戏配置: configId={}, creatorId={}", configId, creatorId);

        GameConfig existingConfig = gameConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("配置不存在: " + configId));

        // 验证权限：只有创建者可以修改
        if (!existingConfig.getCreatedBy().equals(creatorId)) {
            throw new IllegalArgumentException("无权限修改此配置");
        }

        // 更新配置信息
        existingConfig.setConfigName(config.getConfigName());
        existingConfig.setConfigDescription(config.getConfigDescription());
        existingConfig.setBaseScore(config.getBaseScore());
        existingConfig.setMaxRounds(config.getMaxRounds());
        existingConfig.setAllowPeng(config.getAllowPeng());
        existingConfig.setAllowGang(config.getAllowGang());
        existingConfig.setMixedTileEnabled(config.getMixedTileEnabled());
        existingConfig.setThinkTime(config.getThinkTime());
        existingConfig.setAllowSpectate(config.getAllowSpectate());

        GameConfig updatedConfig = gameConfigRepository.save(existingConfig);
        log.info("游戏配置更新成功: configId={}", updatedConfig.getId());
        return updatedConfig;
    }

    /**
     * 删除游戏配置（软删除）
     *
     * @param configId 配置ID
     * @param creatorId 创建者ID（用于权限验证）
     * @throws IllegalArgumentException 当配置不存在或权限不足时
     */
    @Transactional
    public void deleteConfig(Long configId, String creatorId) {
        log.info("删除游戏配置: configId={}, creatorId={}", configId, creatorId);

        GameConfig config = gameConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("配置不存在: " + configId));

        // 验证权限：只有创建者可以删除
        if (!config.getCreatedBy().equals(creatorId)) {
            throw new IllegalArgumentException("无权限删除此配置");
        }

        // 软删除：设置为非活跃状态
        config.setEnabled(false);
        config.setUpdatedAt(LocalDateTime.now());
        gameConfigRepository.save(config);

        log.info("游戏配置删除成功: configId={}", configId);
    }

    /**
     * 增加配置使用次数
     *
     * @param configId 配置ID
     */
    @Transactional
    public void incrementUseCount(Long configId) {
        Optional<GameConfig> configOpt = gameConfigRepository.findById(configId);
        if (configOpt.isPresent()) {
            GameConfig config = configOpt.get();
            config.setUsageCount(config.getUsageCount() + 1);
            config.setUpdatedAt(LocalDateTime.now());
            gameConfigRepository.save(config);
            log.debug("配置使用次数增加: configId={}, useCount={}", configId, config.getUsageCount());
        }
    }

    /**
     * 复制配置
     *
     * @param configId 原配置ID
     * @param newCreatorId 新创建者ID
     * @param newConfigName 新配置名称
     * @return 复制的配置
     */
    @Transactional
    public GameConfig copyConfig(Long configId, String newCreatorId, String newConfigName) {
        log.info("复制游戏配置: configId={}, newCreatorId={}, newConfigName={}",
                configId, newCreatorId, newConfigName);

        GameConfig originalConfig = gameConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("原配置不存在: " + configId));

        GameConfig copiedConfig = GameConfig.builder()
                .configName(newConfigName)
                .configDescription(originalConfig.getConfigDescription() + " (副本)")
                .createdBy(newCreatorId)
                .baseScore(originalConfig.getBaseScore())
                .maxRounds(originalConfig.getMaxRounds())
                .allowPeng(originalConfig.getAllowPeng())
                .allowGang(originalConfig.getAllowGang())
                .mixedTileEnabled(originalConfig.getMixedTileEnabled())
                .thinkTime(originalConfig.getThinkTime())
                .boolDefault(false)
                .enabled(true)
                .usageCount(0L)
                .allowSpectate(false)
                .build();

        copiedConfig = gameConfigRepository.save(copiedConfig);
        log.info("游戏配置复制成功: originalConfigId={}, newConfigId={}", configId, copiedConfig.getId());
        return copiedConfig;
    }

    /**
     * 验证配置参数
     *
     * @param config 配置对象
     * @throws IllegalArgumentException 当参数无效时
     */
    public void validateConfig(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置不能为空");
        }

        if (config.getBaseScore() == null || config.getBaseScore() < 1) {
            throw new IllegalArgumentException("基础分必须大于0");
        }

        if (config.getMaxRounds() == null || config.getMaxRounds() < 1 || config.getMaxRounds() > 16) {
            throw new IllegalArgumentException("最大回合数必须在1-16之间");
        }

        if (config.getThinkTime() == null || config.getThinkTime() < 5 || config.getThinkTime() > 120) {
            throw new IllegalArgumentException("思考时间必须在5-120秒之间");
        }

        if (config.getConfigName() == null || config.getConfigName().trim().isEmpty()) {
            throw new IllegalArgumentException("配置名称不能为空");
        }

        if (config.getConfigName().length() > 50) {
            throw new IllegalArgumentException("配置名称不能超过50个字符");
        }

        if (config.getConfigDescription() != null && config.getConfigDescription().length() > 200) {
            throw new IllegalArgumentException("描述不能超过200个字符");
        }
    }

    /**
     * 获取配置统计信息
     *
     * @return 统计信息
     */
    @Transactional(readOnly = true)
    public ConfigStats getConfigStats() {
        long totalConfigs = gameConfigRepository.count();
        long activeConfigs = gameConfigRepository.countByEnabled(true);
        long publicConfigs = gameConfigRepository.countByEnabledAndAllowSpectate(true, true);

        return ConfigStats.builder()
                .totalConfigs(totalConfigs)
                .activeConfigs(activeConfigs)
                .publicConfigs(publicConfigs)
                .build();
    }

    /**
     * 配置统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class ConfigStats {
        private Long totalConfigs;
        private Long activeConfigs;
        private Long publicConfigs;
    }
}