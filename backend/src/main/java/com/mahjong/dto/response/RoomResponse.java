package com.mahjong.dto.response;

import com.mahjong.dto.response.RoomResponse.PlayerResponse;
import com.mahjong.dto.response.RoomResponse.GameConfigResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间信息响应DTO
 *
 * @author Mahjong Game Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    /**
     * 房间ID
     */
    private Long id;

    /**
     * 房间号（6位数字）
     */
    private String roomNumber;

    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 创建者ID
     */
    private String creatorId;

    /**
     * 创建者昵称
     */
    private String creatorNickname;

    /**
     * 房间状态
     */
    private String roomStatus;

    /**
     * 最大玩家数量
     */
    private Integer maxPlayers;

    /**
     * 当前玩家数量
     */
    private Integer currentPlayers;

    /**
     * 观战人数
     */
    private Integer spectatorCount;

    /**
     * 是否需要密码
     */
    private Boolean hasPassword;

    /**
     * 是否允许观战
     */
    private Boolean allowSpectate;

    /**
     * 是否公开房间
     */
    private Boolean isPublic;

    /**
     * 游戏配置
     */
    private GameConfigResponse gameConfig;

    /**
     * 玩家列表
     */
    private List<PlayerResponse> players;

    /**
     * 观战者列表
     */
    private List<PlayerResponse> spectators;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 是否已准备玩家数量
     */
    private Integer readyCount;

    /**
     * 用户在房间中的状态
     */
    private String userStatus;

    /**
     * 用户在房间中的位置
     */
    private Integer userPosition;

    /**
     * 玩家信息响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerResponse {

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
         * 位置（0-3）
         */
        private Integer position;

        /**
         * 总分
         */
        private Integer totalScore;

        /**
         * 是否为创建者
         */
        private Boolean isCreator;

        /**
         * 是否为观战者
         */
        private Boolean isSpectator;

        /**
         * 加入时间
         */
        private LocalDateTime joinedAt;

        /**
         * 最后活跃时间
         */
        private LocalDateTime lastActiveAt;
    }

    /**
     * 游戏配置响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameConfigResponse {

        /**
         * 配置ID
         */
        private Long configId;

        /**
         * 配置名称
         */
        private String configName;

        /**
         * 基础分
         */
        private Integer baseScore;

        /**
         * 最大回合数
         */
        private Integer maxRounds;

        /**
         * 是否允许碰
         */
        private Boolean allowPeng;

        /**
         * 是否允许杠
         */
        private Boolean allowGang;

        /**
         * 是否启用混牌
         */
        private Boolean mixedTileEnabled;

        /**
         * 思考时间（秒）
         */
        private Integer thinkTime;

        /**
         * 配置描述
         */
        private String description;

        /**
         * 是否为默认配置
         */
        private Boolean isDefault;

        /**
         * 是否为公共配置
         */
        private Boolean isPublic;

        /**
         * 使用次数
         */
        private Integer useCount;
    }

    /**
     * 从实体对象转换为响应DTO
     */
    public static RoomResponse fromEntity(com.mahjong.entity.Room room) {
        return fromEntity(room, null);
    }

    /**
     * 从实体对象转换为响应DTO（包含用户状态）
     */
    public static RoomResponse fromEntity(com.mahjong.entity.Room room, String userId) {
        if (room == null) {
            return null;
        }

        RoomResponseBuilder builder = RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomName(room.getRoomName())
                .creatorId(room.getCreatorId())
                .roomStatus(room.getRoomStatus().name())
                .maxPlayers(room.getMaxPlayers())
                .currentPlayers(room.getCurrentPlayers())
                .spectatorCount(room.getSpectatorCount())
                .hasPassword(room.getHasPassword())
                .allowSpectate(room.getAllowSpectate())
                .isPublic(room.getIsPublic())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .expiresAt(room.getExpiresAt());

        // 转换游戏配置
        if (room.getGameConfig() != null) {
            builder.gameConfig(GameConfigResponse.builder()
                    .configId(room.getGameConfig().getId())
                    .configName(room.getGameConfig().getConfigName())
                    .baseScore(room.getGameConfig().getBaseScore())
                    .maxRounds(room.getGameConfig().getMaxRounds())
                    .allowPeng(room.getGameConfig().getAllowPeng())
                    .allowGang(room.getGameConfig().getAllowGang())
                    .mixedTileEnabled(room.getGameConfig().getMixedTileEnabled())
                    .thinkTime(room.getGameConfig().getThinkTime())
                    .description(room.getGameConfig().getDescription())
                    .isDefault(room.getGameConfig().getIsDefault())
                    .isPublic(room.getGameConfig().getIsPublic())
                    .useCount(room.getGameConfig().getUseCount())
                    .build());
        }

        // 转换玩家列表
        if (room.getPlayers() != null) {
            builder.players(room.getPlayers().stream()
                    .filter(player -> !player.getSpectator())
                    .map(PlayerResponse::fromEntity)
                    .toList());

            builder.spectators(room.getPlayers().stream()
                    .filter(com.mahjong.entity.Player::getSpectator)
                    .map(PlayerResponse::fromEntity)
                    .toList());
        }

        // 计算准备人数
        long readyCount = room.getPlayers() != null ?
                room.getPlayers().stream()
                        .filter(player -> !player.getSpectator())
                        .filter(com.mahjong.entity.Player::isReady)
                        .count() : 0;
        builder.readyCount((int) readyCount);

        // 如果提供了用户ID，设置用户特定信息
        if (userId != null && room.getPlayers() != null) {
            room.getPlayers().stream()
                    .filter(player -> userId.equals(player.getId()))
                    .findFirst()
                    .ifPresent(userPlayer -> {
                        builder.userStatus(userPlayer.getPlayerStatus().name());
                        builder.userPosition(userPlayer.getPosition());
                        if (userPlayer.getPlayerName() != null) {
                            builder.creatorNickname(userPlayer.getPlayerName());
                        }
                    });
        }

        return builder.build();
    }
}