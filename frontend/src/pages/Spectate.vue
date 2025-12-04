<template>
  <div class="spectate-container">
    <div class="spectate-header">
      <h1>观战模式</h1>
      <div class="header-actions">
        <el-button @click="handleExitSpectate">
          <el-icon><Close /></el-icon>
          退出观战
        </el-button>
      </div>
    </div>

    <div class="spectate-content" v-if="roomData">
      <!-- 观战者信息栏 -->
      <div class="spectator-bar">
        <div class="spectator-info">
          <span>观战者: {{ spectatorName }}</span>
          <el-tag type="info">观战中</el-tag>
        </div>
        <div class="spectator-count">
          <el-icon><View /></el-icon>
          当前观战人数: {{ spectatorCount }}
        </div>
      </div>

      <!-- 游戏桌面 -->
      <div class="game-table-spectate">
        <div class="spectate-table">
          <!-- 玩家位置展示 -->
          <div class="players-spectate">
            <div class="player-spectate top" v-if="getTopPlayer()">
              <SpectatePlayerCard :player="getTopPlayer()" position="top" />
            </div>
            <div class="side-players-spectate">
              <div class="player-spectate left" v-if="getLeftPlayer()">
                <SpectatePlayerCard :player="getLeftPlayer()" position="left" />
              </div>
              <div class="table-center-spectate">
                <div class="mahjong-table">
                  <div class="table-content">
                    <h2>{{ getGameStatusText() }}</h2>
                    <p v-if="currentRound">第 {{ currentRound }} / {{ totalRounds }} 回合</p>
                  </div>
                </div>
              </div>
              <div class="player-spectate right" v-if="getRightPlayer()">
                <SpectatePlayerCard :player="getRightPlayer()" position="right" />
              </div>
            </div>
            <div class="player-spectate bottom" v-if="getBottomPlayer()">
              <SpectatePlayerCard :player="getBottomPlayer()" position="bottom" />
            </div>
          </div>
        </div>
      </div>

      <!-- 游戏信息面板 -->
      <div class="spectate-panels">
        <div class="panel-section">
          <h3>游戏信息</h3>
          <div class="game-info">
            <div class="info-item">
              <span class="label">房间名称:</span>
              <span>{{ roomData.roomName }}</span>
            </div>
            <div class="info-item">
              <span class="label">房间号:</span>
              <span>{{ roomId }}</span>
            </div>
            <div class="info-item">
              <span class="label">游戏状态:</span>
              <el-tag :type="getStatusTagType(roomData.roomStatus)">
                {{ getStatusText(roomData.roomStatus) }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="panel-section">
          <h3>游戏规则</h3>
          <div class="rules-info">
            <div class="rule-item">
              <span class="label">基础分:</span>
              <span>{{ roomData.gameConfig?.baseScore || 1 }}</span>
            </div>
            <div class="rule-item">
              <span class="label">最大回合:</span>
              <span>{{ roomData.gameConfig?.maxRounds || 8 }}</span>
            </div>
            <div class="rule-item">
              <span class="label">混牌:</span>
              <el-tag :type="roomData.gameConfig?.mixedTileEnabled ? 'success' : 'danger'">
                {{ roomData.gameConfig?.mixedTileEnabled ? '启用' : '禁用' }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="panel-section">
          <h3>玩家得分</h3>
          <div class="score-board">
            <div
              v-for="player in roomData.players"
              :key="player.id"
              class="score-item"
              :class="{ 'is-current-turn': isCurrentPlayerTurn(player.id) }"
            >
              <div class="player-name">{{ player.nickname }}</div>
              <div class="player-score">{{ player.totalScore || 0 }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 观战聊天 -->
      <div class="spectate-chat">
        <h3>观战聊天</h3>
        <div class="chat-container">
          <div class="chat-messages" ref="chatMessages">
            <div
              v-for="message in chatMessages"
              :key="message.id"
              class="chat-message"
              :class="message.type"
            >
              <span class="message-sender">{{ message.sender }}:</span>
              <span class="message-content">{{ message.content }}</span>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="newMessage"
              placeholder="输入消息（观战者可以聊天）"
              @keyup.enter="sendMessage"
              :maxlength="100"
            >
              <template #append>
                <el-button @click="sendMessage">发送</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else class="loading-state">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <p>加载房间信息中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close, View, Loading } from '@element-plus/icons-vue'
import SpectatePlayerCard from '@/components/game/SpectatePlayerCard.vue'

const route = useRoute()
const router = useRouter()

const roomId = computed(() => route.params.roomId)
const roomData = ref(null)
const spectatorName = ref('观战用户')
const spectatorCount = ref(0)
const currentRound = ref(0)
const totalRounds = ref(8)
const chatMessages = ref([])
const newMessage = ref('')
const wsConnection = ref(null)

// 生命周期钩子
onMounted(async () => {
  spectatorName.value = localStorage.getItem('spectatorName') || '观战用户' + Math.floor(Math.random() * 1000)
  await loadRoomInfo()
  connectWebSocket()
})

onUnmounted(() => {
  if (wsConnection.value) {
    wsConnection.value.close()
  }
})

// 加载房间信息
const loadRoomInfo = async () => {
  try {
    // 调用API获取房间信息
    // const response = await roomApi.getRoomInfo(roomId.value)
    // roomData.value = response.data

    // 模拟数据
    roomData.value = {
      roomName: '河南麻将房间',
      roomStatus: 'PLAYING',
      gameConfig: {
        baseScore: 1,
        maxRounds: 8,
        mixedTileEnabled: true
      },
      players: [
        { id: '1', nickname: '玩家1', totalScore: 100 },
        { id: '2', nickname: '玩家2', totalScore: -50 },
        { id: '3', nickname: '玩家3', totalScore: 200 },
        { id: '4', nickname: '玩家4', totalScore: -150 }
      ]
    }

    spectatorCount.value = roomData.value?.spectatorCount || 1
    currentRound.value = 3
    totalRounds.value = roomData.value?.gameConfig?.maxRounds || 8
  } catch (error) {
    console.error('加载房间信息失败:', error)
    ElMessage.error('房间不存在或已结束')
    router.push('/')
  }
}

// WebSocket连接
const connectWebSocket = () => {
  // WebSocket连接逻辑
  console.log('建立观战WebSocket连接:', roomId.value)
}

// 玩家位置获取
const getTopPlayer = () => roomData.value?.players?.[0]
const getLeftPlayer = () => roomData.value?.players?.[1]
const getRightPlayer = () => roomData.value?.players?.[2]
const getBottomPlayer = () => roomData.value?.players?.[3]

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

const getGameStatusText = () => {
  if (roomData.value?.roomStatus === 'WAITING') {
    return '游戏准备中'
  } else if (roomData.value?.roomStatus === 'PLAYING') {
    return '游戏进行中'
  } else {
    return '游戏已结束'
  }
}

const isCurrentPlayerTurn = (playerId) => {
  // 判断当前是否为该玩家的回合
  return false // 临时值，需要从游戏状态中获取
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString()
}

// 事件处理
const handleExitSpectate = async () => {
  try {
    // 关闭WebSocket连接
    if (wsConnection.value) {
      wsConnection.value.close()
    }

    router.push('/')
  } catch (error) {
    console.error('退出观战失败:', error)
  }
}

const sendMessage = () => {
  if (!newMessage.value.trim()) return

  const message = {
    id: Date.now(),
    sender: spectatorName.value,
    content: newMessage.value,
    type: 'spectator',
    timestamp: new Date().toISOString()
  }

  chatMessages.value.push(message)
  newMessage.value = ''

  // 发送消息到服务器
  if (wsConnection.value) {
    wsConnection.value.send(JSON.stringify({
      type: 'spectator_chat',
      data: message
    }))
  }
}
</script>

<style lang="scss" scoped>
.spectate-container {
  min-height: 100vh;
  background: #1a1a1a;
  color: white;
  display: flex;
  flex-direction: column;
}

.spectate-header {
  background: rgba(0, 0, 0, 0.3);
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  h1 {
    margin: 0;
    color: #f39c12;
  }
}

.spectator-bar {
  background: rgba(0, 0, 0, 0.2);
  padding: 0.8rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  .spectator-info {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .spectator-count {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #95a5a6;
  }
}

.spectate-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 2rem;
}

.game-table-spectate {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
}

.spectate-table {
  width: 100%;
  max-width: 600px;
  aspect-ratio: 4/3;
}

.players-spectate {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;

  .player-spectate {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .top, .bottom {
    max-height: 20%;
  }

  .side-players-spectate {
    flex: 1;
    display: flex;
    align-items: center;

    .left, .right {
      width: 15%;
      max-width: 100px;
    }

    .table-center-spectate {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.mahjong-table {
  width: 100%;
  height: 100%;
  background: radial-gradient(ellipse at center, #8b4513 0%, #654321 70%);
  border-radius: 50%;
  border: 6px solid #4a2c17;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
  position: relative;

  .table-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: #f39c12;

    h2 {
      margin: 0 0 0.5rem 0;
      font-size: 1.1rem;
    }

    p {
      margin: 0;
      font-size: 0.9rem;
      color: #e67e22;
    }
  }
}

.spectate-panels {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;

  .panel-section {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    padding: 1rem;
    border: 1px solid rgba(255, 255, 255, 0.1);

    h3 {
      margin: 0 0 1rem 0;
      color: #3498db;
      font-size: 1.1rem;
    }
  }
}

.game-info, .rules-info {
  .info-item, .rule-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
    padding: 0.3rem 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .label {
      color: #95a5a6;
    }
  }
}

.score-board {
  .score-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
    margin-bottom: 0.5rem;

    &.is-current-turn {
      background: rgba(52, 152, 219, 0.3);
      border: 1px solid #3498db;
    }

    .player-name {
      font-weight: bold;
    }

    .player-score {
      font-size: 1.1rem;
      font-weight: bold;
      color: #f39c12;
    }
  }
}

.spectate-chat {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.1);

  h3 {
    margin: 0 0 1rem 0;
    color: #3498db;
  }

  .chat-container {
    height: 200px;
    display: flex;
    flex-direction: column;
  }

  .chat-messages {
    flex: 1;
    overflow-y: auto;
    margin-bottom: 1rem;
    padding: 0.5rem;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 4px;

    .chat-message {
      padding: 0.3rem;
      margin-bottom: 0.3rem;
      font-size: 0.9rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;

      &.spectator {
        color: #95a5a6;
      }

      .message-sender {
        font-weight: bold;
        color: #3498db;
      }

      .message-content {
        flex: 1;
      }

      .message-time {
        font-size: 0.8rem;
        color: #7f8c8d;
      }
    }
  }
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 50vh;

  .loading-icon {
    font-size: 3rem;
    color: #3498db;
    animation: rotate 1s linear infinite;
    margin-bottom: 1rem;
  }

  p {
    color: #95a5a6;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .spectate-header {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }

  .spectate-panels {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .players-spectate .side-players-spectate .left,
  .players-spectate .side-players-spectate .right {
    width: 10%;
  }
}
</style>