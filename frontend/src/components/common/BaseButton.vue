<template>
  <button
    :class="buttonClasses"
    :disabled="disabled || loading"
    @click="handleClick"
    v-bind="$attrs"
  >
    <!-- 加载状态 -->
    <el-icon v-if="loading" class="loading-icon">
      <Loading />
    </el-icon>

    <!-- 图标 -->
    <el-icon v-if="icon && !loading" :class="iconClass">
      <component :is="icon" />
    </el-icon>

    <!-- 插槽内容 -->
    <span v-if="$slots.default" :class="contentClass">
      <slot />
    </span>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps({
  // 按钮类型
  type: {
    type: String,
    default: 'primary',
    validator: (value) => [
      'primary', 'secondary', 'success', 'warning', 'danger', 'info', 'text'
    ].includes(value)
  },

  // 按钮大小
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },

  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },

  // 是否加载中
  loading: {
    type: Boolean,
    default: false
  },

  // 是否块级按钮
  block: {
    type: Boolean,
    default: false
  },

  // 是否圆角
  round: {
    type: Boolean,
    default: false
  },

  // 是否圆形
  circle: {
    type: Boolean,
    default: false
  },

  // 图标
  icon: {
    type: [String, Object],
    default: null
  },

  // 图标位置
  iconPosition: {
    type: String,
    default: 'left',
    validator: (value) => ['left', 'right'].includes(value)
  },

  // 按钮变体
  variant: {
    type: String,
    default: 'solid',
    validator: (value) => ['solid', 'outline', 'text', 'link'].includes(value)
  }
})

// 定义事件
const emit = defineEmits(['click'])

// 计算属性
const buttonClasses = computed(() => {
  const classes = [
    'base-button',
    `base-button--${props.type}`,
    `base-button--${props.size}`,
    `base-button--${props.variant}`
  ]

  if (props.disabled) {
    classes.push('base-button--disabled')
  }

  if (props.loading) {
    classes.push('base-button--loading')
  }

  if (props.block) {
    classes.push('base-button--block')
  }

  if (props.round) {
    classes.push('base-button--round')
  }

  if (props.circle) {
    classes.push('base-button--circle')
  }

  return classes
})

const iconClass = computed(() => {
  const classes = ['base-button__icon']

  if (props.iconPosition === 'right') {
    classes.push('base-button__icon--right')
  }

  return classes
})

const contentClass = computed(() => {
  return [
    'base-button__content',
    {
      'base-button__content--no-icon': !props.icon,
      'base-button__content--icon-left': props.icon && props.iconPosition === 'left',
      'base-button__content--icon-right': props.icon && props.iconPosition === 'right'
    }
  ]
})

// 事件处理
const handleClick = (event) => {
  if (props.disabled || props.loading) {
    return
  }
  emit('click', event)
}
</script>

<style lang="scss" scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  outline: none;
  position: relative;
  overflow: hidden;
  user-select: none;

  &:hover:not(.base-button--disabled):not(.base-button--loading) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  &:active:not(.base-button--disabled):not(.base-button--loading) {
    transform: translateY(0);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  }

  &--primary {
    background-color: #409eff;
    color: white;

    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background-color: #66b1ff;
    }
  }

  &--secondary {
    background-color: #909399;
    color: white;

    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background-color: #a6a9ad;
    }
  }

  &--success {
    background-color: #67c23a;
    color: white;

    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background-color: #85ce61;
    }
  }

  &--warning {
    background-color: #e6a23c;
    color: white;

    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background-color: #ebb563;
    }
  }

  &--danger {
    background-color: #f56c6c;
    color: white;

    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background-color: #f78989;
    }
  }

  &--info {
    background-color: #909399;
    color: white;

    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background-color: #a6a9ad;
    }
  }

  &--text {
    background-color: transparent;
    color: #409eff;

    &:hover:not(.base-button--disabled):not(.base-button--loading) {
      background-color: rgba(64, 158, 255, 0.1);
    }
  }

  // 变体样式
  &--outline {
    background-color: transparent;
    border: 2px solid;

    &.base-button--primary {
      color: #409eff;
      border-color: #409eff;

      &:hover:not(.base-button--disabled):not(.base-button--loading) {
        background-color: #409eff;
        color: white;
      }
    }

    &.base-button--success {
      color: #67c23a;
      border-color: #67c23a;

      &:hover:not(.base-button--disabled):not(.base-button--loading) {
        background-color: #67c23a;
        color: white;
      }
    }
  }

  // 大小样式
  &--small {
    padding: 8px 16px;
    font-size: 12px;
    min-height: 32px;
  }

  &--medium {
    padding: 10px 20px;
    font-size: 14px;
    min-height: 36px;
  }

  &--large {
    padding: 12px 24px;
    font-size: 16px;
    min-height: 40px;
  }

  // 状态样式
  &--disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none !important;
    box-shadow: none !important;
  }

  &--loading {
    cursor: wait;
  }

  // 布局样式
  &--block {
    width: 100%;
    display: flex;
  }

  &--round {
    border-radius: 20px;
  }

  &--circle {
    border-radius: 50%;
    width: auto;
    height: auto;
    min-width: auto;
    min-height: auto;
    padding: 0;

    &.base-button--small {
      width: 32px;
      height: 32px;
    }

    &.base-button--medium {
      width: 36px;
      height: 36px;
    }

    &.base-button--large {
      width: 40px;
      height: 40px;
    }
  }

  // 子元素样式
  &__icon {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;

    &--right {
      order: 2;
    }

    .loading-icon {
      animation: spin 1s linear infinite;
    }
  }

  &__content {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;

    &--no-icon {
      margin: 0;
    }

    &--icon-left {
      order: 2;
    }

    &--icon-right {
      order: 1;
    }
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .base-button {
    font-size: 14px;

    &--small {
      font-size: 12px;
    }

    &--large {
      font-size: 14px;
    }
  }
}
</style>