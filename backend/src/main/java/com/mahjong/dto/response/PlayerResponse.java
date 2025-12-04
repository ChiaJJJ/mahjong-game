package com.mahjong.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 玩家信息响应DTO
 *
 * @author Mahjong Game Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponse {

    /**
     * 玩家ID
     */
    private String id;

    /**
     * 玩家昵称
     */
    private String nickname;

    /**
     * 玩家头像URL
     */
    private String avatarUrl;

    /**
     * 玩家状态
     */
    private String status;

    /**
     * 是否准备
     */
    private Boolean isReady;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 位置（0-3，观战者为null）
     */
    private Integer position;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 游戏统计
     */
    private GameStatsResponse gameStats;

    /**
     * 当前所在房间号（如果在房间中）
     */
    private String currentRoomNumber;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveAt;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 游戏统计响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameStatsResponse {

        /**
         * 总游戏次数
         */
        private Integer totalGames;

        /**
         * 胜局次数
         */
        private Integer winGames;

        /**
         * 胜率
         */
        private Double winRate;

        /**
         * 最高分数
         */
        private Integer highScore;

        /**
         * 总胡牌次数
         */
        private Integer totalWins;

        /**
         * 总胡牌分数
         */
        private Integer totalWinScore;

        /**
         * 总杠牌次数
         */
        private Integer totalGangs;

        /**
         * 总碰牌次数
         */
        private Integer totalPengs;

        /**
         * 平均每局得分
         */
        private Double averageScore;

        /**
         * 等级
         */
        private Integer level;

        /**
         * 经验值
         */
        private Long experience;
    }

    /**
     * 从实体对象转换为响应DTO
     */
    public static PlayerResponse fromEntity(com.mahjong.entity.Player player) {
        if (player == null) {
            return null;
        }

        return PlayerResponse.builder()
                .id(player.getId())
                .nickname(player.getPlayerName())
                .avatarUrl(player.getPlayerAvatar())
                .status(player.getPlayerStatus().name())
                .isReady(player.isReady())
                .isOnline(player.isOnline())
                .position(player.getPlayerPosition())
                .totalScore(player.getTotalScore())
                .currentRoomNumber(player.getRoom() != null ? player.getRoom().getRoomNumber() : null)
                .createdAt(player.getCreatedAt())
                .lastActiveAt(player.getLastActiveAt())
                .build();
    }

    /**
     * 从实体对象转换为响应DTO（包含游戏统计）
     */
    public static PlayerResponse fromEntityWithStats(com.mahjong.entity.Player player) {
        PlayerResponse response = fromEntity(player);
        if (response != null && player.getGameStats() != null) {
            response.setGameStats(GameStatsResponse.builder()
                    .totalGames(player.getGameStats().getTotalGames())
                    .winGames(player.getGameStats().getWinGames())
                    .winRate(player.getGameStats().getWinRate())
                    .highScore(player.getGameStats().getHighScore())
                    .totalWins(player.getGameStats().getTotalWins())
                    .totalWinScore(player.getGameStats().getTotalWinScore())
                    .totalGangs(player.getGameStats().getTotalGangs())
                    .totalPengs(player.getGameStats().getTotalPengs())
                    .averageScore(player.getGameStats().getAverageScore())
                    .level(player.getGameStats().getLevel())
                    .experience(player.getGameStats().getExperience())
                    .build());
        }
        return response;
    }
}