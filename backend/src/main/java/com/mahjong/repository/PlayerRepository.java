package com.mahjong.repository;

import com.mahjong.entity.Player;
import com.mahjong.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 玩家Repository接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {

    /**
     * 根据房间ID和玩家位置查找玩家
     */
    Optional<Player> findByRoomIdAndPlayerPosition(Long roomId, Integer playerPosition);

    /**
     * 查找房间内所有玩家
     */
    List<Player> findByRoomId(Long roomId);

    /**
     * 查找房间内所有非观战玩家
     */
    @Query("SELECT p FROM Player p WHERE p.room.id = :roomId AND p.isSpectator = false")
    List<Player> findActivePlayersByRoomId(@Param("roomId") Long roomId);

    /**
     * 查找房间内所有观战者
     */
    @Query("SELECT p FROM Player p WHERE p.room.id = :roomId AND p.isSpectator = true")
    List<Player> findSpectatorsByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据房间ID和玩家ID查找玩家
     */
    Optional<Player> findByRoomIdAndPlayerId(Long roomId, String playerId);

    /**
     * 查找房间内准备状态的玩家数量
     */
    @Query("SELECT COUNT(p) FROM Player p WHERE p.room.id = :roomId AND p.playerStatus = com.mahjong.entity.Player.PlayerStatus.READY")
    long countReadyPlayersByRoomId(@Param("roomId") Long roomId);

    /**
     * 查找房间内非观战玩家数量
     */
    @Query("SELECT COUNT(p) FROM Player p WHERE p.room.id = :roomId AND p.isSpectator = false")
    long countActivePlayersByRoomId(@Param("roomId") Long roomId);

    /**
     * 查找长时间未活跃的玩家
     */
    @Query("SELECT p FROM Player p WHERE p.lastActiveAt < :threshold")
    List<Player> findInactivePlayers(@Param("threshold") LocalDateTime threshold);

    /**
     * 根据玩家状态查找
     */
    List<Player> findByPlayerStatus(Player.PlayerStatus status);

    /**
     * 查找房间内指定状态的玩家
     */
    List<Player> findByRoomIdAndPlayerStatus(Long roomId, Player.PlayerStatus status);

    /**
     * 更新玩家状态
     */
    @Modifying
    @Query("UPDATE Player p SET p.playerStatus = :status WHERE p.id = :playerId")
    void updatePlayerStatus(@Param("playerId") String playerId, @Param("status") Player.PlayerStatus status);

    /**
     * 更新玩家最后活跃时间
     */
    @Modifying
    @Query("UPDATE Player p SET p.lastActiveAt = :lastActiveAt WHERE p.id = :playerId")
    void updateLastActiveAt(@Param("playerId") String playerId, @Param("lastActiveAt") LocalDateTime lastActiveAt);

    /**
     * 增加玩家分数
     */
    @Modifying
    @Query("UPDATE Player p SET p.totalScore = p.totalScore + :score WHERE p.id = :playerId")
    void addScore(@Param("playerId") String playerId, @Param("score") int score);

    /**
     * 减少玩家分数
     */
    @Modifying
    @Query("UPDATE Player p SET p.totalScore = p.totalScore - :score WHERE p.id = :playerId AND p.totalScore >= :score")
    void subtractScore(@Param("playerId") String playerId, @Param("score") int score);

    /**
     * 增加玩家胜利次数
     */
    @Modifying
    @Query("UPDATE Player p SET p.winsCount = p.winsCount + 1 WHERE p.id = :playerId")
    void incrementWins(@Param("playerId") String playerId);

    /**
     * 设置玩家准备状态
     */
    @Modifying
    @Query("UPDATE Player p SET p.playerStatus = com.mahjong.entity.Player.PlayerStatus.READY, p.readyAt = :readyAt WHERE p.id = :playerId")
    void setPlayerReady(@Param("playerId") String playerId, @Param("readyAt") LocalDateTime readyAt);

    /**
     * 重置房间内所有玩家状态
     */
    @Modifying
    @Query("UPDATE Player p SET p.playerStatus = com.mahjong.entity.Player.PlayerStatus.ONLINE, p.readyAt = null WHERE p.room.id = :roomId")
    void resetPlayersStatusInRoom(@Param("roomId") Long roomId);

    /**
     * 删除房间内所有玩家
     */
    void deleteByRoomId(Long roomId);

    /**
     * 查找玩家所在房间
     */
    @Query("SELECT p.room FROM Player p WHERE p.id = :playerId")
    Optional<Room> findRoomByPlayerId(@Param("playerId") String playerId);

    /**
     * 检查房间内是否有指定昵称的玩家
     */
    boolean existsByRoomIdAndPlayerName(Long roomId, String playerName);

    /**
     * 统计房间内不同状态的玩家数量
     */
    @Query("SELECT p.playerStatus, COUNT(p) FROM Player p WHERE p.room.id = :roomId GROUP BY p.playerStatus")
    List<Object[]> countPlayersByStatusInRoom(@Param("roomId") Long roomId);
}