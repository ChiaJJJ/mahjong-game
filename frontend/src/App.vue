<template>
  <div id="app" class="app-container">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'

// 页面加载时初始化
onMounted(() => {
  console.log('河南麻将游戏启动')
})

// 页面卸载时清理
onUnmounted(() => {
  console.log('河南麻将游戏结束')
})
</script>

<style lang="scss">
// 全局样式重置
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
  width: 100%;
}

.app-container {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
}

// 移动端适配
@media (max-width: 768px) {
  html {
    font-size: 14px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  html {
    font-size: 15px;
  }
}

@media (min-width: 1025px) {
  html {
    font-size: 16px;
  }
}

// 滚动条样式
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

// 禁用文本选择（游戏时）
.no-select {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

// 触摸设备优化
@media (hover: none) {
  * {
    -webkit-tap-highlight-color: transparent;
  }
}
</style>