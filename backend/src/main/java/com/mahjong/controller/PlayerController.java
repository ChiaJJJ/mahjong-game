package com.mahjong.controller;

import com.mahjong.dto.response.PlayerResponse;
import com.mahjong.entity.Player;
import com.mahjong.service.PlayerService;
import com.mahjong.service.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 玩家管理控制器
 * 提供玩家信息查询、状态管理、统计等API接口
 *
 * @author Mahjong Game Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/players")
@Validated
@RequiredArgsConstructor
@Slf4j
public class PlayerController {

    private final PlayerService playerService;

    /**
     * 获取玩家信息
     *
     * @param playerId 玩家ID
     * @return 玩家信息
     */
    @GetMapping("/{playerId}")
    public ResponseEntity<ApiResponse<PlayerResponse>> getPlayerInfo(
            @PathVariable @NotBlank(message = "玩家ID不能为空") String playerId) {

        log.info("获取玩家信息请求: playerId={}", playerId);

        try {
            // 获取玩家信息
            Player player = playerService.getPlayerById(playerId);
            if (player == null) {
                return ResponseEntity.notFound()
                        .build();
            }

            // 转换为响应DTO（包含游戏统计）
            PlayerResponse response = PlayerResponse.fromEntityWithStats(player);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("获取玩家信息失败 - 系统错误: playerId={}", playerId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 创建或更新玩家信息
     *
     * @param playerId 玩家ID
     * @param nickname 玩家昵称
     * @param avatarUrl 头像URL
     * @param deviceInfo 设备信息
     * @return 玩家信息
     */
    @PostMapping("/{playerId}")
    public ResponseEntity<ApiResponse<PlayerResponse>> createOrUpdatePlayer(
            @PathVariable @NotBlank(message = "玩家ID不能为空") String playerId,
            @RequestParam @NotBlank(message = "玩家昵称不能为空")
            @Size(min = 1, max = 20, message = "玩家昵称长度必须在1-20个字符之间") String nickname,
            @RequestParam(required = false)
            @Size(max = 500, message = "头像URL长度不能超过500个字符") String avatarUrl,
            @RequestParam(required = false)
            @Size(max = 100, message = "设备信息长度不能超过100个字符") String deviceInfo) {

        log.info("创建或更新玩家信息请求: playerId={}, nickname={}", playerId, nickname);

        try {
            // 创建或更新玩家
            Player player = playerService.createOrUpdatePlayer(
                    playerId,
                    nickname,
                    avatarUrl,
                    deviceInfo
            );

            // 转换为响应DTO
            PlayerResponse response = PlayerResponse.fromEntity(player);

            log.info("玩家信息创建/更新成功: playerId={}", playerId);
            return ResponseEntity.ok(ApiResponse.success("操作成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("创建或更新玩家信息失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("创建或更新玩家信息失败 - 系统错误: playerId={}", playerId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 更新玩家在线状态
     *
     * @param playerId 玩家ID
     * @param isOnline 是否在线
     * @param deviceInfo 设备信息
     * @return 操作结果
     */
    @PutMapping("/{playerId}/status")
    public ResponseEntity<ApiResponse<Void>> updatePlayerStatus(
            @PathVariable @NotBlank(message = "玩家ID不能为空") String playerId,
            @RequestParam Boolean isOnline,
            @RequestParam(required = false) String deviceInfo) {

        log.info("更新玩家状态请求: playerId={}, isOnline={}", playerId, isOnline);

        try {
            // 更新玩家状态
            playerService.updatePlayerStatus(playerId, isOnline, deviceInfo);

            log.info("玩家状态更新成功: playerId={}, isOnline={}", playerId, isOnline);
            return ResponseEntity.ok(ApiResponse.success("状态更新成功"));

        } catch (IllegalArgumentException e) {
            log.warn("更新玩家状态失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("更新玩家状态失败 - 系统错误: playerId={}", playerId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 更新玩家昵称
     *
     * @param playerId 玩家ID
     * @param nickname 新昵称
     * @return 操作结果
     */
    @PutMapping("/{playerId}/nickname")
    public ResponseEntity<ApiResponse<PlayerResponse>> updatePlayerNickname(
            @PathVariable @NotBlank(message = "玩家ID不能为空") String playerId,
            @RequestParam @NotBlank(message = "玩家昵称不能为空")
            @Size(min = 1, max = 20, message = "玩家昵称长度必须在1-20个字符之间") String nickname) {

        log.info("更新玩家昵称请求: playerId={}, nickname={}", playerId, nickname);

        try {
            // 更新玩家昵称
            Player player = playerService.updatePlayerNickname(playerId, nickname);

            // 转换为响应DTO
            PlayerResponse response = PlayerResponse.fromEntity(player);

            log.info("玩家昵称更新成功: playerId={}, nickname={}", playerId, nickname);
            return ResponseEntity.ok(ApiResponse.success("昵称更新成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("更新玩家昵称失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("更新玩家昵称失败 - 系统错误: playerId={}", playerId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 更新玩家头像
     *
     * @param playerId 玩家ID
     * @param avatarUrl 新头像URL
     * @return 操作结果
     */
    @PutMapping("/{playerId}/avatar")
    public ResponseEntity<ApiResponse<PlayerResponse>> updatePlayerAvatar(
            @PathVariable @NotBlank(message = "玩家ID不能为空") String playerId,
            @RequestParam @Size(max = 500, message = "头像URL长度不能超过500个字符") String avatarUrl) {

        log.info("更新玩家头像请求: playerId={}", playerId);

        try {
            // 更新玩家头像
            Player player = playerService.updatePlayerAvatar(playerId, avatarUrl);

            // 转换为响应DTO
            PlayerResponse response = PlayerResponse.fromEntity(player);

            log.info("玩家头像更新成功: playerId={}", playerId);
            return ResponseEntity.ok(ApiResponse.success("头像更新成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("更新玩家头像失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("更新玩家头像失败 - 系统错误: playerId={}", playerId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 更新玩家分数
     *
     * @param playerId 玩家ID
     * @param scoreDelta 分数变化量
     * @return 操作结果
     */
    @PutMapping("/{playerId}/score")
    public ResponseEntity<ApiResponse<PlayerResponse>> updatePlayerScore(
            @PathVariable @NotBlank(message = "玩家ID不能为空") String playerId,
            @RequestParam Integer scoreDelta) {

        log.info("更新玩家分数请求: playerId={}, scoreDelta={}", playerId, scoreDelta);

        try {
            // 更新玩家分数
            Player player = playerService.updatePlayerScore(playerId, scoreDelta);

            // 转换为响应DTO
            PlayerResponse response = PlayerResponse.fromEntity(player);

            log.info("玩家分数更新成功: playerId={}, newScore={}", playerId, player.getTotalScore());
            return ResponseEntity.ok(ApiResponse.success("分数更新成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("更新玩家分数失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("更新玩家分数失败 - 系统错误: playerId={}", playerId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 获取玩家游戏统计
     *
     * @param playerId 玩家ID
     * @return 游戏统计信息
     */
    @GetMapping("/{playerId}/stats")
    public ResponseEntity<ApiResponse<PlayerResponse.GameStatsResponse>> getPlayerStats(
            @PathVariable @NotBlank(message = "玩家ID不能为空") String playerId) {

        log.info("获取玩家游戏统计请求: playerId={}", playerId);

        try {
            // 获取玩家游戏统计
            Player player = playerService.getPlayerById(playerId);
            if (player == null) {
                return ResponseEntity.notFound()
                        .build();
            }

            // 转换统计信息为响应DTO
            PlayerResponse.GameStatsResponse statsResponse = null;
            if (player.getGameStats() != null) {
                statsResponse = PlayerResponse.GameStatsResponse.builder()
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
                        .build();
            }

            return ResponseEntity.ok(ApiResponse.success(statsResponse));

        } catch (Exception e) {
            log.error("获取玩家游戏统计失败 - 系统错误: playerId={}", playerId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 获取在线玩家列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 在线玩家列表
     */
    @GetMapping("/online")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOnlinePlayers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("获取在线玩家列表请求: page={}, size={}", page, size);

        try {
            // 获取在线玩家列表
            List<Player> onlinePlayers = playerService.getOnlinePlayers(page, size);

            // 转换为响应DTO
            List<PlayerResponse> playerResponses = onlinePlayers.stream()
                    .map(PlayerResponse::fromEntity)
                    .collect(Collectors.toList());

            // 构建分页响应
            Map<String, Object> response = Map.of(
                    "players", playerResponses,
                    "page", page,
                    "size", size,
                    "total", playerResponses.size()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("获取在线玩家列表失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 搜索玩家
     *
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchPlayers(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("搜索玩家请求: keyword={}, page={}, size={}", keyword, page, size);

        try {
            // 搜索玩家
            List<Player> players = playerService.searchPlayers(keyword, page, size);

            // 转换为响应DTO
            List<PlayerResponse> playerResponses = players.stream()
                    .map(PlayerResponse::fromEntity)
                    .collect(Collectors.toList());

            // 构建分页响应
            Map<String, Object> response = Map.of(
                    "players", playerResponses,
                    "page", page,
                    "size", size,
                    "total", playerResponses.size(),
                    "keyword", keyword
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("搜索玩家失败 - 系统错误: keyword={}", keyword, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 清理离线玩家
     *
     * @param minutes 离线时间（分钟）
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> cleanupOfflinePlayers(
            @RequestParam(defaultValue = "30") Integer minutes) {

        log.info("清理离线玩家请求: minutes={}", minutes);

        try {
            // 清理离线玩家
            int cleanedCount = playerService.cleanupOfflinePlayers(minutes);

            Map<String, Integer> response = Map.of("cleanedCount", cleanedCount);

            log.info("离线玩家清理完成: cleanedCount={}", cleanedCount);
            return ResponseEntity.ok(ApiResponse.success("清理完成", response));

        } catch (Exception e) {
            log.error("清理离线玩家失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 获取玩家统计概览
     *
     * @return 统计概览
     */
    @GetMapping("/stats/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlayerStatsOverview() {

        log.info("获取玩家统计概览请求");

        try {
            // 获取玩家统计概览
            Map<String, Object> overview = playerService.getPlayerStatsOverview();

            return ResponseEntity.ok(ApiResponse.success(overview));

        } catch (Exception e) {
            log.error("获取玩家统计概览失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }
}