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
                .createdAt(player.getJoinedAt())
                .lastActiveAt(player.getLastActiveAt())
                .build();
    }

    /**
     * 从实体对象转换为响应DTO（包含游戏统计）
     */
    public static PlayerResponse fromEntityWithStats(com.mahjong.entity.Player player) {
        PlayerResponse response = fromEntity(player);
        if (response != null) {
            // 使用Player实体的现有数据创建游戏统计
            int totalWins = player.getWinsCount() != null ? player.getWinsCount() : 0;
            int totalScore = player.getTotalScore() != null ? player.getTotalScore() : 0;
            double winRate = totalWins > 0 ? (double) totalWins / Math.max(1, totalWins) : 0.0;

            response.setGameStats(GameStatsResponse.builder()
                    .totalGames(Math.max(1, totalWins)) // 假设至少有一局游戏
                    .winGames(totalWins)
                    .winRate(winRate)
                    .highScore(totalScore) // 使用当前分数作为最高分
                    .totalWins(totalWins)
                    .totalWinScore(totalScore)
                    .totalGangs(0) // Player实体中没有这个数据
                    .totalPengs(0) // Player实体中没有这个数据
                    .averageScore(totalWins > 0 ? (double) totalScore / totalWins : 0.0)
                    .level(1) // Player实体中没有这个数据
                    .experience((long)(totalWins * 100)) // 简单的经验计算
                    .build());
        }
        return response;
    }
}