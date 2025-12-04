<template>
  <div class="join-room-container">
    <div class="form-card">
      <h2>加入房间</h2>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        @submit.prevent="handleJoinRoom"
      >
        <el-form-item label="房间号" prop="roomNumber">
          <el-input
            v-model="form.roomNumber"
            placeholder="请输入6位房间号"
            maxlength="6"
            clearable
            @input="handleRoomNumberInput"
          >
            <template #prefix>
              <el-icon><House /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="您的昵称" prop="playerName">
          <el-input
            v-model="form.playerName"
            placeholder="请输入您的昵称"
            maxlength="20"
            show-word-limit
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="房间密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="如果房间有密码请输入"
            maxlength="20"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="加入方式">
          <el-radio-group v-model="form.asSpectator">
            <el-radio :label="false">加入游戏</el-radio>
            <el-radio :label="true">观战模式</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="handleJoinRoom"
            :loading="isLoading"
            native-type="submit"
          >
            <el-icon><Right /></el-icon>
            加入房间
          </el-button>
          <el-button size="large" @click="$router.push('/')">
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 最近房间 -->
      <div class="recent-rooms" v-if="recentRooms.length > 0">
        <h3>最近加入的房间</h3>
        <div class="room-list">
          <div
            v-for="room in recentRooms"
            :key="room.roomNumber"
            class="room-item"
            @click="joinRecentRoom(room)"
          >
            <div class="room-info">
              <div class="room-number">{{ room.roomNumber }}</div>
              <div class="room-name">{{ room.roomName }}</div>
            </div>
            <div class="room-meta">
              <span class="last-join">{{ formatTime(room.lastJoinTime) }}</span>
              <el-icon><Right /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, User, Lock, Right, ArrowLeft } from '@element-plus/icons-vue'
import { useRoomStore } from '@/stores/room'

const router = useRouter()
const roomStore = useRoomStore()
const formRef = ref()
const isLoading = ref(false)
const recentRooms = ref([])

// 表单数据
const form = reactive({
  roomNumber: '',
  playerName: '',
  playerId: '',
  password: '',
  asSpectator: false
})

// 表单验证规则
const rules = {
  roomNumber: [
    { required: true, message: '请输入房间号', trigger: 'blur' },
    { len: 6, message: '房间号必须是6位数字', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '房间号只能包含数字', trigger: 'blur' }
  ],
  playerName: [
    { required: true, message: '请输入您的昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

// 组件挂载时初始化
onMounted(() => {
  form.playerId = generateUserId()
  loadRecentRooms()
})

// 生成用户ID
const generateUserId = () => {
  return 'player_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// 房间号输入处理（只允许数字）
const handleRoomNumberInput = (value) => {
  form.roomNumber = value.replace(/\D/g, '').slice(0, 6)
}

// 加载最近房间
const loadRecentRooms = () => {
  const rooms = localStorage.getItem('recentRooms')
  if (rooms) {
    recentRooms.value = JSON.parse(rooms).slice(0, 5) // 只保留最近5个
  }
}

// 加入最近房间
const joinRecentRoom = (room) => {
  form.roomNumber = room.roomNumber
  form.password = room.password || ''
  form.asSpectator = room.asSpectator || false
}

// 保存房间到最近记录
const saveRecentRoom = (roomNumber, roomData) => {
  const newRoom = {
    roomNumber,
    roomName: roomData.roomName || '未知房间',
    password: form.password,
    asSpectator: form.asSpectator,
    lastJoinTime: new Date().toISOString()
  }

  // 去重并排序
  const updatedRooms = [newRoom, ...recentRooms.value.filter(r => r.roomNumber !== roomNumber)]
  recentRooms.value = updatedRooms.slice(0, 5)
  localStorage.setItem('recentRooms', JSON.stringify(recentRooms.value))
}

// 格式化时间
const formatTime = (timeStr) => {
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return time.toLocaleDateString()
  }
}

// 加入房间
const handleJoinRoom = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    isLoading.value = true
    roomStore.clearError()

    const playerInfo = {
      playerId: form.playerId,
      playerName: form.playerName,
      password: form.password,
      asSpectator: form.asSpectator,
      avatarUrl: '',
      deviceInfo: navigator.userAgent
    }

    const room = await roomStore.joinRoom(form.roomNumber, playerInfo)

    // 保存到最近房间
    saveRecentRoom(form.roomNumber, room.data)

    ElMessage.success('成功加入房间！')

    // 跳转到房间页面
    router.push(`/room/${form.roomNumber}`)
  } catch (error) {
    console.error('加入房间失败:', error)
    ElMessage.error(roomStore.error || '加入房间失败')
  } finally {
    isLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.join-room-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  box-sizing: border-box;
}

.form-card {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);

  h2 {
    text-align: center;
    margin-bottom: 2rem;
    color: #333;
    font-size: 1.8rem;
  }
}

.recent-rooms {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e4e7ed;

  h3 {
    color: #606266;
    font-size: 1.1rem;
    margin-bottom: 1rem;
  }

  .room-list {
    .room-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0.8rem;
      border-radius: 8px;
      background: #f5f7fa;
      margin-bottom: 0.5rem;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: #e4e7ed;
        transform: translateX(5px);
      }

      .room-info {
        .room-number {
          font-weight: bold;
          color: #409eff;
          font-size: 1.1rem;
        }

        .room-name {
          color: #606266;
          font-size: 0.9rem;
          margin-top: 0.2rem;
        }
      }

      .room-meta {
        display: flex;
        align-items: center;
        color: #909399;
        font-size: 0.8rem;

        .el-icon {
          margin-left: 0.5rem;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .join-room-container {
    padding: 1rem;
  }

  .form-card {
    padding: 1.5rem;
  }

  .room-item {
    .room-meta {
      flex-direction: column;
      align-items: flex-end;

      .last-join {
        margin-bottom: 0.2rem;
      }
    }
  }
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-button) {
  min-width: 120px;
}
</style>