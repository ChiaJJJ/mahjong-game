package com.mahjong.repository;

import com.mahjong.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 游戏Repository接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    /**
     * 根据房间ID查找当前游戏
     */
    Optional<Game> findTopByRoomIdOrderByCreatedAtDesc(Long roomId);

    /**
     * 根据房间ID和游戏状态查找
     */
    List<Game> findByRoomIdAndGameStatus(Long roomId, Game.GameStatus status);

    /**
     * 根据房间ID查找所有游戏
     */
    List<Game> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    /**
     * 根据房间ID分页查找游戏
     */
    Page<Game> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    /**
     * 查找指定时间范围内的游戏
     */
    @Query("SELECT g FROM Game g WHERE g.startedAt BETWEEN :startTime AND :endTime")
    List<Game> findGamesInTimeRange(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查找未开始的游戏
     */
    List<Game> findByGameStatus(Game.GameStatus status);

    /**
     * 查找超时的游戏
     */
    @Query("SELECT g FROM Game g WHERE g.gameStatus = com.mahjong.entity.Game.GameStatus.PLAYING " +
           "AND g.updatedAt < :timeoutThreshold")
    List<Game> findTimedOutGames(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    /**
     * 查找玩家参与的游戏
     */
    @Query("SELECT g FROM Game g JOIN g.room r JOIN r.players p WHERE p.id = :playerId ORDER BY g.createdAt DESC")
    List<Game> findGamesByPlayerId(@Param("playerId") String playerId);

    /**
     * 更新游戏状态
     */
    @Modifying
    @Query("UPDATE Game g SET g.gameStatus = :status WHERE g.id = :gameId")
    void updateGameStatus(@Param("gameId") Long gameId, @Param("status") Game.GameStatus status);

    /**
     * 更新当前玩家
     */
    @Modifying
    @Query("UPDATE Game g SET g.currentPlayer = :playerPosition WHERE g.id = :gameId")
    void updateCurrentPlayer(@Param("gameId") Long gameId, @Param("playerPosition") Integer playerPosition);

    /**
     * 更新当前回合
     */
    @Modifying
    @Query("UPDATE Game g SET g.roundNumber = :round WHERE g.id = :gameId")
    void updateCurrentRound(@Param("gameId") Long gameId, @Param("round") Integer round);

    /**
     * 设置游戏开始时间
     */
    @Modifying
    @Query("UPDATE Game g SET g.startedAt = :startTime WHERE g.id = :gameId")
    void setGameStartTime(@Param("gameId") Long gameId, @Param("startTime") LocalDateTime startTime);

    /**
     * 设置游戏结束时间和结果
     */
    @Modifying
    @Query("UPDATE Game g SET g.endedAt = :finishTime WHERE g.id = :gameId")
    void finishGame(@Param("gameId") Long gameId,
                    @Param("finishTime") LocalDateTime finishTime);

    /**
     * 删除指定时间之前的游戏记录
     */
    @Modifying
    @Query("DELETE FROM Game g WHERE g.createdAt < :cutoffDate")
    void deleteGamesBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 统计房间内游戏数量
     */
    @Query("SELECT COUNT(g) FROM Game g WHERE g.room.id = :roomId")
    long countGamesByRoomId(@Param("roomId") Long roomId);

    /**
     * 统计各种状态的游戏数量
     */
    @Query("SELECT g.gameStatus, COUNT(g) FROM Game g GROUP BY g.gameStatus")
    List<Object[]> countGamesByStatus();

    /**
     * 查找房间内最新的游戏局数
     */
    @Query("SELECT MAX(g.roundNumber) FROM Game g WHERE g.room.id = :roomId")
    Integer findMaxGameNumberByRoomId(@Param("roomId") Long roomId);

    /**
     * 查找包含混牌信息的游戏
     */
    List<Game> findByMixedTilesIsNotNull();

    /**
     * 更新弃牌堆信息
     */
    @Modifying
    @Query("UPDATE Game g SET g.discardPile = :tiles WHERE g.id = :gameId")
    void updateDiscardPile(@Param("gameId") Long gameId, @Param("tiles") String tiles);
}