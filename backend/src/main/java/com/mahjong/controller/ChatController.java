package com.mahjong.controller;

import com.mahjong.service.ChatService;
import com.mahjong.service.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 聊天控制器
 * 处理游戏内聊天相关的HTTP请求
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "聊天管理", description = "游戏内聊天功能相关接口")
public class ChatController {

    private final ChatService chatService;

    /**
     * 发送聊天消息
     */
    @PostMapping("/message")
    @Operation(summary = "发送聊天消息", description = "向指定房间发送聊天消息")
    public ResponseEntity<ApiResponse<String>> sendMessage(
            @Parameter(description = "用户ID", required = true)
            @RequestParam @NotNull Long userId,

            @Parameter(description = "房间号", required = true)
            @RequestParam @NotBlank String roomId,

            @Parameter(description = "消息内容", required = true)
            @RequestParam @NotBlank @Size(max = 100, message = "消息长度不能超过100个字符") String content) {

        log.info("HTTP请求发送聊天消息: 用户={}, 房间={}, 内容={}", userId, roomId, content);

        ApiResponse<?> response = chatService.sendChatMessage(userId, roomId, content);
        // 转换为String类型的ApiResponse
        ApiResponse<String> stringResponse = response.isSuccess()
            ? ApiResponse.success(response.getMessage(), "消息发送成功")
            : ApiResponse.error(response.getMessage());

        return ResponseEntity.status(stringResponse.getCode() == 200 ? 200 : 400)
                .body(stringResponse);
    }

    /**
     * 发送系统消息
     */
    @PostMapping("/system/message")
    @Operation(summary = "发送系统消息", description = "向指定房间发送系统消息")
    public ResponseEntity<ApiResponse<String>> sendSystemMessage(
            @Parameter(description = "房间号", required = true)
            @RequestParam @NotBlank String roomId,

            @Parameter(description = "消息内容", required = true)
            @RequestParam @NotBlank @Size(max = 200, message = "消息长度不能超过200个字符") String content) {

        log.info("HTTP请求发送系统消息: 房间={}, 内容={}", roomId, content);

        ApiResponse<?> response = chatService.sendSystemMessage(roomId, content);
        // 转换为String类型的ApiResponse
        ApiResponse<String> stringResponse = response.isSuccess()
            ? ApiResponse.success(response.getMessage(), "系统消息发送成功")
            : ApiResponse.error(response.getMessage());

        return ResponseEntity.status(stringResponse.getCode() == 200 ? 200 : 400)
                .body(stringResponse);
    }

    /**
     * 广播系统消息
     */
    @PostMapping("/system/broadcast")
    @Operation(summary = "广播系统消息", description = "向所有在线用户广播系统消息")
    public ResponseEntity<ApiResponse<String>> broadcastSystemMessage(
            @Parameter(description = "消息内容", required = true)
            @RequestParam @NotBlank @Size(max = 200, message = "消息长度不能超过200个字符") String content) {

        log.info("HTTP请求广播系统消息: 内容={}", content);

        ApiResponse<?> response = chatService.broadcastSystemMessage(content);
        // 转换为String类型的ApiResponse
        ApiResponse<String> stringResponse = response.isSuccess()
            ? ApiResponse.success(response.getMessage(), "广播消息发送成功")
            : ApiResponse.error(response.getMessage());

        return ResponseEntity.status(stringResponse.getCode() == 200 ? 200 : 400)
                .body(stringResponse);
    }
}