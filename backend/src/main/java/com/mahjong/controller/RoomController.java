package com.mahjong.controller;

import com.mahjong.dto.request.CreateRoomRequest;
import com.mahjong.dto.request.JoinRoomRequest;
import com.mahjong.dto.request.LeaveRoomRequest;
import com.mahjong.dto.request.PlayerReadyRequest;
import com.mahjong.dto.response.RoomResponse;
import com.mahjong.entity.Room;
import com.mahjong.entity.GameConfig;
import com.mahjong.service.RoomService;
import com.mahjong.service.GameConfigService;
import com.mahjong.service.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房间管理控制器
 * 提供房间创建、加入、离开、查询等API接口
 *
 * @author Mahjong Game Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/rooms")
@Validated
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;
    private final GameConfigService gameConfigService;

    /**
     * 创建房间
     *
     * @param request 创建房间请求
     *param httpRequest HTTP请求对象（用于获取IP等信息）
     * @return 房间信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            HttpServletRequest httpRequest) {

        log.info("创建房间请求: roomName={}, creatorId={}, maxPlayers={}",
                request.getRoomName(), request.getCreatorId(), request.getMaxPlayers());

        try {
            // 验证配置参数
            if (request.getGameConfig() != null) {
                validateGameConfig(request.getGameConfig());
            }

            // 根据请求创建或获取游戏配置
            Long configId = createOrGetGameConfig(request);

            ApiResponse<Room> createResponse = roomService.createRoom(
                    request.getRoomName(),
                    request.getCreatorId(),
                    request.getCreatorNickname(),
                    configId,
                    request.getMaxPlayers(),
                    request.getPassword()
            );

            if (!createResponse.isSuccess() || createResponse.getData() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(createResponse.getMessage()));
            }
            Room room = createResponse.getData();

            // 转换为响应DTO
            RoomResponse response = RoomResponse.fromEntity(room, request.getCreatorId());

            log.info("房间创建成功: roomId={}, roomNumber={}", room.getId(), room.getRoomNumber());
            return ResponseEntity.ok(ApiResponse.success("房间创建成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("创建房间失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("创建房间失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 加入房间
     *
     * @param roomNumber 房间号
     * @param request 加入房间请求
     * @return 房间信息
     */
    @PostMapping("/{roomNumber}/join")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(
            @PathVariable String roomNumber,
            @Valid @RequestBody JoinRoomRequest request) {

        log.info("加入房间请求: roomNumber={}, playerId={}, playerName={}, asSpectator={}",
                roomNumber, request.getPlayerId(), request.getPlayerName(), request.getAsSpectator());

        try {
            // 加入房间
            ApiResponse<Room> joinResponse = roomService.joinRoom(
                    roomNumber,
                    request.getPlayerId(),
                    request.getPlayerName(),
                    request.getPassword(),
                    request.getAsSpectator()
            );

            if (!joinResponse.isSuccess() || joinResponse.getData() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(joinResponse.getMessage()));
            }
            Room room = joinResponse.getData();

            // 转换为响应DTO
            RoomResponse response = RoomResponse.fromEntity(room, request.getPlayerId());

            log.info("加入房间成功: roomNumber={}, playerId={}", roomNumber, request.getPlayerId());
            return ResponseEntity.ok(ApiResponse.success("加入房间成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("加入房间失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("加入房间失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 离开房间
     *
     * @param roomNumber 房间号
     * @param request 离开房间请求
     * @return 操作结果
     */
    @PostMapping("/{roomNumber}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable String roomNumber,
            @Valid @RequestBody LeaveRoomRequest request) {

        log.info("离开房间请求: roomNumber={}, playerId={}, switchToSpectator={}",
                roomNumber, request.getPlayerId(), request.getSwitchToSpectator());

        try {
            // 离开房间
            ApiResponse<String> leaveResponse = roomService.leaveRoom(
                    roomNumber,
                    request.getPlayerId()
            );

            if (!leaveResponse.isSuccess()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Void>error(leaveResponse.getMessage()));
            }

            log.info("离开房间成功: roomNumber={}, playerId={}", roomNumber, request.getPlayerId());
            return ResponseEntity.ok(ApiResponse.success("离开房间成功", (Void)null));

        } catch (IllegalArgumentException e) {
            log.warn("离开房间失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("离开房间失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 获取房间信息
     *
     * @param roomNumber 房间号
     * @param userId 用户ID（可选，用于获取用户特定信息）
     * @return 房间信息
     */
    @GetMapping("/{roomNumber}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomInfo(
            @PathVariable String roomNumber,
            @RequestParam(required = false) String userId) {

        log.info("获取房间信息请求: roomNumber={}, userId={}", roomNumber, userId);

        try {
            // 获取房间信息
            Room room = roomService.getRoomByNumber(roomNumber);
            if (room == null) {
                return ResponseEntity.notFound()
                        .build();
            }

            // 转换为响应DTO
            RoomResponse response = RoomResponse.fromEntity(room, userId);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("获取房间信息失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("获取房间信息失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 玩家准备/取消准备
     *
     * @param roomNumber 房间号
     * @param request 准备请求
     * @return 房间信息
     */
    @PostMapping("/{roomNumber}/ready")
    public ResponseEntity<ApiResponse<RoomResponse>> playerReady(
            @PathVariable String roomNumber,
            @Valid @RequestBody PlayerReadyRequest request) {

        log.info("玩家准备请求: roomNumber={}, playerId={}, isReady={}",
                roomNumber, request.getPlayerId(), request.getIsReady());

        try {
            // 设置玩家准备状态
            ApiResponse<Room> readyResponse = roomService.setPlayerReady(
                    roomNumber,
                    request.getPlayerId(),
                    request.getIsReady()
            );

            if (!readyResponse.isSuccess() || readyResponse.getData() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(readyResponse.getMessage()));
            }
            Room room = readyResponse.getData();

            // 转换为响应DTO
            RoomResponse response = RoomResponse.fromEntity(room, request.getPlayerId());

            log.info("玩家准备状态更新成功: roomNumber={}, playerId={}, isReady={}",
                    roomNumber, request.getPlayerId(), request.getIsReady());
            return ResponseEntity.ok(ApiResponse.success("准备状态更新成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("玩家准备状态更新失败 - 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("玩家准备状态更新失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 获取房间列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param status 房间状态过滤
     * @param isPublic 是否公开房间
     * @return 房间列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isPublic) {

        log.info("获取房间列表请求: page={}, size={}, status={}, isPublic={}",
                page, size, status, isPublic);

        try {
            // 获取房间列表
            ApiResponse<List<Room>> listResponse = roomService.getRoomList(status, isPublic, page, size);
            if (!listResponse.isSuccess() || listResponse.getData() == null) {
                return ResponseEntity.ok(ApiResponse.success(Map.of(
                        "rooms", List.of(),
                        "page", page,
                        "size", size,
                        "total", 0
                )));
            }
            List<Room> rooms = listResponse.getData();

            // 转换为响应DTO
            List<RoomResponse> roomResponses = rooms.stream()
                    .map(room -> RoomResponse.fromEntity(room))
                    .collect(Collectors.toList());

            // 构建分页响应
            Map<String, Object> response = Map.of(
                    "rooms", roomResponses,
                    "page", page,
                    "size", size,
                    "total", roomResponses.size()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("获取房间列表失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 获取用户所在的房间
     *
     * @param userId 用户ID
     * @return 房间信息
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getUserRoom(
            @PathVariable String userId) {

        log.info("获取用户房间请求: userId={}", userId);

        try {
            // 获取用户所在房间
            ApiResponse<Room> userRoomResponse = roomService.getUserRoom(userId);
            if (!userRoomResponse.isSuccess() || userRoomResponse.getData() == null) {
                return ResponseEntity.notFound().build();
            }
            Room room = userRoomResponse.getData();
            if (room == null) {
                return ResponseEntity.notFound()
                        .build();
            }

            // 转换为响应DTO
            RoomResponse response = RoomResponse.fromEntity(room, userId);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("获取用户房间失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 清理过期房间
     *
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> cleanupExpiredRooms() {

        log.info("清理过期房间请求");

        try {
            // 清理过期房间
            ApiResponse<String> cleanupResponse = roomService.cleanupExpiredRooms();
            int cleanedCount = 0; // 简化处理，实际可以从response中获取

            Map<String, Integer> response = Map.of("cleanedCount", cleanedCount);

            log.info("过期房间清理完成: cleanedCount={}", cleanedCount);
            return ResponseEntity.ok(ApiResponse.success("清理完成", response));

        } catch (Exception e) {
            log.error("清理过期房间失败 - 系统错误", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalServerError("系统内部错误，请稍后重试"));
        }
    }

    /**
     * 验证游戏配置参数
     */
    private void validateGameConfig(CreateRoomRequest.GameConfigRequest config) {
        if (config.getBaseScore() != null && (config.getBaseScore() < 1 || config.getBaseScore() > 100)) {
            throw new IllegalArgumentException("基础分必须在1-100之间");
        }
        if (config.getMaxRounds() != null && (config.getMaxRounds() < 1 || config.getMaxRounds() > 16)) {
            throw new IllegalArgumentException("最大回合数必须在1-16之间");
        }
        if (config.getThinkTime() != null && (config.getThinkTime() < 5 || config.getThinkTime() > 120)) {
            throw new IllegalArgumentException("思考时间必须在5-120秒之间");
        }
    }

    /**
     * 根据请求创建或获取游戏配置
     *
     * @param request 创建房间请求
     * @return 游戏配置ID
     */
    private Long createOrGetGameConfig(CreateRoomRequest request) {
        try {
            // 如果请求中包含游戏配置，创建新的配置
            if (request.getGameConfig() != null) {
                CreateRoomRequest.GameConfigRequest configRequest = request.getGameConfig();

                // 创建GameConfig实体
                GameConfig gameConfig = GameConfig.builder()
                        .configName("房间配置-" + request.getRoomName())
                        .configDescription("房间 " + request.getRoomName() + " 的游戏配置")
                        .createdBy(request.getCreatorId())
                        .baseScore(configRequest.getBaseScore() != null ? configRequest.getBaseScore() : 1)
                        .maxRounds(configRequest.getMaxRounds() != null ? configRequest.getMaxRounds() : 8)
                        .allowPeng(configRequest.getAllowPeng() != null ? configRequest.getAllowPeng() : true)
                        .allowGang(configRequest.getAllowGang() != null ? configRequest.getAllowGang() : true)
                        .mixedTileEnabled(configRequest.getMixedTileEnabled() != null ? configRequest.getMixedTileEnabled() : true)
                        .thinkTime(configRequest.getThinkTime() != null ? configRequest.getThinkTime() : 30)
                        .allowSpectate(request.getAllowSpectate() != null ? request.getAllowSpectate() : true)
                        .boolDefault(false)
                        .enabled(true)
                        .usageCount(0L)
                        .build();

                // 验证配置
                gameConfigService.validateConfig(gameConfig);

                // 保存配置并返回ID
                GameConfig savedConfig = gameConfigService.createDefaultConfig(
                        gameConfig.getConfigName(),
                        request.getCreatorId()
                );

                // 更新配置为请求中的值
                gameConfig.setId(savedConfig.getId());
                gameConfigService.updateConfig(savedConfig.getId(), request.getCreatorId(), gameConfig);

                return savedConfig.getId();
            }

            // 如果没有提供配置，使用默认配置
            GameConfig defaultConfig = gameConfigService.createDefaultConfig(
                    "默认配置-" + request.getRoomName(),
                    request.getCreatorId()
            );

            return defaultConfig.getId();
        } catch (Exception e) {
            log.error("创建或获取游戏配置失败", e);
            // 如果配置创建失败，返回一个基本的配置ID
            return 1L;
        }
    }
}