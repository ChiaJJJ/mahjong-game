<template>
  <div class="game-room-container">
    <!-- 房间头部信息 -->
    <div class="room-header">
      <div class="room-info">
        <h1>{{ roomData?.roomName || '游戏房间' }}</h1>
        <div class="room-meta">
          <span class="room-number">房间号: {{ roomNumber }}</span>
          <el-tag :type="getStatusTagType(roomData?.roomStatus)">
            {{ getStatusText(roomData?.roomStatus) }}
          </el-tag>
        </div>
      </div>
      <div class="room-actions">
        <el-button @click="handleLeaveRoom" :disabled="isGameStarted">
          <el-icon><Back /></el-icon>
          离开房间
        </el-button>
      </div>
    </div>

    <!-- 游戏桌面 -->
    <div class="game-table-container">
      <div class="game-table">
        <!-- 玩家位置 -->
        <div class="player-positions">
          <!-- 上方玩家 -->
          <div class="player-position top" v-if="getTopPlayer()">
            <PlayerCard :player="getTopPlayer()" :position="'top'" />
          </div>

          <!-- 左侧和右侧玩家 -->
          <div class="side-players">
            <div class="player-position left" v-if="getLeftPlayer()">
              <PlayerCard :player="getLeftPlayer()" :position="'left'" />
            </div>
            <div class="table-center">
              <div class="mahjong-table">
                <div class="table-center-content">
                  <h2 v-if="!isGameStarted">等待玩家准备...</h2>
                  <div v-else class="game-status">
                    <p>游戏进行中</p>
                  </div>
                </div>
              </div>
            </div>
            <div class="player-position right" v-if="getRightPlayer()">
              <PlayerCard :player="getRightPlayer()" :position="'right'" />
            </div>
          </div>

          <!-- 下方玩家（当前玩家） -->
          <div class="player-position bottom" v-if="getCurrentPlayer()">
            <PlayerCard :player="getCurrentPlayer()" :position="'bottom'" :isCurrentPlayer="true" />
          </div>
        </div>
      </div>
    </div>

    <!-- 玩家列表和控制面板 -->
    <div class="room-panel">
      <div class="panel-section">
        <h3>玩家列表 ({{ roomData?.currentPlayers || 0 }}/{{ roomData?.maxPlayers || 4 }})</h3>
        <div class="players-list">
          <div
            v-for="player in roomData?.players || []"
            :key="player.id"
            class="player-item"
            :class="{ 'is-current': player.id === currentPlayerId }"
          >
            <div class="player-avatar">
              <el-avatar :src="player.avatarUrl" :alt="player.nickname">
                <el-icon><User /></el-icon>
              </el-avatar>
            </div>
            <div class="player-info">
              <div class="player-name">{{ player.nickname }}</div>
              <div class="player-status">
                <el-tag :type="player.isOnline ? 'success' : 'danger'" size="small">
                  {{ player.isOnline ? '在线' : '离线' }}
                </el-tag>
                <el-tag :type="player.isReady ? 'success' : 'warning'" size="small">
                  {{ player.isReady ? '已准备' : '未准备' }}
                </el-tag>
                <el-tag v-if="player.isCreator" type="primary" size="small">
                  房主
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 房间操作 -->
      <div class="panel-section" v-if="canOperate">
        <div class="room-operations">
          <el-button
            type="primary"
            :loading="isReadyLoading"
            @click="handleReadyToggle"
            size="large"
          >
            {{ isReady ? '取消准备' : '准备' }}
          </el-button>
          <el-button
            v-if="canStartGame"
            type="success"
            @click="handleStartGame"
            size="large"
          >
            开始游戏
          </el-button>
        </div>
      </div>

      <!-- 游戏配置信息 -->
      <div class="panel-section">
        <h4>游戏配置</h4>
        <div class="config-info">
          <div class="config-item">
            <span class="config-label">基础分:</span>
            <span>{{ roomData?.gameConfig?.baseScore || 1 }}</span>
          </div>
          <div class="config-item">
            <span class="config-label">最大回合:</span>
            <span>{{ roomData?.gameConfig?.maxRounds || 8 }}</span>
          </div>
          <div class="config-item">
            <span class="config-label">思考时间:</span>
            <span>{{ roomData?.gameConfig?.thinkTime || 30 }}秒</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天面板 -->
    <div class="chat-panel">
      <ChatPanel />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, User } from '@element-plus/icons-vue'
import { useRoomStore } from '@/stores/room'
import { useGameStore } from '@/stores/game'
import PlayerCard from '@/components/game/PlayerCard.vue'
import ChatPanel from '@/components/chat/ChatPanel.vue'

const route = useRoute()
const router = useRouter()
const roomStore = useRoomStore()
const gameStore = useGameStore()

const roomNumber = computed(() => route.params.roomNumber)
const roomData = computed(() => roomStore.currentRoom)
const currentPlayerId = ref('') // 应该从用户状态管理中获取
const isReadyLoading = ref(false)
const wsConnection = ref(null)

// 计算属性
const isGameStarted = computed(() => {
  return roomData.value?.roomStatus === 'PLAYING'
})

const isReady = computed(() => {
  const currentPlayer = roomData.value?.players?.find(p => p.id === currentPlayerId.value)
  return currentPlayer?.isReady || false
})

const canOperate = computed(() => {
  const currentPlayer = roomData.value?.players?.find(p => p.id === currentPlayerId.value)
  return currentPlayer && !currentPlayer.isSpectator && !isGameStarted.value
})

const canStartGame = computed(() => {
  return roomStore.canStartGame && roomStore.isRoomOwner
})

