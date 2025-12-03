package com.mahjong.service;

import com.mahjong.entity.Player;
import com.mahjong.entity.Room;
import com.mahjong.repository.PlayerRepository;
import com.mahjong.repository.RoomRepository;
import com.mahjong.service.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 玩家管理服务
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;

    /**
     * 获取玩家信息
     *
     * @param playerId 玩家ID
     * @return 玩家信息
     */
    public ApiResponse<Player> getPlayerInfo(String playerId) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不存在");
            }

            Player player = playerOpt.get();
            return ApiResponse.success("获取玩家信息成功", player);

        } catch (Exception e) {
            log.error("获取玩家信息失败: ", e);
            return ApiResponse.error("获取玩家信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新玩家昵称
     *
     * @param playerId   玩家ID
     * @param newName    新昵称
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> updatePlayerName(String playerId, String newName) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不存在");
            }

            Player player = playerOpt.get();

            // 验证昵称长度
            if (newName == null || newName.trim().isEmpty() || newName.length() > 20) {
                return ApiResponse.badRequest("昵称长度必须在1-20个字符之间");
            }

            // 如果在房间中，检查昵称是否重复
            if (player.getRoom() != null) {
                boolean nameExists = playerRepository.existsByRoomIdAndPlayerName(
                        player.getRoom().getId(), newName);
                if (nameExists) {
                    return ApiResponse.badRequest("房间中已存在此昵称");
                }
            }

            String oldName = player.getPlayerName();
            player.setPlayerName(newName);
            playerRepository.save(player);

            log.info("玩家昵称更新成功: 玩家ID={}, {} -> {}", playerId, oldName, newName);
            return ApiResponse.success("昵称更新成功");

        } catch (Exception e) {
            log.error("更新玩家昵称失败: ", e);
            return ApiResponse.error("更新昵称失败: " + e.getMessage());
        }
    }

    /**
     * 更新玩家头像
     *
     * @param playerId   玩家ID
     * @param avatarUrl  头像URL
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> updatePlayerAvatar(String playerId, String avatarUrl) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不存在");
            }

            Player player = playerOpt.get();

            // 验证头像URL长度
            if (avatarUrl != null && avatarUrl.length() > 200) {
                return ApiResponse.badRequest("头像URL过长");
            }

            player.setPlayerAvatar(avatarUrl);
            playerRepository.save(player);

            log.info("玩家头像更新成功: 玩家ID={}", playerId);
            return ApiResponse.success("头像更新成功");

        } catch (Exception e) {
            log.error("更新玩家头像失败: ", e);
            return ApiResponse.error("更新头像失败: " + e.getMessage());
        }
    }

    /**
     * 获取玩家所在房间
     *
     * @param playerId 玩家ID
     * @return 玩家所在房间
     */
    public ApiResponse<Room> getPlayerRoom(String playerId) {
        try {
            Optional<Room> roomOpt = playerRepository.findRoomByPlayerId(playerId);
            if (roomOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不在任何房间中");
            }

            Room room = roomOpt.get();

            // 检查房间是否过期
            if (room.isExpired()) {
                return ApiResponse.badRequest("房间已过期");
            }

            return ApiResponse.success("获取玩家房间成功", room);

        } catch (Exception e) {
            log.error("获取玩家房间失败: ", e);
            return ApiResponse.error("获取房间失败: " + e.getMessage());
        }
    }

    /**
     * 更新玩家最后活跃时间
     *
     * @param playerId 玩家ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> updatePlayerActiveTime(String playerId) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不存在");
            }

            Player player = playerOpt.get();
            playerRepository.updateLastActiveAt(playerId, LocalDateTime.now());

            log.debug("更新玩家活跃时间: 玩家ID={}", playerId);
            return ApiResponse.success("更新活跃时间成功");

        } catch (Exception e) {
            log.error("更新玩家活跃时间失败: ", e);
            return ApiResponse.error("更新活跃时间失败: " + e.getMessage());
        }
    }

    /**
     * 设置玩家离线
     *
     * @param playerId 玩家ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> setPlayerOffline(String playerId) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不存在");
            }

            Player player = playerOpt.get();

            // 如果玩家在游戏中，不允许离线
            if (player.isPlaying()) {
                return ApiResponse.badRequest("游戏中无法离线");
            }

            player.setOffline();
            playerRepository.save(player);

            log.info("玩家离线: 玩家ID={}", playerId);
            return ApiResponse.success("玩家已离线");

        } catch (Exception e) {
            log.error("设置玩家离线失败: ", e);
            return ApiResponse.error("设置离线失败: " + e.getMessage());
        }
    }

    /**
     * 设置玩家在线
     *
     * @param playerId 玩家ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> setPlayerOnline(String playerId) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不存在");
            }

            Player player = playerOpt.get();
            player.setOnline();
            playerRepository.save(player);

            log.info("玩家上线: 玩家ID={}", playerId);
            return ApiResponse.success("玩家已上线");

        } catch (Exception e) {
            log.error("设置玩家在线失败: ", e);
            return ApiResponse.error("设置在线失败: " + e.getMessage());
        }
    }

    /**
     * 获取房间中的玩家列表
     *
     * @param roomId 房间ID
     * @return 玩家列表
     */
    public ApiResponse<List<Player>> getRoomPlayers(Long roomId) {
        try {
            List<Player> players = playerRepository.findByRoomId(roomId);
            return ApiResponse.success("获取房间玩家列表成功", players);

        } catch (Exception e) {
            log.error("获取房间玩家列表失败: ", e);
            return ApiResponse.error("获取玩家列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取房间中的活跃玩家（非观战者）
     *
     * @param roomId 房间ID
     * @return 活跃玩家列表
     */
    public ApiResponse<List<Player>> getActivePlayers(Long roomId) {
        try {
            List<Player> players = playerRepository.findActivePlayersByRoomId(roomId);
            return ApiResponse.success("获取活跃玩家列表成功", players);

        } catch (Exception e) {
            log.error("获取活跃玩家列表失败: ", e);
            return ApiResponse.error("获取活跃玩家失败: " + e.getMessage());
        }
    }

    /**
     * 获取房间中的观战者
     *
     * @param roomId 房间ID
     * @return 观战者列表
     */
    public ApiResponse<List<Player>> getSpectators(Long roomId) {
        try {
            List<Player> spectators = playerRepository.findSpectatorsByRoomId(roomId);
            return ApiResponse.success("获取观战者列表成功", spectators);

        } catch (Exception e) {
            log.error("获取观战者列表失败: ", e);
            return ApiResponse.error("获取观战者失败: " + e.getMessage());
        }
    }

    /**
     * 获取准备状态的玩家数量
     *
     * @param roomId 房间ID
     * @return 准备玩家数量
     */
    public ApiResponse<Long> getReadyPlayerCount(Long roomId) {
        try {
            long count = playerRepository.countReadyPlayersByRoomId(roomId);
            return ApiResponse.success("获取准备玩家数量成功", count);

        } catch (Exception e) {
            log.error("获取准备玩家数量失败: ", e);
            return ApiResponse.error("获取准备数量失败: " + e.getMessage());
        }
    }

    /**
     * 重置房间中所有玩家的状态
     *
     * @param roomId 房间ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> resetRoomPlayersStatus(Long roomId) {
        try {
            playerRepository.resetPlayersStatusInRoom(roomId);
            log.info("重置房间玩家状态: 房间ID={}", roomId);
            return ApiResponse.success("重置玩家状态成功");

        } catch (Exception e) {
            log.error("重置房间玩家状态失败: ", e);
            return ApiResponse.error("重置状态失败: " + e.getMessage());
        }
    }

    /**
     * 增加玩家分数
     *
     * @param playerId 玩家ID
     * @param score    分数
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> addPlayerScore(String playerId, int score) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不存在");
            }

            playerRepository.addScore(playerId, score);
            log.info("增加玩家分数: 玩家ID={}, 分数={}", playerId, score);
            return ApiResponse.success("分数更新成功");

        } catch (Exception e) {
            log.error("增加玩家分数失败: ", e);
            return ApiResponse.error("分数更新失败: " + e.getMessage());
        }
    }

    /**
     * 增加玩家胜利次数
     *
     * @param playerId 玩家ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<String> addPlayerWin(String playerId) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                return ApiResponse.notFound("玩家不存在");
            }

            playerRepository.incrementWins(playerId);
            log.info("增加玩家胜利次数: 玩家ID={}", playerId);
            return ApiResponse.success("胜利次数更新成功");

        } catch (Exception e) {
            log.error("增加玩家胜利次数失败: ", e);
            return ApiResponse.error("胜利次数更新失败: " + e.getMessage());
        }
    }

    /**
     * 清理不活跃的玩家
     */
    @Transactional
    public void cleanInactivePlayers() {
        try {
            // 查找5分钟未活跃的玩家
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
            List<Player> inactivePlayers = playerRepository.findInactivePlayers(threshold);

            for (Player player : inactivePlayers) {
                // 设置为离线状态
                player.setOffline();
                playerRepository.save(player);
            }

            if (!inactivePlayers.isEmpty()) {
                log.info("清理不活跃玩家完成，共清理{}个玩家", inactivePlayers.size());
            }

        } catch (Exception e) {
            log.error("清理不活跃玩家失败: ", e);
        }
    }

    /**
     * 获取房间玩家统计信息
     *
     * @param roomId 房间ID
     * @return 统计信息
     */
    public ApiResponse<Object> getRoomPlayerStats(Long roomId) {
        try {
            List<Object[]> stats = playerRepository.countPlayersByStatusInRoom(roomId);

            // 构建统计结果
            StringBuilder statsBuilder = new StringBuilder();
            statsBuilder.append("房间玩家统计:\n");
            for (Object[] stat : stats) {
                Player.PlayerStatus status = (Player.PlayerStatus) stat[0];
                Long count = (Long) stat[1];
                statsBuilder.append(status.getDescription()).append(": ").append(count).append("人\n");
            }

            return ApiResponse.success("获取玩家统计成功", statsBuilder.toString());

        } catch (Exception e) {
            log.error("获取房间玩家统计失败: ", e);
            return ApiResponse.error("获取统计失败: " + e.getMessage());
        }
    }
}