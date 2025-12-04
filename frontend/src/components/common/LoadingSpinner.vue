<template>
  <div
    :class="spinnerClasses"
    :style="spinnerStyle"
    role="progressbar"
    :aria-label="ariaLabel"
  >
    <!-- 默认加载器 -->
    <div v-if="type === 'default'" class="spinner-default">
      <div class="spinner-circle" v-for="i in 3" :key="i" :style="circleStyle(i)"></div>
    </div>

    <!-- 点状加载器 -->
    <div v-else-if="type === 'dots'" class="spinner-dots">
      <div class="dot" v-for="i in 3" :key="i" :style="dotStyle(i)"></div>
    </div>

    <!-- 脉冲加载器 -->
    <div v-else-if="type === 'pulse'" class="spinner-pulse">
      <div class="pulse-circle"></div>
    </div>

    <!-- 波浪加载器 -->
    <div v-else-if="type === 'wave'" class="spinner-wave">
      <div class="wave-bar" v-for="i in 5" :key="i" :style="waveStyle(i)"></div>
    </div>

    <!-- 自定义图标加载器 -->
    <div v-else-if="type === 'icon'" class="spinner-icon">
      <el-icon :size="iconSize" :color="color">
        <component :is="icon" />
      </el-icon>
    </div>

    <!-- 加载文本 -->
    <div v-if="text" :class="textClasses">
      {{ text }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// 定义组件属性
const props = defineProps({
  // 加载器类型
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'dots', 'pulse', 'wave', 'icon'].includes(value)
  },

  // 大小
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },

  // 颜色
  color: {
    type: String,
    default: '#409eff'
  },

  // 加载文本
  text: {
    type: String,
    default: ''
  },

  // 是否显示背景
  background: {
    type: Boolean,
    default: false
  },

  // 背景透明度
  backgroundOpacity: {
    type: Number,
    default: 0.8
  },

  // 是否全屏显示
  fullscreen: {
    type: Boolean,
    default: false
  },

  // 自定义尺寸
  width: {
    type: [String, Number],
    default: null
  },

  // 自定义图标
  icon: {
    type: [String, Object],
    default: 'Loading'
  },

  // 自定义类名
  customClass: {
    type: String,
    default: ''
  },

  // 延迟显示时间（毫秒）
  delay: {
    type: Number,
    default: 0
  }
})

// 计算属性
const spinnerClasses = computed(() => {
  return [
    'loading-spinner',
    `loading-spinner--${props.type}`,
    `loading-spinner--${props.size}`,
    {
      'loading-spinner--background': props.background,
      'loading-spinner--fullscreen': props.fullscreen
    },
    props.customClass
  ]
})

const textClasses = computed(() => {
  return [
    'loading-spinner__text',
    `loading-spinner__text--${props.size}`
  ]
})

const spinnerStyle = computed(() => {
  const style = {}

  if (props.width) {
    style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
    style.height = style.width
  }

  return style
})

const iconSize = computed(() => {
  const sizeMap = {
    small: 20,
    medium: 32,
    large: 48
  }
  return sizeMap[props.size] || 32
})

const circleStyle = (index) => {
  const delay = index * 0.2
  return {
    animationDelay: `${delay}s`
  }
}

const dotStyle = (index) => {
  const delay = index * 0.16
  return {
    animationDelay: `${delay}s`
  }
}

const waveStyle = (index) => {
  const delay = index * 0.1
  return {
    animationDelay: `${delay}s`
  }
}

const ariaLabel = computed(() => {
  return props.text ? '加载中' : '内容加载中'
})
</script>

<style lang="scss" scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  &--background {
    background-color: rgba(255, 255, 255, v-bind(backgroundOpacity));
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  &--fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, v-bind(backgroundOpacity));
    z-index: 9999;
  }

  // 大小样式
  &--small {
    .spinner-default {
      width: 24px;
      height: 24px;

      .spinner-circle {
        width: 6px;
        height: 6px;
        margin: 0 1px;
      }
    }

    .spinner-dots {
      .dot {
        width: 4px;
        height: 4px;
        margin: 0 2px;
      }
    }

    .spinner-pulse {
      .pulse-circle {
        width: 20px;
        height: 20px;
      }
    }

    .spinner-wave {
      .wave-bar {
        width: 2px;
        height: 16px;
        margin: 0 1px;
      }
    }
  }

  &--medium {
    .spinner-default {
      width: 40px;
      height: 40px;

      .spinner-circle {
        width: 10px;
        height: 10px;
        margin: 0 2px;
      }
    }

    .spinner-dots {
      .dot {
        width: 8px;
        height: 8px;
        margin: 0 3px;
      }
    }

    .spinner-pulse {
      .pulse-circle {
        width: 32px;
        height: 32px;
      }
    }

    .spinner-wave {
      .wave-bar {
        width: 3px;
        height: 24px;
        margin: 0 2px;
      }
    }
  }

  &--large {
    .spinner-default {
      width: 56px;
      height: 56px;

      .spinner-circle {
        width: 14px;
        height: 14px;
        margin: 0 3px;
      }
    }

    .spinner-dots {
      .dot {
        width: 12px;
        height: 12px;
        margin: 0 4px;
      }
    }

    .spinner-pulse {
      .pulse-circle {
        width: 48px;
        height: 48px;
      }
    }

    .spinner-wave {
      .wave-bar {
        width: 4px;
        height: 32px;
        margin: 0 3px;
      }
    }
  }

  // 默认加载器
  .spinner-default {
    display: flex;
    align-items: center;

    .spinner-circle {
      background-color: v-bind(color);
      border-radius: 50%;
      animation: circle-scale 1.4s ease-in-out infinite both;
    }
  }

  // 点状加载器
  .spinner-dots {
    display: flex;
    align-items: center;

    .dot {
      background-color: v-bind(color);
      border-radius: 50%;
      animation: dot-scale 1.4s ease-in-out infinite both;
    }
  }

  // 脉冲加载器
  .spinner-pulse {
    .pulse-circle {
      background-color: v-bind(color);
      border-radius: 50%;
      animation: pulse-scale 1.4s ease-in-out infinite both;
    }
  }

  // 波浪加载器
  .spinner-wave {
    display: flex;
    align-items: flex-end;

    .wave-bar {
      background-color: v-bind(color);
      border-radius: 2px;
      animation: wave-scale 1.4s ease-in-out infinite both;
    }
  }

  // 图标加载器
  .spinner-icon {
    animation: icon-rotate 1s linear infinite;
  }

  // 文本样式
  &__text {
    margin-top: 12px;
    color: #666;
    font-size: 14px;
    text-align: center;

    &--small {
      font-size: 12px;
      margin-top: 8px;
    }

    &--medium {
      font-size: 14px;
      margin-top: 12px;
    }

    &--large {
      font-size: 16px;
      margin-top: 16px;
    }
  }
}

// 动画定义
@keyframes circle-scale {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes dot-scale {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes pulse-scale {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

@keyframes wave-scale {
  0%, 40%, 100% {
    transform: scaleY(0.4);
  }
  20% {
    transform: scaleY(1);
  }
}

@keyframes icon-rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .loading-spinner {
    &__text {
      font-size: 13px;
    }
  }
}
</style>