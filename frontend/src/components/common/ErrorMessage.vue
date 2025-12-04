<template>
  <div
    v-if="visible"
    :class="errorClasses"
    role="alert"
    :aria-label="ariaLabel"
  >
    <!-- 错误图标 -->
    <div class="error-icon">
      <el-icon :size="iconSize">
        <component :is="icon" />
      </el-icon>
    </div>

    <!-- 错误内容 -->
    <div class="error-content">
      <div v-if="title" class="error-title">
        {{ title }}
      </div>
      <div class="error-message">
        {{ message }}
      </div>

      <!-- 错误详情 -->
      <div v-if="showDetails && details" class="error-details">
        <el-button
          type="text"
          size="small"
          @click="toggleDetails"
          class="details-toggle"
        >
          {{ detailsVisible ? '隐藏详情' : '显示详情' }}
          <el-icon>
            <ArrowDown v-if="!detailsVisible" />
            <ArrowUp v-else />
          </el-icon>
        </el-button>

        <div v-show="detailsVisible" class="details-content">
          <pre>{{ details }}</pre>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div v-if="showActions" class="error-actions">
        <el-button
          v-if="showRetry"
          type="primary"
          size="small"
          @click="handleRetry"
        >
          重试
        </el-button>
        <el-button
          v-if="showClose"
          type="text"
          size="small"
          @click="handleClose"
        >
          关闭
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Warning,
  CircleCloseFilled,
  ArrowDown,
  ArrowUp
} from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps({
  // 错误消息
  message: {
    type: String,
    required: true
  },

  // 错误标题
  title: {
    type: String,
    default: ''
  },

  // 错误类型
  type: {
    type: String,
    default: 'error',
    validator: (value) => ['error', 'warning', 'info', 'success'].includes(value)
  },

  // 错误详情
  details: {
    type: String,
    default: ''
  },

  // 是否显示详情切换
  showDetails: {
    type: Boolean,
    default: false
  },

  // 是否显示重试按钮
  showRetry: {
    type: Boolean,
    default: false
  },

  // 是否显示关闭按钮
  showClose: {
    type: Boolean,
    default: true
  },

  // 是否显示操作按钮
  showActions: {
    type: Boolean,
    default: true
  },

  // 是否自动关闭（毫秒）
  autoClose: {
    type: Number,
    default: 0
  },

  // 自定义图标
  icon: {
    type: [String, Object],
    default: null
  },

  // 自定义类名
  customClass: {
    type: String,
    default: ''
  },

  // 是否显示
  visible: {
    type: Boolean,
    default: true
  },

  // 错误代码
  code: {
    type: [String, Number],
    default: null
  },

  // 错误来源
  source: {
    type: String,
    default: ''
  }
})

// 定义事件
const emit = defineEmits(['retry', 'close', 'show', 'hide'])

// 响应式数据
const detailsVisible = ref(false)

// 计算属性
const errorClasses = computed(() => {
  return [
    'error-message',
    `error-message--${props.type}`,
    {
      'error-message--has-title': props.title,
      'error-message--has-details': props.details,
      'error-message--has-actions': props.showActions
    },
    props.customClass
  ]
})

const iconSize = computed(() => {
  return props.title ? 24 : 16
})

const displayIcon = computed(() => {
  if (props.icon) {
    return props.icon
  }

  const iconMap = {
    error: CircleCloseFilled,
    warning: Warning,
    info: Warning,
    success: Warning
  }

  return iconMap[props.type] || CircleCloseFilled
})

const ariaLabel = computed(() => {
  const typeText = {
    error: '错误',
    warning: '警告',
    info: '信息',
    success: '成功'
  }

  return `${typeText[props.type]}: ${props.message}`
})

// 方法
const toggleDetails = () => {
  detailsVisible.value = !detailsVisible.value
}

const handleRetry = () => {
  emit('retry')
}

const handleClose = () => {
  emit('close')
}

// 自动关闭逻辑
if (props.autoClose > 0) {
  setTimeout(() => {
    emit('close')
  }, props.autoClose)
}

// 监听visible变化
const handleVisibilityChange = (visible) => {
  if (visible) {
    emit('show')
    if (props.code) {
      console.error(`[${props.type.toUpperCase()}] ${props.code}: ${props.message}`, {
        details: props.details,
        source: props.source
      })
    }
  } else {
    emit('hide')
    detailsVisible.value = false
  }
}

// 暴露给模板的方法
defineExpose({
  toggleDetails,
  handleRetry,
  handleClose
})
</script>

<style lang="scss" scoped>
.error-message {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 8px;
  border: 1px solid;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

  &--error {
    border-color: #f56c6c;
    background-color: #fef0f0;
    color: #f56c6c;
  }

  &--warning {
    border-color: #e6a23c;
    background-color: #fdf6ec;
    color: #e6a23c;
  }

  &--info {
    border-color: #909399;
    background-color: #f4f4f5;
    color: #909399;
  }

  &--success {
    border-color: #67c23a;
    background-color: #f0f9ff;
    color: #67c23a;
  }

  &--has-title {
    align-items: flex-start;
  }

  &--has-details {
    padding-bottom: 8px;
  }

  &--has-actions {
    padding-bottom: 8px;
  }
}

.error-icon {
  flex-shrink: 0;
  margin-right: 12px;
  margin-top: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-content {
  flex: 1;
  min-width: 0;
}

.error-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
  line-height: 1.4;
}

.error-message {
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.error-details {
  margin-top: 8px;

  .details-toggle {
    padding: 0;
    height: auto;
    font-size: 12px;
    color: inherit;
    display: inline-flex;
    align-items: center;
    gap: 4px;

    &:hover {
      background-color: transparent;
    }

    .el-icon {
      transition: transform 0.3s ease;
    }
  }

  .details-content {
    margin-top: 8px;
    padding: 8px;
    background-color: rgba(0, 0, 0, 0.05);
    border-radius: 4px;
    font-size: 12px;
    line-height: 1.4;
    overflow-x: auto;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

.error-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

// 响应式设计
@media (max-width: 768px) {
  .error-message {
    padding: 10px 12px;

    .error-icon {
      margin-right: 10px;
    }

    .error-title {
      font-size: 13px;
    }

    .error-message {
      font-size: 12px;
    }

    .error-actions {
      flex-direction: column;
      gap: 6px;

      .el-button {
        width: 100%;
      }
    }
  }
}

// 动画
.error-message-enter-active,
.error-message-leave-active {
  transition: all 0.3s ease;
}

.error-message-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.error-message-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 可访问性
.error-message:focus {
  outline: 2px solid currentColor;
  outline-offset: 2px;
}
</style>