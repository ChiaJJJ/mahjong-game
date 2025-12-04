<template>
  <div
    :class="cardClasses"
    :style="cardStyle"
  >
    <!-- 玩家头像 -->
    <div class="player-avatar">
      <el-avatar
        :size="avatarSize"
        :src="player.avatarUrl"
        :alt="player.nickname"
      >
        <el-icon :size="iconSize">
          <User />
        </el-icon>
      </el-avatar>

      <!-- 状态指示器 -->
      <div :class="statusIndicatorClasses"></div>
    </div>

    <!-- 玩家信息 -->
    <div class="player-info">
      <div class="player-name">
        {{ player.nickname }}
        <el-tag
          v-if="player.isCreator"
          type="primary"
          size="small"
          effect="dark"
        >
          房主
        </el-tag>
      </div>

      <div class="player-status">
        <!-- 在线状态 -->
        <el-tag
          :type="player.isOnline ? 'success' : 'danger'"
          size="small"
          effect="light"
        >
          {{ player.isOnline ? '在线' : '离线' }}
        </el-tag>

        <!-- 准备状态 -->
        <el-tag
          v-if="showReadyStatus && !player.isSpectator"
          :type="player.isReady ? 'success' : 'warning'"
          size="small"
          effect="light"
        >
          {{ player.isReady ? '已准备' : '未准备' }}
        </el-tag>

        <!-- 观战者标识 -->
        <el-tag
          v-if="player.isSpectator"
          type="info"
          size="small"
          effect="light"
        >
          观战
        </el-tag>

        <!-- 当前回合指示器 -->
        <div v-if="isCurrentTurn" class="current-turn">
          <el-icon size="12">
            <CaretRight />
          </el-icon>
        </div>
      </div>
    </div>

    <!-- 玩家分数 -->
    <div v-if="showScore" class="player-score">
      <div class="score-label">分数</div>
      <div :class="scoreClasses">
        {{ player.totalScore || 0 }}
      </div>
    </div>

    <!-- 位置指示器 -->
    <div v-if="showPosition" class="player-position">
      位置 {{ player.position + 1 }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { User, CaretRight } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps({
  // 玩家数据
  player: {
    type: Object,
    required: true
  },

  // 位置
  position: {
    type: String,
    default: 'bottom',
    validator: (value) => ['top', 'bottom', 'left', 'right'].includes(value)
  },

  // 是否为当前玩家
  isCurrentPlayer: {
    type: Boolean,
    default: false
  },

  // 是否显示准备状态
  showReadyStatus: {
    type: Boolean,
    default: true
  },

  // 是否显示分数
  showScore: {
    type: Boolean,
    default: true
  },

  // 是否显示位置
  showPosition: {
    type: Boolean,
    default: false
  },

  // 是否高亮
  highlight: {
    type: Boolean,
    default: false
  },

  // 是否为当前回合
  isCurrentTurn: {
    type: Boolean,
    default: false
  },

  // 尺寸
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },

  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// 计算属性
const cardClasses = computed(() => {
  return [
    'player-card',
    `player-card--${props.position}`,
    `player-card--${props.size}`,
    {
      'player-card--current': props.isCurrentPlayer,
      'player-card--highlight': props.highlight,
      'player-card--current-turn': props.isCurrentTurn,
      'player-card--offline': !props.player.isOnline,
      'player-card--spectator': props.player.isSpectator
    },
    props.customClass
  ]
})

const cardStyle = computed(() => {
  const style = {}

  // 根据位置调整样式
  if (props.position === 'top' || props.position === 'bottom') {
    style.flexDirection = 'column'
  } else {
    style.flexDirection = 'row'
  }

  return style
})

const avatarSize = computed(() => {
  const sizeMap = {
    small: 32,
    medium: 48,
    large: 64
  }
  return sizeMap[props.size] || 48
})

const iconSize = computed(() => {
  const sizeMap = {
    small: 16,
    medium: 24,
    large: 32
  }
  return sizeMap[props.size] || 24
})

const statusIndicatorClasses = computed(() => {
  return [
    'status-indicator',
    {
      'status-indicator--online': props.player.isOnline,
      'status-indicator--offline': !props.player.isOnline,
      'status-indicator--ready': props.player.isReady && !props.player.isSpectator,
      'status-indicator--spectator': props.player.isSpectator
    }
  ]
})

const scoreClasses = computed(() => {
  return [
    'score-value',
    {
      'score-value--positive': (props.player.totalScore || 0) > 0,
      'score-value--negative': (props.player.totalScore || 0) < 0,
      'score-value--zero': (props.player.totalScore || 0) === 0
    }
  ]
})
</script>

<style lang="scss" scoped>
.player-card {
  display: flex;
  align-items: center;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
  }

  &--current {
    border-color: #409eff;
    background: rgba(64, 158, 255, 0.1);
  }

  &--highlight {
    box-shadow: 0 0 12px rgba(64, 158, 255, 0.3);
  }

  &--current-turn {
    animation: pulse 2s infinite;
  }

  &--offline {
    opacity: 0.6;
  }

  &--spectator {
    border-style: dashed;
    opacity: 0.8;
  }

  // 位置相关样式
  &--top,
  &--bottom {
    flex-direction: column;
    text-align: center;
    gap: 8px;

    .player-avatar {
      margin: 0 auto;
    }

    .player-info {
      order: 2;
    }

    .player-score {
      order: 3;
    }
  }

  &--left,
  &--right {
    flex-direction: row;
    gap: 12px;

    .player-avatar {
      order: 1;
    }

    .player-info {
      order: 2;
      flex: 1;
    }

    .player-score {
      order: 3;
    }
  }
}

.player-avatar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  .status-indicator {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    border: 2px solid white;

    &--online {
      background-color: #67c23a;
    }

    &--offline {
      background-color: #f56c6c;
    }

    &--ready {
      background-color: #409eff;
    }

    &--spectator {
      background-color: #909399;
    }
  }
}

.player-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: white;
  min-width: 0;

  .player-name {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-weight: 500;
    font-size: 14px;

    .el-tag {
      font-size: 10px;
      padding: 1px 4px;
      height: 16px;
    }
  }

  .player-status {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    flex-wrap: wrap;

    .el-tag {
      font-size: 10px;
      padding: 1px 4px;
      height: 16px;
    }

    .current-turn {
      color: #f39c12;
      animation: blink 1s infinite;
    }
  }
}

.player-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
  font-weight: 600;

  .score-label {
    font-size: 10px;
    opacity: 0.8;
    margin-bottom: 2px;
  }

  .score-value {
    font-size: 16px;

    &--positive {
      color: #67c23a;
    }

    &--negative {
      color: #f56c6c;
    }

    &--zero {
      color: #ecf0f1;
    }
  }
}

.player-position {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

// 动画
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(64, 158, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0);
  }
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .player-card {
    padding: 8px;

    &--top,
    &--bottom {
      gap: 6px;
    }

    &--left,
    &--right {
      gap: 8px;
    }
  }

  .player-info {
    .player-name {
      font-size: 12px;
    }
  }

  .player-score {
    .score-label {
      font-size: 9px;
    }

    .score-value {
      font-size: 14px;
    }
  }

  .player-position {
    font-size: 11px;
  }
}
</style>