// 生命周期钩子
onMounted(async () => {
  // 从本地存储获取当前用户ID
  currentPlayerId.value = localStorage.getItem('currentPlayerId') || ''

  if (!currentPlayerId.value) {
    ElMessage.error('未找到用户信息，请重新加入房间')
    router.push('/join-room')
    return
  }

  try {
    // 加载房间信息
    await roomStore.getRoomInfo(roomNumber.value)

    // 建立WebSocket连接
    connectWebSocket()
  } catch (error) {
    console.error('加载房间信息失败:', error)
    ElMessage.error('房间不存在或已过期')
    router.push('/')
  }
})

onUnmounted(() => {
  if (wsConnection.value) {
    wsConnection.value.close()
  }
})

// WebSocket连接
const connectWebSocket = () => {
  // WebSocket连接逻辑将在后续实现
  console.log('建立WebSocket连接:', roomNumber.value)
}

// 玩家位置获取
const getTopPlayer = () => roomData.value?.players?.find((p, index) => index === 1 && p.id !== currentPlayerId.value)
const getLeftPlayer = () => roomData.value?.players?.find((p, index) => index === 1 && p.id !== currentPlayerId.value)
const getRightPlayer = () => roomData.value?.players?.find((p, index) => index === 2 && p.id !== currentPlayerId.value)
const getCurrentPlayer = () => roomData.value?.players?.find(p => p.id === currentPlayerId.value)

// 状态相关方法
const getStatusTagType = (status) => {
  const statusMap = {
    'WAITING': 'warning',
    'PLAYING': 'success',
    'FINISHED': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'WAITING': '等待中',
    'PLAYING': '游戏中',
    'FINISHED': '已结束'
  }
  return statusMap[status] || '未知'
}

// 事件处理
const handleReadyToggle = async () => {
  try {
    isReadyLoading.value = true
    await roomStore.setPlayerReady(roomNumber.value, {
      playerId: currentPlayerId.value,
      isReady: !isReady.value
    })
  } catch (error) {
    console.error('设置准备状态失败:', error)
    ElMessage.error('操作失败')
  } finally {
    isReadyLoading.value = false
  }
}

const handleStartGame = async () => {
  try {
    await ElMessageBox.confirm('确定开始游戏吗？', '确认开始', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 开始游戏逻辑将在后续实现
    ElMessage.success('游戏开始！')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始游戏失败:', error)
    }
  }
}

const handleLeaveRoom = async () => {
  try {
    await ElMessageBox.confirm('确定要离开房间吗？', '确认离开', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await roomStore.leaveRoom(roomNumber.value, {
      playerId: currentPlayerId.value,
      reason: '主动离开'
    })

    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('离开房间失败:', error)
      ElMessage.error('离开房间失败')
    }
  }
}
</script>

<style lang="scss" scoped>
.game-room-container {
  min-height: 100vh;
  background: #2c3e50;
  display: flex;
  flex-direction: column;
  color: white;
}

.room-header {
  background: rgba(0, 0, 0, 0.3);
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  .room-info {
    h1 {
      margin: 0 0 0.5rem 0;
      font-size: 1.5rem;
    }

    .room-meta {
      display: flex;
      align-items: center;
      gap: 1rem;

      .room-number {
        color: #3498db;
        font-weight: bold;
      }
    }
  }
}

.game-table-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.game-table {
  width: 100%;
  max-width: 800px;
  aspect-ratio: 4/3;
}

.player-positions {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;

  .player-position {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .top, .bottom {
    max-height: 25%;
  }

  .side-players {
    flex: 1;
    display: flex;
    align-items: center;

    .left, .right {
      width: 20%;
      max-width: 150px;
    }

    .table-center {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .bottom {
    margin-top: 1rem;
  }
}

.mahjong-table {
  width: 100%;
  height: 100%;
  background: radial-gradient(ellipse at center, #27ae60 0%, #229954 70%);
  border-radius: 50%;
  border: 8px solid #1e8449;
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.5);
  position: relative;

  .table-center-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    font-size: 1.2rem;
    font-weight: bold;
    color: #f1c40f;
  }
}

.room-panel {
  background: rgba(0, 0, 0, 0.3);
  padding: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;

  .panel-section {
    h3, h4 {
      margin: 0 0 1rem 0;
      color: #3498db;
    }

    h3 {
      font-size: 1.2rem;
    }

    h4 {
      font-size: 1rem;
    }
  }
}

.players-list {
  .player-item {
    display: flex;
    align-items: center;
    padding: 0.8rem;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    margin-bottom: 0.5rem;
    transition: all 0.3s ease;

    &.is-current {
      background: rgba(52, 152, 219, 0.3);
      border: 1px solid #3498db;
    }

    .player-avatar {
      margin-right: 1rem;
    }

    .player-info {
      flex: 1;

      .player-name {
        font-weight: bold;
        margin-bottom: 0.3rem;
      }

      .player-status {
        display: flex;
        gap: 0.3rem;
        flex-wrap: wrap;
      }
    }
  }
}

.room-operations {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.config-info {
  .config-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
    padding: 0.3rem 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .config-label {
      color: #bdc3c7;
    }
  }
}

.chat-panel {
  position: fixed;
  bottom: 0;
  right: 0;
  width: 300px;
  height: 400px;
  background: rgba(0, 0, 0, 0.5);
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

@media (max-width: 768px) {
  .room-header {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }

  .room-panel {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .chat-panel {
    position: static;
    width: 100%;
    height: 300px;
  }
}
</style>