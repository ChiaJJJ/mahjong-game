<template>
  <div class="chat-panel">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <h3>
        <el-icon><ChatDotRound /></el-icon>
        聊天
      </h3>
      <div class="chat-actions">
        <el-button
          v-if="showClearButton"
          type="text"
          size="small"
          @click="handleClearMessages"
        >
          清空
        </el-button>
        <el-button
          type="text"
          size="small"
          @click="toggleMinimize"
        >
          <el-icon>
            <Minus v-if="!minimized" />
            <Plus v-else />
          </el-icon>
        </el-button>
      </div>
    </div>

    <!-- 聊天内容 -->
    <div v-show="!minimized" class="chat-content">
      <!-- 消息列表 -->
      <div
        ref="messageList"
        class="message-list"
        @scroll="handleScroll"
      >
        <div
          v-for="message in messages"
          :key="message.id"
          :class="messageClasses(message)"
        >
          <!-- 时间戳 -->
          <div class="message-time">
            {{ formatTime(message.timestamp) }}
          </div>

          <!-- 发送者信息 -->
          <div class="message-sender">
            <el-avatar
              :size="24"
              :src="message.avatarUrl"
              :alt="message.sender"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="sender-name">{{ message.sender }}</span>
          </div>

          <!-- 消息内容 -->
          <div class="message-content">
            <div v-html="formatMessage(message.content)"></div>
          </div>
        </div>

        <!-- 滚动到底部按钮 -->
        <div
          v-if="showScrollToBottom"
          class="scroll-to-bottom"
          @click="scrollToBottom"
        >
          <el-icon><ArrowDown /></el-icon>
        </div>
      </div>

      <!-- 消息输入 -->
      <div class="message-input">
        <el-input
          v-model="newMessage"
          type="textarea"
          :rows="2"
          :maxlength="maxLength"
          show-word-limit
          placeholder="输入消息..."
          @keydown="handleKeyDown"
          resize="none"
        >
          <template #append>
            <el-button
              type="primary"
              :disabled="!canSend"
              @click="sendMessage"
            >
              发送
            </el-button>
          </template>
        </el-input>

        <!-- 快捷回复 -->
        <div v-if="showQuickReplies" class="quick-replies">
          <el-button
            v-for="(reply, index) in quickReplies"
            :key="index"
            size="small"
            type="text"
            @click="sendQuickReply(reply)"
          >
            {{ reply }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ChatDotRound, User, Minus, Plus, ArrowDown } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps({
  // 当前用户信息
  currentUser: {
    type: Object,
    default: () => ({})
  },

  // 房间号
  roomNumber: {
    type: String,
    default: ''
  },

  // 最大消息数量
  maxMessages: {
    type: Number,
    default: 100
  },

  // 是否显示清空按钮
  showClearButton: {
    type: Boolean,
    default: true
  },

  // 是否显示快捷回复
  showQuickReplies: {
    type: Boolean,
    default: true
  },

  // 消息最大长度
  maxLength: {
    type: Number,
    default: 100
  },

  // 自定义类名
  customClass: {
    type: String,
    default: ''
  },

  // 是否最小化
  minimized: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits(['message-sent', 'message-received', 'cleared', 'minimized'])

// 响应式数据
const messageList = ref(null)
const messages = ref([])
const newMessage = ref('')
const showScrollToBottom = ref(false)
const isScrolling = ref(false)
const scrollTimer = ref(null)

// 计算属性
const canSend = computed(() => {
  return newMessage.value.trim().length > 0 && newMessage.value.trim().length <= props.maxLength
})

const quickReplies = computed(() => {
  return [
    '👍',
    '😊',
    '🎉',
    '加油！',
    '不错',
    '呵呵',
    '好牌',
    '等等'
  ]
})

// 消息类名计算
const messageClasses = (message) => {
  const isOwn = message.senderId === props.currentUser?.id
  const isSystem = message.type === 'system'

  return [
    'message-item',
    {
      'message-item--own': isOwn,
      'message-item--system': isSystem,
      'message-item--spectator': message.type === 'spectator'
    }
  ]
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化消息内容（支持表情符号和换行）
const formatMessage = (content) => {
  return content
    .replace(/\n/g, '<br>')
    .replace(/:([\w+-]+):/g, (match, emojiName) => {
      const emojiMap = {
        'thumbsup': '👍',
        'smile': '😊',
        'laugh': '😂',
        'heart': '❤️',
        'fire': '🔥',
        'clap': '👏'
      }
      return emojiMap[emojiName] || match
    })
}

// 事件处理
const sendMessage = () => {
  if (!canSend.value) return

  const message = {
    id: Date.now(),
    sender: props.currentUser?.nickname || '匿名',
    senderId: props.currentUser?.id,
    content: newMessage.value.trim(),
    type: 'user',
    timestamp: new Date().toISOString(),
    avatarUrl: props.currentUser?.avatarUrl
  }

  messages.value.push(message)
  emit('message-sent', message)
  newMessage.value = ''

  nextTick(() => {
    scrollToBottom()
  })
}

const sendQuickReply = (reply) => {
  newMessage.value = reply
  sendMessage()
}

const handleKeyDown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const handleClearMessages = () => {
  messages.value = []
  emit('cleared')
}

const toggleMinimize = () => {
  emit('minimized', !props.minimized)
}

const scrollToBottom = () => {
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}

const handleScroll = () => {
  if (!messageList.value) return

  const { scrollTop, scrollHeight, clientHeight } = messageList.value
  const isAtBottom = scrollTop + clientHeight >= scrollHeight - 10

  if (!isAtBottom) {
    showScrollToBottom.value = true
  } else {
    showScrollToBottom.value = false
  }

  // 节流滚动检测
  isScrolling.value = true
  clearTimeout(scrollTimer.value)
  scrollTimer.value = setTimeout(() => {
    isScrolling.value = false
  }, 100)
}

// 添加系统消息
const addSystemMessage = (content) => {
  const message = {
    id: Date.now(),
    sender: '系统',
    content,
    type: 'system',
    timestamp: new Date().toISOString()
  }

  messages.value.push(message)
  nextTick(() => {
    scrollToBottom()
  })
}

// 监听消息变化
watch(messages, () => {
  // 限制消息数量
  if (messages.value.length > props.maxMessages) {
    messages.value = messages.value.slice(-props.maxMessages)
  }

  nextTick(() => {
    if (!isScrolling.value) {
      scrollToBottom()
    }
  })
})

// 生命周期钩子
onMounted(() => {
  nextTick(() => {
    scrollToBottom()
  })
})

onUnmounted(() => {
  clearTimeout(scrollTimer.value)
})

// 暴露方法给父组件
defineExpose({
  addSystemMessage,
  scrollToBottom,
  messages
})
</script>

<style lang="scss" scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  border-radius: 8px 8px 0 0;

  h3 {
    margin: 0;
    font-size: 14px;
    color: #333;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .chat-actions {
    display: flex;
    gap: 4px;
  }
}

.chat-content {
  display: flex;
  flex-direction: column;
  height: calc(100% - 45px);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  position: relative;

  // 自定义滚动条
  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 2px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 2px;

    &:hover {
      background: #a8a8a8;
    }
  }
}

.message-item {
  margin-bottom: 12px;
  padding: 8px;
  border-radius: 6px;
  background: #f8f9fa;

  &--own {
    background: #e3f2fd;
    margin-left: 20%;
    border-left: 3px solid #2196f3;
  }

  &--system {
    background: #fff3cd;
    text-align: center;
    border: 1px solid #ffeaa7;
    font-size: 12px;
    color: #856404;
    margin: 0 auto 8px;
    max-width: 80%;
  }

  &--spectator {
    background: #f8f9fa;
    opacity: 0.8;
    font-style: italic;
    border-left: 3px solid #6c757d;
  }
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
}

.message-sender {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;

  .sender-name {
    font-weight: 500;
    color: #333;
    font-size: 13px;
  }
}

.message-content {
  color: #333;
  line-height: 1.4;
  word-break: break-word;
  font-size: 14px;
}

.scroll-to-bottom {
  position: absolute;
  bottom: 20px;
  right: 20px;
  width: 32px;
  height: 32px;
  background: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;

  &:hover {
    background: #66b1ff;
    transform: scale(1.1);
  }
}

.message-input {
  border-top: 1px solid #e4e7ed;
  background: #fafafa;
}

.quick-replies {
  padding: 8px 12px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  border-bottom: 1px solid #e4e7ed;

  .el-button {
    font-size: 12px;
    height: 24px;
    padding: 0 8px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .chat-panel {
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .message-item {
    &--own {
      margin-left: 10px;
    }

    .message-content {
      font-size: 13px;
    }
  }

  .chat-header {
    h3 {
      font-size: 13px;
    }
  }

  .quick-replies {
    padding: 6px 8px;
    gap: 4px;
  }
}

// 动画
.message-item {
  animation: slideInUp 0.3s ease;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>