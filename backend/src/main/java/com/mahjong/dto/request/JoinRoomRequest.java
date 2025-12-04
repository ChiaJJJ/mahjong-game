package com.mahjong.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * 加入房间请求DTO
 *
 * @author Mahjong Game Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequest {

    /**
     * 玩家ID
     */
    @NotBlank(message = "玩家ID不能为空")
    @Size(max = 36, message = "玩家ID长度不能超过36个字符")
    private String playerId;

    /**
     * 玩家昵称
     */
    @NotBlank(message = "玩家昵称不能为空")
    @Size(min = 1, max = 20, message = "玩家昵称长度必须在1-20个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$",
             message = "玩家昵称只能包含中文、英文、数字和下划线")
    private String playerName;

    /**
     * 房间密码（如果有的话）
     */
    @Size(max = 20, message = "密码长度不能超过20个字符")
    private String password;

    /**
     * 是否观战
     */
    private Boolean asSpectator = false;

    /**
     * 用户头像URL（可选）
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;

    /**
     * 设备信息（用于推送）
     */
    @Size(max = 100, message = "设备信息长度不能超过100个字符")
    private String deviceInfo;
}