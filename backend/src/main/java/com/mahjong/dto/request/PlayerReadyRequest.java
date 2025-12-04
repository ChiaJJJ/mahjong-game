package com.mahjong.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 玩家准备请求DTO
 *
 * @author Mahjong Game Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerReadyRequest {

    /**
     * 玩家ID
     */
    @NotBlank(message = "玩家ID不能为空")
    @Size(max = 36, message = "玩家ID长度不能超过36个字符")
    private String playerId;

    /**
     * 是否准备
     */
    private Boolean isReady = true;
}