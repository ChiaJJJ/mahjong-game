import { apiClient } from './client'

/**
 * 聊天API接口
 */
export const chatApi = {
  /**
   * 发送聊天消息
   * @param {Object} params 参数
   * @param {number} params.userId 用户ID
   * @param {string} params.roomId 房间号
   * @param {string} params.content 消息内容
   * @returns {Promise} 发送结果
   */
  sendMessage: (params) => {
    return apiClient.post('/chat/message', params)
  },

  /**
   * 发送系统消息
   * @param {Object} params 参数
   * @param {string} params.roomId 房间号
   * @param {string} params.content 消息内容
   * @returns {Promise} 发送结果
   */
  sendSystemMessage: (params) => {
    return apiClient.post('/chat/system/message', params)
  },

  /**
   * 广播系统消息
   * @param {Object} params 参数
   * @param {string} params.content 消息内容
   * @returns {Promise} 发送结果
   */
  broadcastSystemMessage: (params) => {
    return apiClient.post('/chat/system/broadcast', params)
  }
}

/**
 * WebSocket聊天事件
 */
export const chatEvents = {
  /**
   * 监听聊天消息
   * @param {Function} callback 回调函数
   */
  onChatMessage: (callback) => {
    apiClient.on('chat_message', callback)
  },

  /**
   * 发送聊天消息到WebSocket
   * @param {Object} params 参数
   * @param {string} params.roomId 房间号
   * @param {string} params.content 消息内容
   */
  sendChatMessage: (params) => {
    apiClient.emit('chat_message', params)
  },

  /**
   * 加入房间聊天频道
   * @param {string} roomId 房间号
   */
  joinRoom: (roomId) => {
    apiClient.emit('join_room', { roomId })
  },

  /**
   * 离开房间聊天频道
   * @param {string} roomId 房间号
   */
  leaveRoom: (roomId) => {
    apiClient.emit('leave_room', { roomId })
  }
}

/**
 * 聊天消息工具函数
 */
export const chatUtils = {
  /**
   * 验证消息内容
   * @param {string} content 消息内容
   * @returns {Object} 验证结果
   */
  validateMessage: (content) => {
    if (!content || !content.trim()) {
      return { valid: false, message: '消息内容不能为空' }
    }

    if (content.length > 100) {
      return { valid: false, message: '消息长度不能超过100个字符' }
    }

    // 检查是否包含敏感词
    const sensitiveWords = ['政治', '色情', '暴力', '赌博', '毒品']
    const hasSensitiveWord = sensitiveWords.some(word =>
      content.toLowerCase().includes(word.toLowerCase())
    )

    if (hasSensitiveWord) {
      return { valid: false, message: '消息包含敏感词汇' }
    }

    return { valid: true }
  },

  /**
   * 过滤敏感词
   * @param {string} content 原始内容
   * @returns {string} 过滤后的内容
   */
  filterSensitiveWords: (content) => {
    const sensitiveWords = ['政治', '色情', '暴力', '赌博', '毒品']
    let filtered = content

    sensitiveWords.forEach(word => {
      const regex = new RegExp(word, 'gi')
      filtered = filtered.replace(regex, '***')
    })

    return filtered
  },

  /**
   * 格式化时间戳
   * @param {number} timestamp 时间戳
   * @returns {string} 格式化时间
   */
  formatTime: (timestamp) => {
    const date = new Date(timestamp)
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  },

  /**
   * 检测消息类型
   * @param {string} content 消息内容
   * @returns {string} 消息类型
   */
  detectMessageType: (content) => {
    // 检测是否是表情符号
    if (/^[\u{1F600}-\u{1F64F}]|[\u{1F300}-\u{1F5FF}]|[\u{1F680}-\u{1F6FF}]|[\u{1F1E0}-\u{1F1FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}]/u.test(content)) {
      return 'emoji'
    }

    // 检测是否是系统消息
    if (content.includes('[系统]')) {
      return 'system'
    }

    // 检测是否包含URL
    if (/https?:\/\/[^\s]+/.test(content)) {
      return 'link'
    }

    return 'text'
  }
}