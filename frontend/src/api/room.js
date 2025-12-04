import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9980/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 添加loading状态（可选）
    if (config.showLoading !== false) {
      // 可以在这里添加全局loading
    }

    // 添加token（如果有的话）
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 添加请求ID用于追踪
    config.headers['X-Request-ID'] = generateRequestId()

    console.log('API请求:', config.method?.toUpperCase(), config.url, config.data)
    return config
  },
  error => {
    console.error('API请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('API响应:', response.config.url, response.data)

    // 检查业务状态码
    if (response.data && response.data.code !== 200) {
      const message = response.data.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }

    return response
  },
  error => {
    console.error('API请求错误:', error)

    let message = '网络错误，请稍后重试'

    if (error.response) {
      const status = error.response.status
      const data = error.response.data

      switch (status) {
        case 400:
          message = data?.message || '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          // 清除token
          localStorage.removeItem('token')
          break
        case 403:
          message = '无权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 429:
          message = '请求过于频繁，请稍后重试'
          break
        case 500:
          message = data?.message || '服务器内部错误'
          break
        default:
          message = data?.message || `请求失败 (${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请检查网络连接'
    } else if (error.message === 'Network Error') {
      message = '网络连接失败，请检查网络设置'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

/**
 * 生成请求ID
 */
const generateRequestId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

/**
 * 重试请求
 */
const retryRequest = async (fn, retries = 3, delay = 1000) => {
  try {
    return await fn()
  } catch (error) {
    if (retries > 0 && shouldRetry(error)) {
      console.log(`请求失败，${delay}ms后重试，剩余重试次数: ${retries}`)
      await new Promise(resolve => setTimeout(resolve, delay))
      return retryRequest(fn, retries - 1, delay * 2)
    }
    throw error
  }
}

/**
 * 判断是否应该重试
 */
const shouldRetry = (error) => {
  // 网络错误或5xx错误可以重试
  return !error.response ||
         (error.response.status >= 500 && error.response.status < 600) ||
         error.code === 'ECONNABORTED' ||
         error.message === 'Network Error'
}

/**
 * 房间API接口
 */
export const roomApi = {
  /**
   * 创建房间
   * @param {Object} roomData - 房间数据
   * @param {string} roomData.roomName - 房间名称
   * @param {string} roomData.creatorId - 创建者ID
   * @param {string} roomData.creatorNickname - 创建者昵称
   * @param {string} roomData.password - 房间密码（可选）
   * @param {number} roomData.maxPlayers - 最大玩家数
   * @param {boolean} roomData.allowSpectate - 是否允许观战
   * @param {boolean} roomData.isPublic - 是否公开房间
   * @param {Object} roomData.gameConfig - 游戏配置
   * @returns {Promise}
   */
  createRoom: (roomData) => {
    return retryRequest(() =>
      api.post('/rooms', roomData, { showLoading: true })
    )
  },

  /**
   * 加入房间
   * @param {string} roomNumber - 房间号
   * @param {Object} playerInfo - 玩家信息
   * @param {string} playerInfo.playerId - 玩家ID
   * @param {string} playerInfo.playerName - 玩家昵称
   * @param {string} playerInfo.password - 房间密码（可选）
   * @param {boolean} playerInfo.asSpectator - 是否观战
   * @param {string} playerInfo.avatarUrl - 头像URL（可选）
   * @param {string} playerInfo.deviceInfo - 设备信息（可选）
   * @returns {Promise}
   */
  joinRoom: (roomNumber, playerInfo) => {
    return retryRequest(() =>
      api.post(`/rooms/${roomNumber}/join`, playerInfo, { showLoading: true })
    )
  },

  /**
   * 离开房间
   * @param {string} roomNumber - 房间号
   * @param {Object} leaveData - 离开房间数据
   * @param {string} leaveData.playerId - 玩家ID
   * @param {string} leaveData.reason - 离开原因（可选）
   * @param {boolean} leaveData.switchToSpectator - 是否切换到观战模式
   * @returns {Promise}
   */
  leaveRoom: (roomNumber, leaveData) => {
    return retryRequest(() =>
      api.post(`/rooms/${roomNumber}/leave`, leaveData, { showLoading: true })
    )
  },

  /**
   * 获取房间信息
   * @param {string} roomNumber - 房间号
   * @param {string} userId - 用户ID（可选，用于获取用户特定信息）
   * @returns {Promise}
   */
  getRoomInfo: (roomNumber, userId) => {
    const params = userId ? { userId } : {}
    return retryRequest(() =>
      api.get(`/rooms/${roomNumber}`, { params, showLoading: false })
    )
  },

  /**
   * 获取房间列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页大小
   * @param {string} params.status - 房间状态过滤
   * @param {boolean} params.isPublic - 是否公开房间
   * @returns {Promise}
   */
  getRoomList: (params = {}) => {
    const defaultParams = {
      page: 1,
      size: 20,
      ...params
    }
    return retryRequest(() =>
      api.get('/rooms', { params: defaultParams, showLoading: false })
    )
  },

  /**
   * 玩家准备/取消准备
   * @param {string} roomNumber - 房间号
   * @param {Object} readyData - 准备状态数据
   * @param {string} readyData.playerId - 玩家ID
   * @param {boolean} readyData.isReady - 是否准备
   * @returns {Promise}
   */
  setPlayerReady: (roomNumber, readyData) => {
    return retryRequest(() =>
      api.post(`/rooms/${roomNumber}/ready`, readyData, { showLoading: true })
    )
  },

  /**
   * 获取用户所在房间
   * @param {string} userId - 用户ID
   * @returns {Promise}
   */
  getUserRoom: (userId) => {
    return retryRequest(() =>
      api.get(`/rooms/user/${userId}`, { showLoading: false })
    )
  },

  /**
   * 清理过期房间
   * @returns {Promise}
   */
  cleanupExpiredRooms: () => {
    return retryRequest(() =>
      api.delete('/rooms/cleanup', { showLoading: false })
    )
  },

  /**
   * 开始游戏
   * @param {string} roomNumber - 房间号
   * @param {Object} gameData - 游戏数据
   * @returns {Promise}
   */
  startGame: (roomNumber, gameData) => {
    return retryRequest(() =>
      api.post(`/rooms/${roomNumber}/start`, gameData, { showLoading: true })
    )
  },

  /**
   * 结束游戏
   * @param {string} roomNumber - 房间号
   * @param {Object} endData - 结束游戏数据
   * @returns {Promise}
   */
  endGame: (roomNumber, endData) => {
    return retryRequest(() =>
      api.post(`/rooms/${roomNumber}/end`, endData, { showLoading: true })
    )
  }
}

/**
 * WebSocket管理类
 */
export class WebSocketManager {
  constructor() {
    this.connections = new Map()
  }

  /**
   * 建立WebSocket连接
   * @param {string} roomNumber - 房间号
   * @param {Object} options - 连接选项
   * @returns {WebSocket}
   */
  connect(roomNumber, options = {}) {
    if (this.connections.has(roomNumber)) {
      this.disconnect(roomNumber)
    }

    const wsUrl = `${import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:9914'}/ws/room/${roomNumber}`
    const ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      console.log(`WebSocket连接成功: ${roomNumber}`)
      options.onOpen?.(ws)
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        options.onMessage?.(data)
      } catch (err) {
        console.error('WebSocket消息解析失败:', err)
        options.onError?.(err)
      }
    }

    ws.onclose = () => {
      console.log(`WebSocket连接关闭: ${roomNumber}`)
      this.connections.delete(roomNumber)
      options.onClose?.()
    }

    ws.onerror = (err) => {
      console.error(`WebSocket连接错误: ${roomNumber}`, err)
      options.onError?.(err)
    }

    this.connections.set(roomNumber, ws)
    return ws
  }

  /**
   * 断开WebSocket连接
   * @param {string} roomNumber - 房间号
   */
  disconnect(roomNumber) {
    const ws = this.connections.get(roomNumber)
    if (ws) {
      ws.close()
      this.connections.delete(roomNumber)
    }
  }

  /**
   * 断开所有连接
   */
  disconnectAll() {
    for (const [roomNumber] of this.connections) {
      this.disconnect(roomNumber)
    }
  }

  /**
   * 发送消息
   * @param {string} roomNumber - 房间号
   * @param {Object} message - 消息对象
   */
  sendMessage(roomNumber, message) {
    const ws = this.connections.get(roomNumber)
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(message))
      return true
    }
    return false
  }
}

// 创建全局WebSocket管理器实例
export const wsManager = new WebSocketManager()

// 导出默认实例
export default roomApi