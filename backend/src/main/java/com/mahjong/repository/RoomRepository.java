package com.mahjong.repository;

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
 * 房间Repository接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * 根据房间号查找房间
     */
    Optional<Room> findByRoomNumber(String roomNumber);

    /**
     * 查找创建者的房间
     */
    List<Room> findByCreatorId(String creatorId);

    /**
     * 查找活跃房间（未过期且等待中）
     */
    @Query("SELECT r FROM Room r WHERE r.roomStatus = com.mahjong.entity.Room.RoomStatus.WAITING " +
           "AND (r.expiresAt IS NULL OR r.expiresAt > :now) ORDER BY r.createdAt DESC")
    List<Room> findActiveRooms(@Param("now") LocalDateTime now);

    /**
     * 查找需要清理的过期房间
     */
    @Query("SELECT r FROM Room r WHERE r.expiresAt IS NOT NULL AND r.expiresAt < :now")
    List<Room> findExpiredRooms(@Param("now") LocalDateTime now);

    /**
     * 增加房间玩家数量
     */
    @Modifying
    @Query("UPDATE Room r SET r.currentPlayers = r.currentPlayers + 1 WHERE r.id = :roomId")
    void incrementPlayerCount(@Param("roomId") Long roomId);

    /**
     * 减少房间玩家数量
     */
    @Modifying
    @Query("UPDATE Room r SET r.currentPlayers = r.currentPlayers - 1 WHERE r.id = :roomId AND r.currentPlayers > 0")
    void decrementPlayerCount(@Param("roomId") Long roomId);

    /**
     * 更新房间状态
     */
    @Modifying
    @Query("UPDATE Room r SET r.roomStatus = :status WHERE r.id = :roomId")
    void updateRoomStatus(@Param("roomId") Long roomId, @Param("status") Room.RoomStatus status);

    /**
     * 设置房间过期时间
     */
    @Modifying
    @Query("UPDATE Room r SET r.expiresAt = :expiresAt WHERE r.id = :roomId")
    void setExpiresAt(@Param("roomId") Long roomId, @Param("expiresAt") LocalDateTime expiresAt);

    /**
     * 删除过期的房间
     */
    @Modifying
    @Query("DELETE FROM Room r WHERE r.expiresAt IS NOT NULL AND r.expiresAt < :now")
    void deleteExpiredRooms(@Param("now") LocalDateTime now);

    /**
     * 查找指定状态下的房间数
     */
    long countByRoomStatus(Room.RoomStatus status);

    /**
     * 查找用户的房间（玩家或创建者）
     */
    @Query("SELECT r FROM Room r JOIN r.players p WHERE p.id = :playerId AND r.roomStatus = :status")
    List<Room> findRoomsByPlayer(@Param("playerId") String playerId, @Param("status") Room.RoomStatus status);
}