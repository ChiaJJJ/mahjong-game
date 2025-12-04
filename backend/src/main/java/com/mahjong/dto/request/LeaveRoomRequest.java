package com.mahjong.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 离开房间请求DTO
 *
 * @author Mahjong Game Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRoomRequest {

    /**
     * 玩家ID
     */
    @NotBlank(message = "玩家ID不能为空")
    @Size(max = 36, message = "玩家ID长度不能超过36个字符")
    private String playerId;

    /**
     * 离开原因（可选）
     */
    @Size(max = 200, message = "离开原因不能超过200个字符")
    private String reason;

    /**
     * 是否切换到观战模式
     */
    private Boolean switchToSpectator = false;
}