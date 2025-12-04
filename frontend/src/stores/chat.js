import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { chatApi, chatEvents, chatUtils } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  // 状态定义
  const messages = ref([])
  const isLoading = ref(false)
  const error = ref(null)
  const isConnected = ref(false)
  const currentRoomId = ref('')
  const messageHistory = ref(new Map()) // 存储不同房间的消息历史

  // 计算属性
  const currentMessages = computed(() => {
    if (!currentRoomId.value) return []
    return messageHistory.value.get(currentRoomId.value) || []
  })

  const unreadCount = computed(() => {
    return currentMessages.value.filter(msg => !msg.isRead && msg.senderId !== getCurrentUserId()).length
  })

  const lastMessage = computed(() => {
    const msgs = currentMessages.value
    return msgs.length > 0 ? msgs[msgs.length - 1] : null
  })

  // 消息操作
  const sendMessage = async (content) => {
    try {
      // 验证消息内容
      const validation = chatUtils.validateMessage(content)
      if (!validation.valid) {
        throw new Error(validation.message)
      }

      // 过滤敏感词
      const filteredContent = chatUtils.filterSensitiveWords(content)

      isLoading.value = true

      // 通过WebSocket发送消息
      chatEvents.sendChatMessage({
        roomId: currentRoomId.value,
        content: filteredContent
      })

      // 同时通过HTTP API发送（作为备用）
      await chatApi.sendMessage({
        userId: getCurrentUserId(),
        roomId: currentRoomId.value,
        content: filteredContent
      })

      return { success: true }
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const sendSystemMessage = async (content) => {
    try {
      await chatApi.sendSystemMessage({
        roomId: currentRoomId.value,
        content
      })
    } catch (err) {
      error.value = err.message
      throw err
    }
  }

  // 消息接收处理
  const handleMessage = (messageData) => {
    const message = {
      id: messageData.id || generateMessageId(),
      senderId: messageData.senderId,
      sender: messageData.sender || '未知用户',
      avatarUrl: messageData.avatarUrl || '',
      content: messageData.content,
      timestamp: messageData.timestamp || Date.now(),
      type: messageData.type || 'user',
      isRead: false,
      messageType: chatUtils.detectMessageType(messageData.content)
    }

    // 添加到对应房间的消息历史
    const roomId = currentRoomId.value
    if (roomId) {
      if (!messageHistory.value.has(roomId)) {
        messageHistory.value.set(roomId, [])
      }

      const roomMessages = messageHistory.value.get(roomId)
      roomMessages.push(message)

      // 限制消息数量，保留最新的100条
      if (roomMessages.length > 100) {
        roomMessages.splice(0, roomMessages.length - 100)
      }

      // 更新全局消息列表（兼容性）
      messages.value = roomMessages
    }
  }

  // 房间操作
  const joinRoom = (roomId) => {
    currentRoomId.value = roomId

    // 如果还没有该房间的消息历史，创建一个
    if (!messageHistory.value.has(roomId)) {
      messageHistory.value.set(roomId, [])
      messages.value = []
    } else {
      messages.value = messageHistory.value.get(roomId)
    }

    // 发送加入房间事件
    chatEvents.joinRoom(roomId)

    // 添加系统消息
    addSystemMessage(`已加入房间 ${roomId}`)
  }

  const leaveRoom = () => {
    if (currentRoomId.value) {
      chatEvents.leaveRoom(currentRoomId.value)
      addSystemMessage(`已离开房间 ${currentRoomId.value}`)
      currentRoomId.value = ''
    }
  }

  // 系统消息
  const addSystemMessage = (content) => {
    const message = {
      id: generateMessageId(),
      senderId: 'system',
      sender: '系统',
      avatarUrl: '',
      content,
      timestamp: Date.now(),
      type: 'system',
      isRead: true,
      messageType: 'system'
    }

    handleMessage(message)
  }

  // 消息管理
  const markAsRead = (messageId) => {
    const msg = currentMessages.value.find(m => m.id === messageId)
    if (msg) {
      msg.isRead = true
    }
  }

  const markAllAsRead = () => {
    currentMessages.value.forEach(msg => {
      if (msg.senderId !== getCurrentUserId()) {
        msg.isRead = true
      }
    })
  }

  const clearMessages = () => {
    if (currentRoomId.value) {
      messageHistory.value.set(currentRoomId.value, [])
      messages.value = []
    }
  }

  const clearHistory = (roomId) => {
    if (roomId) {
      messageHistory.value.delete(roomId)
      if (currentRoomId.value === roomId) {
        messages.value = []
      }
    }
  }

  // 消息搜索
  const searchMessages = (keyword) => {
    if (!keyword.trim()) {
      return currentMessages.value
    }

    return currentMessages.value.filter(msg =>
      msg.content.toLowerCase().includes(keyword.toLowerCase())
    )
  }

  // 获取消息统计
  const getMessageStats = () => {
    const roomMessages = currentMessages.value
    const userMessages = roomMessages.filter(msg => msg.type === 'user')
    const systemMessages = roomMessages.filter(msg => msg.type === 'system')

    return {
      total: roomMessages.length,
      user: userMessages.length,
      system: systemMessages.length,
      unread: unreadCount.value
    }
  }

  // 工具函数
  const generateMessageId = () => {
    return Date.now().toString(36) + Math.random().toString(36).substr(2)
  }

  const getCurrentUserId = () => {
    // 这里应该从用户store或其他地方获取当前用户ID
    return localStorage.getItem('userId') || 'guest'
  }

  // 初始化WebSocket事件监听
  const initializeWebSocket = () => {
    // 监听聊天消息
    chatEvents.onChatMessage((data) => {
      handleMessage(data)
    })

    // 监听连接状态变化
    isConnected.value = true
  }

  // 重置状态
  const reset = () => {
    messages.value = []
    messageHistory.value.clear()
    currentRoomId.value = ''
    isLoading.value = false
    error.value = null
    isConnected.value = false
  }

  return {
    // 状态
    messages,
    isLoading,
    error,
    isConnected,
    currentRoomId,
    currentMessages,
    unreadCount,
    lastMessage,

    // 操作方法
    sendMessage,
    sendSystemMessage,
    joinRoom,
    leaveRoom,
    addSystemMessage,
    markAsRead,
    markAllAsRead,
    clearMessages,
    clearHistory,
    searchMessages,
    getMessageStats,
    initializeWebSocket,
    reset
  }
})