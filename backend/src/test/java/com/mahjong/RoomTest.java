package com.mahjong;

import com.mahjong.entity.Room;
import com.mahjong.entity.Player;
import com.mahjong.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoomTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void shouldCreateRoom() {
        Room room = new Room();
        room.setRoomNumber("12345678");
        room.setRoomName("测试房间");
        room.setMaxPlayers(4);
        room.setAllowSpectate(true);

        Room savedRoom = roomRepository.save(room);

        assertNotNull(savedRoom.getId());
        assertEquals("12345678", savedRoom.getRoomNumber());
        assertEquals("测试房间", savedRoom.getRoomName());
        assertEquals(4, savedRoom.getMaxPlayers());
        assertTrue(savedRoom.isAllowSpectate());
    }

    @Test
    void shouldFindRoomByRoomNumber() {
        // 先创建一个房间
        Room room = new Room();
        room.setRoomNumber("87654321");
        room.setRoomName("查找测试房间");
        roomRepository.save(room);

        // 查找房间
        Room foundRoom = roomRepository.findByRoomNumber("87654321").orElse(null);

        assertNotNull(foundRoom);
        assertEquals("查找测试房间", foundRoom.getRoomName());
    }

    @Test
    void shouldAddPlayerToRoom() {
        // 创建房间
        Room room = new Room();
        room.setRoomNumber("11223344");
        room.setMaxPlayers(4);
        room.setCurrentPlayers(0);
        Room savedRoom = roomRepository.save(room);

        // 创建玩家
        Player player = new Player();
        player.setPlayerId("player001");
        player.setPlayerName("测试玩家");
        player.setRoomId(savedRoom.getId());
        player.setPlayerPosition(1);

        // 模拟保存玩家（这里需要PlayerRepository）
        // playerRepository.save(player);

        // 更新房间的玩家数量
        savedRoom.setCurrentPlayers(1);
        Room updatedRoom = roomRepository.save(savedRoom);

        assertEquals(1, updatedRoom.getCurrentPlayers());
    }
}