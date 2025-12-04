package com.mahjong.service;

import com.mahjong.dto.response.RoomResponse;
import com.mahjong.entity.GameConfig;
import com.mahjong.entity.Player;
import com.mahjong.entity.Room;
import com.mahjong.repository.GameConfigRepository;
import com.mahjong.repository.PlayerRepository;
import com.mahjong.repository.RoomRepository;
import com.mahjong.service.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 房间管理服务
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final GameConfigRepository gameConfigRepository;

  
    /**
     * 将Room实体转换为RoomResponse DTO
     */
    public RoomResponse convertToRoomResponse(Room room) {
        if (room == null) {
            return null;
        }

        
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomName(room.getRoomName())
                .creatorId(room.getCreatorId())
                .creatorNickname("")
                .roomStatus(room.getRoomStatus().toString())
                .maxPlayers(room.getMaxPlayers())
                .currentPlayers(room.getCurrentPlayers())
                .spectatorCount(room.getSpectatorCount())
                .hasPassword(false)
                .allowSpectate(room.getAllowSpectate())
                .isPublic(true)
                .gameConfig(null)
                .players(null)
                .spectators(null)
                .createdAt(room.getCreatedAt())
                .expiresAt(room.getExpiresAt())
                .build();
    }

    /**
     * 创建房间
     *
     * @param roomName    房间名称
     * @param creatorId   创建者ID
     * @param creatorName 创建者昵称
     * @param configId    游戏配置ID
     * @param maxPlayers  最大玩家数
     * @param password    房间密码（可选）
     * @return 创建的房间信息
     */
    @Transactional
    public ApiResponse<Room> createRoom(String roomName, String creatorId, String creatorName,
                                      Long configId, Integer maxPlayers, String password) {
        try {
            log.info("开始创建房间: 创建者={}, 房间名={}", creatorName, roomName);

            // 验证游戏配置
            Optional<GameConfig> configOpt = gameConfigRepository.findById(configId);
            if (configOpt.isEmpty()) {
                return ApiResponse.badRequest("游戏配置不存在");
            }
            GameConfig config = configOpt.get();

            // 验证最大玩家数
            if (maxPlayers == null || maxPlayers < 2 || maxPlayers > 4) {
                maxPlayers = config.getActualPlayerCount();
            }

            // 生成唯一房间号
            String roomNumber = generateUniqueRoomNumber();

            // 创建房间
            Room room = Room.builder()
                    .roomNumber(roomNumber)
                    .roomName(roomName)
                    .password(password)
                    .creatorId(creatorId)
                    .maxPlayers(maxPlayers)
                    .currentPlayers(0)
                    .roomStatus(Room.RoomStatus.WAITING)
                    .gameConfig(config)
                    .allowSpectate(config.allowsSpectate())
                    .spectatorCount(0)
                    .expiresAt(LocalDateTime.now().plusHours(config.getRoomExpiryHours()))
                    .build();

            // 保存房间
            room = roomRepository.save(room);

            // 创建创建者玩家记录
            Player creator = Player.builder()
                    .id(creatorId)
                    .room(room)
                    .playerName(creatorName)
                    .playerPosition(1) // 创建者默认位置1
                    .playerStatus(Player.PlayerStatus.ONLINE)
                    .spectator(false)
                    .totalScore(0)
                    .winsCount(0)
                    .build();

            playerRepository.save(creator);

            // 更新房间玩家数量
            room.incrementPlayerCount();
            roomRepository.save(room);

            log.info("房间创建成功: 房间号={}, 房间ID={}", roomNumber, room.getId());
            return ApiResponse.success("房间创建成功", room);

        } catch (Exception e) {
            log.error("创建房间失败: ", e);
            return ApiResponse.error("创建房间失败: " + e.getMessage());
        }
    }

    /**
     * 加入房间
     *
     * @param roomNumber  房间号
     * @param playerId    玩家ID
     * @param playerName  玩家昵称
     * @param password    房间密码（可选）
     * @param spectator 是否为观战者
     * @return 加入结果
     */
    @Transactional
    public ApiResponse<Room> joinRoom(String roomNumber, String playerId, String playerName, String password, Boolean spectator) {
        try {
            log.info("玩家尝试加入房间: 玩家={}, 房间号={}", playerName, roomNumber);

            // 查找房间
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
            if (roomOpt.isEmpty()) {
                return ApiResponse.notFound("房间不存在");
            }

            Room room = roomOpt.get();

            // 检查房间状态
            if (!room.canJoin()) {
                return ApiResponse.badRequest("房间不可加入");
            }

            // 验证房间密码
            if (room.getPassword() != null && !room.getPassword().trim().isEmpty()) {
                if (password == null || !room.getPassword().equals(password)) {
                    return ApiResponse.badRequest("房间密码错误");
                }
            }

            // 检查是否已在房间中
            Optional<Player> existingPlayerOpt = playerRepository.findByRoomIdAndId(room.getId(), playerId);
            if (existingPlayerOpt.isPresent()) {
                return ApiResponse.badRequest("您已在此房间中");
            }

            // 处理观战者加入
            if (spectator != null && spectator) {
                if (!room.allowsSpectate()) {
                    return ApiResponse.badRequest("此房间不允许观战");
                }

                Player spectatorPlayer = Player.builder()
                        .id(playerId)
                        .room(room)
                        .playerName(playerName)
                        .playerPosition(0) // 观战者位置为0
                        .playerStatus(Player.PlayerStatus.ONLINE)
                        .spectator(true)
                        .totalScore(0)
                        .winsCount(0)
                        .build();

                playerRepository.save(spectatorPlayer);

                // 更新观战人数
                room.incrementSpectatorCount();
                roomRepository.save(room);

  
                log.info("观战者加入成功: 玩家={}, 房间号={}", playerName, roomNumber);
                return ApiResponse.success("观战成功", room);
            }

            // 处理玩家加入
            if (room.isFull()) {
                return ApiResponse.badRequest("房间已满");
            }

            // 查找可用位置
            Integer position = findAvailablePosition(room);
            if (position == null) {
                return ApiResponse.badRequest("没有可用位置");
            }

            Player player = Player.builder()
                    .id(playerId)
                    .room(room)
                    .playerName(playerName)
                    .playerPosition(position)
                    .playerStatus(Player.PlayerStatus.ONLINE)
                    .spectator(false)
                    .totalScore(0)
                    .winsCount(0)
                    .build();

            playerRepository.save(player);

            // 更新房间玩家数量
            room.incrementPlayerCount();
            roomRepository.save(room);

  
            log.info("玩家加入成功: 玩家={}, 位置={}, 房间号={}", playerName, position, roomNumber);
            return ApiResponse.success("加入房间成功", room);

        } catch (Exception e) {
            log.error("加入房间失败: ", e);
            return ApiResponse.error("加入房间失败: " + e.getMessage());
        }
    }

    /**
     * 离开房间
     *
     * @param roomNumber 房间号
     * @param playerId   玩家ID
     * @return 离开结果
     */
    @Transactional
    public ApiResponse<String> leaveRoom(String roomNumber, String playerId) {
        try {
            log.info("玩家离开房间: 玩家ID={}, 房间号={}", playerId, roomNumber);

            // 查找房间
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
            if (roomOpt.isEmpty()) {
                return ApiResponse.notFound("房间不存在");
            }

            Room room = roomOpt.get();

            // 查找玩家
            Optional<Player> playerOpt = playerRepository.findByRoomIdAndId(room.getId(), playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("您不在此房间中");
            }

            Player player = playerOpt.get();

            // 如果是游戏中的玩家，不允许离开
                      if (!player.getSpectator() && room.getRoomStatus() == Room.RoomStatus.PLAYING) {
                return ApiResponse.badRequest("游戏中无法离开房间");
            }

            // 删除玩家记录
            playerRepository.delete(player);

            // 更新房间人数
            if (player.getSpectator()) {
                room.decrementSpectatorCount();
            } else {
                room.decrementPlayerCount();
            }

            // 如果是创建者离开且有其他玩家，转移创建者身份
            if (player.getId().equals(room.getCreatorId()) && room.getCurrentPlayers() > 0) {
                List<Player> remainingPlayers = playerRepository.findActivePlayersByRoomId(room.getId());
                if (!remainingPlayers.isEmpty()) {
                    Player newCreator = remainingPlayers.get(0);
                    room.setCreatorId(newCreator.getId());
                }
            }

            roomRepository.save(room);

    
            log.info("玩家离开成功: 玩家ID={}, 房间号={}", playerId, roomNumber);
            return ApiResponse.success("离开房间成功", "已成功离开房间");

        } catch (Exception e) {
            log.error("离开房间失败: ", e);
            return ApiResponse.error("离开房间失败: " + e.getMessage());
        }
    }

    /**
     * 根据房间号获取房间
     */
    public Room getRoomByNumber(String roomNumber) {
        Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
        return roomOpt.orElse(null);
    }

    /**
     * 设置玩家准备状态
     */
    @Transactional
    public ApiResponse<Room> setPlayerReady(String roomNumber, String playerId, Boolean ready) {
        try {
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
            if (roomOpt.isEmpty()) {
                return ApiResponse.notFound("房间不存在");
            }

            Room room = roomOpt.get();
            Optional<Player> playerOpt = playerRepository.findByRoomIdAndId(room.getId(), playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不在此房间中");
            }

            Player player = playerOpt.get();
            if (ready != null && ready) {
                player.setReady();
            } else {
                player.setOnline();
            }

            playerRepository.save(player);
            return ApiResponse.success("准备状态更新成功", room);
        } catch (Exception e) {
            log.error("设置玩家准备状态失败", e);
            return ApiResponse.error("准备状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取房间列表
     */
    public ApiResponse<List<Room>> getRoomList(String status, Boolean needFull, int page, int size) {
        try {
            List<Room> rooms;
            if ("active".equals(status)) {
                rooms = roomRepository.findActiveRooms(LocalDateTime.now());
            } else {
                rooms = roomRepository.findAll();
            }
            return ApiResponse.success("获取房间列表成功", rooms);
        } catch (Exception e) {
            log.error("获取房间列表失败", e);
            return ApiResponse.error("获取房间列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户所在房间
     */
    public ApiResponse<Room> getUserRoom(String userId) {
        try {
            Optional<Room> roomOpt = playerRepository.findRoomByPlayerId(userId);
            if (roomOpt.isEmpty()) {
                return ApiResponse.notFound("用户不在任何房间中");
            }
            return ApiResponse.success("获取用户房间成功", roomOpt.get());
        } catch (Exception e) {
            log.error("获取用户房间失败", e);
            return ApiResponse.error("获取用户房间失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期房间
     */
    @Transactional
    public ApiResponse<String> cleanupExpiredRooms() {
        try {
            cleanExpiredRooms();
            return ApiResponse.success("清理过期房间成功");
        } catch (Exception e) {
            log.error("清理过期房间失败", e);
            return ApiResponse.error("清理过期房间失败: " + e.getMessage());
        }
    }

    /**
     * 获取房间信息
     *
     * @param roomNumber 房间号
     * @return 房间信息
     */
    public ApiResponse<Room> getRoomInfo(String roomNumber) {
        try {
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
            if (roomOpt.isEmpty()) {
                return ApiResponse.notFound("房间不存在");
            }

            Room room = roomOpt.get();

            // 检查房间是否过期
            if (room.isExpired()) {
                return ApiResponse.badRequest("房间已过期");
            }

            return ApiResponse.success("获取房间信息成功", room);

        } catch (Exception e) {
            log.error("获取房间信息失败: ", e);
            return ApiResponse.error("获取房间信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取活跃房间列表
     *
     * @return 活跃房间列表
     */
    public ApiResponse<List<Room>> getActiveRooms() {
        try {
            List<Room> rooms = roomRepository.findActiveRooms(LocalDateTime.now());
            return ApiResponse.success("获取活跃房间列表成功", rooms);

        } catch (Exception e) {
            log.error("获取活跃房间列表失败: ", e);
            return ApiResponse.error("获取活跃房间列表失败: " + e.getMessage());
        }
    }

    /**
     * 玩家准备
     *
     * @param roomNumber 房间号
     * @param playerId   玩家ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> playerReady(String roomNumber, String playerId) {
        try {
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
            if (roomOpt.isEmpty()) {
                return ApiResponse.notFound("房间不存在");
            }

            Room room = roomOpt.get();

            Optional<Player> playerOpt = playerRepository.findByRoomIdAndId(room.getId(), playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("您不在此房间中");
            }

            Player player = playerOpt.get();

            if (player.getSpectator()) {
                return ApiResponse.badRequest("观战者无需准备");
            }

            if (player.isReady()) {
                return ApiResponse.badRequest("您已经准备好了");
            }

            // 设置玩家准备状态
            player.setReady();
            playerRepository.save(player);

            log.info("玩家准备: 玩家ID={}, 房间号={}", playerId, roomNumber);
            return ApiResponse.success("准备成功");

        } catch (Exception e) {
            log.error("玩家准备失败: ", e);
            return ApiResponse.error("准备失败: " + e.getMessage());
        }
    }

    /**
     * 取消准备
     *
     * @param roomNumber 房间号
     * @param playerId   玩家ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> playerUnready(String roomNumber, String playerId) {
        try {
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
            if (roomOpt.isEmpty()) {
                return ApiResponse.notFound("房间不存在");
            }

            Room room = roomOpt.get();

            Optional<Player> playerOpt = playerRepository.findByRoomIdAndId(room.getId(), playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("您不在此房间中");
            }

            Player player = playerOpt.get();

            if (player.getSpectator()) {
                return ApiResponse.badRequest("观战者无法取消准备");
            }

            if (!player.isReady()) {
                return ApiResponse.badRequest("您还未准备");
            }

            // 重置玩家状态
            player.setOnline();
            playerRepository.save(player);

            log.info("玩家取消准备: 玩家ID={}, 房间号={}", playerId, roomNumber);
            return ApiResponse.success("取消准备成功");

        } catch (Exception e) {
            log.error("取消准备失败: ", e);
            return ApiResponse.error("取消准备失败: " + e.getMessage());
        }
    }

    /**
     * 检查房间是否可以开始游戏
     *
     * @param roomNumber 房间号
     * @return 检查结果
     */
    public ApiResponse<Boolean> canStartGame(String roomNumber) {
        try {
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
            if (roomOpt.isEmpty()) {
                return ApiResponse.notFound("房间不存在");
            }

            Room room = roomOpt.get();

            boolean canStart = room.canStart();
            return ApiResponse.success(canStart ? "可以开始游戏" : "不满足开始条件", canStart);

        } catch (Exception e) {
            log.error("检查游戏开始条件失败: ", e);
            return ApiResponse.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期房间
     */
    @Transactional
    public void cleanExpiredRooms() {
        try {
            List<Room> expiredRooms = roomRepository.findExpiredRooms(LocalDateTime.now());
            if (!expiredRooms.isEmpty()) {
                for (Room room : expiredRooms) {
                    // 删除房间相关的所有玩家
                    playerRepository.deleteByRoomId(room.getId());
                    // 删除房间
                    roomRepository.delete(room);
                }
                log.info("清理过期房间完成，共清理{}个房间", expiredRooms.size());
            }
        } catch (Exception e) {
            log.error("清理过期房间失败: ", e);
        }
    }

    /**
     * 生成唯一房间号
     *
     * @return 房间号
     */
    private String generateUniqueRoomNumber() {
        String roomNumber;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        do {
            // 生成6位数字房间号
            roomNumber = String.format("%06d", (int)(Math.random() * 1000000));
            attempts++;

            if (attempts >= MAX_ATTEMPTS) {
                // 如果随机生成失败，使用UUID
                roomNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                break;
            }
        } while (roomRepository.findByRoomNumber(roomNumber).isPresent());

        return roomNumber;
    }

    /**
     * 查找房间中的可用位置
     *
     * @param room 房间
     * @return 可用位置，null表示没有可用位置
     */
    private Integer findAvailablePosition(Room room) {
        List<Player> players = playerRepository.findActivePlayersByRoomId(room.getId());

        for (int position = 1; position <= room.getMaxPlayers(); position++) {
            int finalPosition = position;
            boolean isOccupied = players.stream()
                    .anyMatch(p -> p.getPlayerPosition().equals(finalPosition));
            if (!isOccupied) {
                return position;
            }
        }

        return null;
    }
}