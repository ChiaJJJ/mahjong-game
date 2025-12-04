package com.mahjong.controller;

import com.mahjong.dto.request.CreateRoomRequest;
import com.mahjong.dto.request.JoinRoomRequest;
import com.mahjong.dto.request.LeaveRoomRequest;
import com.mahjong.dto.request.PlayerReadyRequest;
import com.mahjong.dto.response.RoomResponse;
import com.mahjong.entity.Room;
import com.mahjong.service.RoomService;
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

            // 创建房间
            Room room = roomService.createRoom(
                    request.getRoomName(),
                    request.getCreatorId(),
                    request.getCreatorNickname(),
                    request.getPassword(),
                    request.getMaxPlayers(),
                    request.getAllowSpectate(),
                    request.getIsPublic(),
                    request.getGameConfig()
            );

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
            Room room = roomService.joinRoom(
                    roomNumber,
                    request.getPlayerId(),
                    request.getPlayerName(),
                    request.getPassword(),
                    request.getAsSpectator(),
                    request.getAvatarUrl(),
                    request.getDeviceInfo()
            );

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
            roomService.leaveRoom(
                    roomNumber,
                    request.getPlayerId(),
                    request.getReason(),
                    request.getSwitchToSpectator()
            );

            log.info("离开房间成功: roomNumber={}, playerId={}", roomNumber, request.getPlayerId());
            return ResponseEntity.ok(ApiResponse.success("离开房间成功"));

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
            Room room = roomService.setPlayerReady(
                    roomNumber,
                    request.getPlayerId(),
                    request.getIsReady()
            );

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
            List<Room> rooms = roomService.getRoomList(status, isPublic, page, size);

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
            Room room = roomService.getUserRoom(userId);
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
            int cleanedCount = roomService.cleanupExpiredRooms();

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
}