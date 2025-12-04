<template>
  <div class="create-room-container">
    <div class="form-card">
      <h2>创建房间</h2>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        size="large"
        @submit.prevent="handleCreateRoom"
      >
        <el-form-item label="房间名称" prop="roomName">
          <el-input
            v-model="form.roomName"
            placeholder="请输入房间名称"
            maxlength="20"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="您的昵称" prop="creatorNickname">
          <el-input
            v-model="form.creatorNickname"
            placeholder="请输入您的昵称"
            maxlength="20"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="房间密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="可选，设置密码保护房间"
            maxlength="20"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="最大玩家数" prop="maxPlayers">
          <el-radio-group v-model="form.maxPlayers">
            <el-radio :label="2">2人</el-radio>
            <el-radio :label="3">3人</el-radio>
            <el-radio :label="4">4人</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="游戏配置" prop="gameConfig">
          <div class="game-config-section">
            <div class="config-row">
              <label>基础分：</label>
              <el-input-number
                v-model="form.gameConfig.baseScore"
                :min="1"
                :max="10"
                size="small"
              />
            </div>
            <div class="config-row">
              <label>最大回合数：</label>
              <el-input-number
                v-model="form.gameConfig.maxRounds"
                :min="1"
                :max="16"
                size="small"
              />
            </div>
            <div class="config-row">
              <label>思考时间（秒）：</label>
              <el-input-number
                v-model="form.gameConfig.thinkTime"
                :min="5"
                :max="120"
                size="small"
              />
            </div>
            <div class="config-switches">
              <div class="switch-item">
                <el-switch v-model="form.gameConfig.allowPeng" />
                <span>允许碰牌</span>
              </div>
              <div class="switch-item">
                <el-switch v-model="form.gameConfig.allowGang" />
                <span>允许杠牌</span>
              </div>
              <div class="switch-item">
                <el-switch v-model="form.gameConfig.mixedTileEnabled" />
                <span>启用混牌（赖子）</span>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="房间设置">
          <div class="room-settings">
            <div class="setting-item">
              <el-switch v-model="form.isPublic" />
              <span>公开房间（其他人可以在房间列表中看到）</span>
            </div>
            <div class="setting-item">
              <el-switch v-model="form.allowSpectate" />
              <span>允许观战</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="handleCreateRoom"
            :loading="isLoading"
            native-type="submit"
          >
            创建房间
          </el-button>
          <el-button size="large" @click="$router.push('/')">
            返回首页
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useRoomStore } from '@/stores/room'

const router = useRouter()
const roomStore = useRoomStore()
const formRef = ref()
const isLoading = ref(false)

// 表单数据
const form = reactive({
  roomName: '',
  creatorNickname: '',
  creatorId: '',
  password: '',
  maxPlayers: 4,
  isPublic: true,
  allowSpectate: true,
  gameConfig: {
    baseScore: 1,
    maxRounds: 8,
    thinkTime: 30,
    allowPeng: true,
    allowGang: true,
    mixedTileEnabled: true
  }
})

// 表单验证规则
const rules = {
  roomName: [
    { required: true, message: '请输入房间名称', trigger: 'blur' },
    { min: 2, max: 20, message: '房间名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  creatorNickname: [
    { required: true, message: '请输入您的昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

// 组件挂载时生成用户ID
onMounted(() => {
  form.creatorId = generateUserId()
})

// 生成用户ID
const generateUserId = () => {
  return 'player_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// 创建房间
const handleCreateRoom = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    isLoading.value = true
    roomStore.clearError()

    const roomData = {
      roomName: form.roomName,
      creatorId: form.creatorId,
      creatorNickname: form.creatorNickname,
      password: form.password,
      maxPlayers: form.maxPlayers,
      isPublic: form.isPublic,
      allowSpectate: form.allowSpectate,
      gameConfig: form.gameConfig
    }

    const room = await roomStore.createRoom(roomData)
    ElMessage.success('房间创建成功！')

    // 跳转到房间页面
    router.push(`/room/${room.data.roomNumber}`)
  } catch (error) {
    console.error('创建房间失败:', error)
    ElMessage.error(roomStore.error || '创建房间失败')
  } finally {
    isLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.create-room-container {
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
  max-width: 600px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);

  h2 {
    text-align: center;
    margin-bottom: 2rem;
    color: #333;
    font-size: 1.8rem;
  }
}

.game-config-section {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 1rem;

  .config-row {
    display: flex;
    align-items: center;
    margin-bottom: 0.8rem;

    label {
      min-width: 120px;
      font-weight: 500;
      color: #606266;
    }
  }

  .config-switches {
    margin-top: 1rem;
    border-top: 1px solid #e4e7ed;
    padding-top: 1rem;

    .switch-item {
      display: flex;
      align-items: center;
      margin-bottom: 0.8rem;

      .el-switch {
        margin-right: 0.8rem;
      }

      span {
        color: #606266;
      }
    }
  }
}

.room-settings {
  .setting-item {
    display: flex;
    align-items: center;
    margin-bottom: 0.8rem;

    .el-switch {
      margin-right: 0.8rem;
    }

    span {
      color: #606266;
    }
  }
}

@media (max-width: 768px) {
  .create-room-container {
    padding: 1rem;
  }

  .form-card {
    padding: 1.5rem;
  }

  .game-config-section {
    .config-row {
      flex-direction: column;
      align-items: flex-start;

      label {
        margin-bottom: 0.5rem;
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