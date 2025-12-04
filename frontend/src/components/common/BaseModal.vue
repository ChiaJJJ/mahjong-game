<template>
  <teleport to="body">
    <transition name="modal" appear>
      <div
        v-show="visible"
        :class="modalClasses"
        @click="handleMaskClick"
      >
        <div
          class="modal-content"
          :class="contentClasses"
          :style="contentStyle"
          @click.stop
        >
          <!-- 模态框头部 -->
          <div v-if="showHeader" class="modal-header">
            <div class="modal-title">
              <slot name="title">
                {{ title }}
              </slot>
            </div>
            <button
              v-if="showClose"
              class="modal-close"
              @click="handleClose"
              :aria-label="closeAriaLabel"
            >
              <el-icon><Close /></el-icon>
            </button>
          </div>

          <!-- 模态框内容 -->
          <div class="modal-body" :class="bodyClasses">
            <slot />
          </div>

          <!-- 模态框底部 -->
          <div v-if="showFooter" class="modal-footer">
            <slot name="footer">
              <el-button @click="handleCancel">
                {{ cancelText }}
              </el-button>
              <el-button
                type="primary"
                :loading="confirmLoading"
                @click="handleConfirm"
              >
                {{ confirmText }}
              </el-button>
            </slot>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { computed, watch, onMounted, onUnmounted } from 'vue'
import { Close } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps({
  // 是否显示
  visible: {
    type: Boolean,
    default: false
  },

  // 标题
  title: {
    type: String,
    default: ''
  },

  // 模态框宽度
  width: {
    type: [String, Number],
    default: '50%'
  },

  // 模态框高度
  height: {
    type: [String, Number],
    default: 'auto'
  },

  // 是否全屏
  fullscreen: {
    type: Boolean,
    default: false
  },

  // 是否显示关闭按钮
  showClose: {
    type: Boolean,
    default: true
  },

  // 是否显示头部
  showHeader: {
    type: Boolean,
    default: true
  },

  // 是否显示底部
  showFooter: {
    type: Boolean,
    default: false
  },

  // 点击遮罩是否关闭
  closeOnClickModal: {
    type: Boolean,
    default: true
  },

  // 按ESC键是否关闭
  closeOnPressEscape: {
    type: Boolean,
    default: true
  },

  // 是否显示遮罩
  showMask: {
    type: Boolean,
    default: true
  },

  // 遮罩透明度
  maskOpacity: {
    type: Number,
    default: 0.5
  },

  // 居中位置
  center: {
    type: Boolean,
    default: true
  },

  // 锁定滚动
  lockScroll: {
    type: Boolean,
    default: true
  },

  // 取消按钮文本
  cancelText: {
    type: String,
    default: '取消'
  },

  // 确认按钮文本
  confirmText: {
    type: String,
    default: '确定'
  },

  // 确认按钮加载状态
  confirmLoading: {
    type: Boolean,
    default: false
  },

  // 自定义类名
  customClass: {
    type: String,
    default: ''
  },

  // 动画类型
  animation: {
    type: String,
    default: 'fade',
    validator: (value) => ['fade', 'slide', 'zoom'].includes(value)
  }
})

// 定义事件
const emit = defineEmits([
  'update:visible',
  'close',
  'cancel',
  'confirm',
  'open',
  'opened',
  'close'
])

// 计算属性
const modalClasses = computed(() => {
  return [
    'modal-mask',
    {
      'modal-mask--no-mask': !props.showMask,
      'modal-mask--fullscreen': props.fullscreen
    },
    props.customClass
  ]
})

const contentClasses = computed(() => {
  return [
    'modal-content',
    `modal-content--${props.animation}`,
    {
      'modal-content--fullscreen': props.fullscreen,
      'modal-content--center': props.center
    }
  ]
})

const contentStyle = computed(() => {
  const style = {}

  if (!props.fullscreen) {
    if (props.width) {
      style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
    }

    if (props.height && props.height !== 'auto') {
      style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
    }
  }

  return style
})

const closeAriaLabel = computed(() => {
  return props.title ? `关闭${props.title}` : '关闭对话框'
})

// 键盘事件处理
const handleKeydown = (event) => {
  if (event.key === 'Escape' && props.visible && props.closeOnPressEscape) {
    handleClose()
  }
}

// 事件处理
const handleMaskClick = () => {
  if (props.closeOnClickModal) {
    handleClose()
  }
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const handleCancel = () => {
  emit('cancel')
  handleClose()
}

const handleConfirm = () => {
  emit('confirm')
}

// 生命周期钩子
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

// 监听visible变化
watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      emit('open')
      // 锁定滚动
      if (props.lockScroll) {
        document.body.style.overflow = 'hidden'
      }

      // 触发opened事件（在动画结束后）
      setTimeout(() => {
        emit('opened')
      }, 300)
    } else {
      // 恢复滚动
      if (props.lockScroll) {
        document.body.style.overflow = ''
      }
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
  box-sizing: border-box;

  &--no-mask {
    background-color: transparent;
  }

  &--fullscreen {
    padding: 0;
  }
}

.modal-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 90vw;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  &--center {
    margin: 0 auto;
  }

  &--fullscreen {
    width: 100vw;
    height: 100vh;
    max-width: 100vw;
    max-height: 100vh;
    border-radius: 0;
  }

  // 动画变体
  &--fade {
    animation: modal-fade-in 0.3s ease;
  }

  &--slide {
    animation: modal-slide-in 0.3s ease;
  }

  &--zoom {
    animation: modal-zoom-in 0.3s ease;
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.modal-close {
  background: none;
  border: none;
  font-size: 16px;
  color: #999;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background-color: #f5f5f5;
    color: #666;
  }

  &:focus {
    outline: none;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }
}

.modal-body {
  padding: 24px;
  flex: 1;
  overflow-y: auto;
  color: #666;
  line-height: 1.6;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

// 动画
@keyframes modal-fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes modal-slide-in {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes modal-zoom-in {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

// 过渡动画
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

// 响应式设计
@media (max-width: 768px) {
  .modal-mask {
    padding: 10px;
  }

  .modal-content {
    max-width: 95vw;
    margin: 0 auto;
  }

  .modal-header {
    padding: 16px 20px;
  }

  .modal-body {
    padding: 20px;
  }

  .modal-footer {
    padding: 12px 20px;
    flex-direction: column;

    :deep(.el-button) {
      width: 100%;
    }
  }
}

@media (max-width: 480px) {
  .modal-header {
    padding: 12px 16px;
  }

  .modal-body {
    padding: 16px;
  }

  .modal-footer {
    padding: 8px 16px;
  }

  .modal-title {
    font-size: 16px;
  }
}
</style>