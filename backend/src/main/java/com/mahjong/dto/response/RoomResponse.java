package com.mahjong.dto.response;

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
     * 房间是否需要密码
     */
    private Boolean hasPassword;

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
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 简化的静态工厂方法 - 单参数版本
     */
    public static RoomResponse fromEntity(com.mahjong.entity.Room room) {
        if (room == null) {
            return null;
        }

        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomName(room.getRoomName())
                .hasPassword(room.getPassword() != null && !room.getPassword().trim().isEmpty())
                .creatorId(room.getCreatorId())
                .creatorNickname("")
                .roomStatus(room.getRoomStatus().name())
                .maxPlayers(room.getMaxPlayers())
                .currentPlayers(room.getCurrentPlayers())
                .spectatorCount(room.getSpectatorCount())
                .allowSpectate(room.getAllowSpectate())
                .isPublic(true)
                .gameConfig(null)
                .players(null)
                .spectators(null)
                .createdAt(room.getCreatedAt())
                .expiresAt(room.getExpiresAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    /**
     * 带用户ID的静态工厂方法 - 双参数版本
     */
    public static RoomResponse fromEntity(com.mahjong.entity.Room room, String userId) {
        RoomResponse response = fromEntity(room);
        return response;
    }

    /**
     * 玩家信息响应DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerResponse {
        private String id;
        private String playerName;
        private String nickname;
        private String avatarUrl;
        private Integer position;
        private String status;
        private Boolean isSpectator;
        private Boolean isOnline;
        private Boolean isReady;
        private Long totalScore;
        private Integer winCount;
        private LocalDateTime lastActiveAt;
    }

    /**
     * 游戏配置响应DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameConfigResponse {
        private Long id;
        private String configName;
        private Integer baseScore;
        private Integer maxRounds;
        private Boolean allowPeng;
        private Boolean allowGang;
        private Boolean mixedTileEnabled;
        private Integer thinkTime;
        private String description;
        private Boolean isDefault;
        private Boolean isPublic;
        private Integer useCount;
    }
}