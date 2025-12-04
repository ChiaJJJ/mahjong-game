package com.mahjong.dto.request;

import com.mahjong.dto.request.CreateRoomRequest.GameConfigRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * 创建房间请求DTO
 *
 * @author Mahjong Game Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    /**
     * 房间名称
     */
    @NotBlank(message = "房间名称不能为空")
    @Size(min = 1, max = 50, message = "房间名称长度必须在1-50个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\s]+$",
             message = "房间名称只能包含中文、英文、数字、下划线和空格")
    private String roomName;

    /**
     * 创建者ID
     */
    @NotBlank(message = "创建者ID不能为空")
    @Size(max = 36, message = "创建者ID长度不能超过36个字符")
    private String creatorId;

    /**
     * 创建者昵称
     */
    @NotBlank(message = "创建者昵称不能为空")
    @Size(min = 1, max = 20, message = "创建者昵称长度必须在1-20个字符之间")
    private String creatorNickname;

    /**
     * 房间密码（可选）
     */
    @Size(max = 20, message = "密码长度不能超过20个字符")
    private String password;

    /**
     * 最大玩家数量
     */
    @Min(value = 2, message = "最小玩家数量为2")
    @Max(value = 4, message = "最大玩家数量为4")
    private Integer maxPlayers = 4;

    /**
     * 是否允许观战
     */
    private Boolean allowSpectate = true;

    /**
     * 是否公开房间（在房间列表中显示）
     */
    private Boolean isPublic = true;

    /**
     * 游戏配置
     */
    @Valid
    private GameConfigRequest gameConfig;

    /**
     * 游戏配置请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameConfigRequest {

        /**
         * 配置ID（如果使用已有配置）
         */
        private Long configId;

        /**
         * 基础分
         */
        @Min(value = 1, message = "基础分必须大于0")
        @Max(value = 100, message = "基础分不能超过100")
        private Integer baseScore = 1;

        /**
         * 最大回合数
         */
        @Min(value = 1, message = "最大回合数必须大于0")
        @Max(value = 16, message = "最大回合数不能超过16")
        private Integer maxRounds = 8;

        /**
         * 是否允许碰
         */
        private Boolean allowPeng = true;

        /**
         * 是否允许杠
         */
        private Boolean allowGang = true;

        /**
         * 是否启用混牌（赖子）
         */
        private Boolean mixedTileEnabled = true;

        /**
         * 思考时间（秒）
         */
        @Min(value = 5, message = "思考时间不能少于5秒")
        @Max(value = 120, message = "思考时间不能超过120秒")
        private Integer thinkTime = 30;

        /**
         * 配置描述
         */
        @Size(max = 200, message = "配置描述不能超过200个字符")
        private String description;
    }
}