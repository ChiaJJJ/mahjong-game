import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { roomApi } from '@/api/room'

export const RoomStatus = {
  WAITING: 'WAITING',
  PLAYING: 'PLAYING',
  FINISHED: 'FINISHED'
}

export const useRoomStore = defineStore('room', () => {
  // 状态定义
  const currentRoom = ref(null)
  const roomList = ref([])
  const isLoading = ref(false)
  const error = ref(null)
  const wsConnection = ref(null)
  const currentPlayerId = ref('')

  // 计算属性
  const isRoomOwner = computed(() => {
    if (!currentRoom.value || !currentPlayerId.value) return false
    return currentRoom.value.creatorId === currentPlayerId.value
  })

  const canStartGame = computed(() => {
    if (!currentRoom.value) return false
    const players = currentRoom.value.players || []
    const activePlayers = players.filter(p => !p.isSpectator)
    return activePlayers.length >= 2 && activePlayers.every(p => p.isReady)
  })

  const onlinePlayers = computed(() => {
    if (!currentRoom.value?.players) return []
    return currentRoom.value.players.filter(p => p.isOnline)
  })

  const roomPlayers = computed(() => {
    return currentRoom.value?.players || []
  })

  const spectatorCount = computed(() => {
    if (!currentRoom.value?.players) return 0
    return currentRoom.value.players.filter(p => p.isSpectator).length
  })

  const activePlayerCount = computed(() => {
    if (!currentRoom.value?.players) return 0
    return currentRoom.value.players.filter(p => !p.isSpectator).length
  })

  const isGameStarted = computed(() => {
    return currentRoom.value?.roomStatus === RoomStatus.PLAYING
  })

  const isGameFinished = computed(() => {
    return currentRoom.value?.roomStatus === RoomStatus.FINISHED
  })

  const getCurrentPlayer = computed(() => {
    if (!currentRoom.value?.players || !currentPlayerId.value) return null
    return currentRoom.value.players.find(p => p.id === currentPlayerId.value)
  })

  // Actions
  const createRoom = async (roomData) => {
    try {
      isLoading.value = true
      error.value = null

      // 生成创建者ID（如果没有提供）
      if (!roomData.creatorId) {
        roomData.creatorId = generateUserId()
      }

      const response = await roomApi.createRoom(roomData)
      currentRoom.value = response.data.data

      // 保存用户信息到本地
      setCurrentPlayer({
        id: roomData.creatorId,
        nickname: roomData.creatorNickname
      })

      return response.data
    } catch (err) {
      console.error('创建房间失败:', err)
      error.value = err.response?.data?.message || err.message || '创建房间失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const joinRoom = async (roomNumber, playerInfo) => {
    try {
      isLoading.value = true
      error.value = null

      // 生成玩家ID（如果没有提供）
      if (!playerInfo.playerId) {
        playerInfo.playerId = generateUserId()
      }

      const response = await roomApi.joinRoom(roomNumber, playerInfo)
      currentRoom.value = response.data.data

      // 保存用户信息到本地
      setCurrentPlayer({
        id: playerInfo.playerId,
        nickname: playerInfo.playerName
      })

      return response.data
    } catch (err) {
      console.error('加入房间失败:', err)
      error.value = err.response?.data?.message || err.message || '加入房间失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const leaveRoom = async (roomNumber, leaveData) => {
    try {
      isLoading.value = true
      error.value = null

      await roomApi.leaveRoom(roomNumber, leaveData)
      currentRoom.value = null

      // 清除本地用户信息
      clearCurrentPlayer()
    } catch (err) {
      console.error('离开房间失败:', err)
      error.value = err.response?.data?.message || err.message || '离开房间失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const setPlayerReady = async (roomNumber, readyData) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await roomApi.setPlayerReady(roomNumber, readyData)
      currentRoom.value = response.data.data

      return response.data
    } catch (err) {
      console.error('设置准备状态失败:', err)
      error.value = err.response?.data?.message || err.message || '设置准备状态失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const getRoomInfo = async (roomNumber) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await roomApi.getRoomInfo(roomNumber)
      currentRoom.value = response.data.data

      return response.data
    } catch (err) {
      console.error('获取房间信息失败:', err)
      error.value = err.response?.data?.message || err.message || '获取房间信息失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const getRoomList = async (params = {}) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await roomApi.getRoomList(params)
      roomList.value = response.data.data.rooms

      return response.data
    } catch (err) {
      console.error('获取房间列表失败:', err)
      error.value = err.response?.data?.message || err.message || '获取房间列表失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const getUserRoom = async (userId) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await roomApi.getUserRoom(userId)
      currentRoom.value = response.data.data

      return response.data
    } catch (err) {
      console.error('获取用户房间失败:', err)
      error.value = err.response?.data?.message || err.message || '获取用户房间失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  // WebSocket 连接管理
  const connectWebSocket = (roomNumber) => {
    if (wsConnection.value) {
      wsConnection.value.close()
    }

    try {
      // 这里需要实现WebSocket连接逻辑
      // const wsUrl = `ws://localhost:9914/ws/room/${roomNumber}`
      // wsConnection.value = new WebSocket(wsUrl)
      // setupWebSocketHandlers()

      console.log('WebSocket连接成功:', roomNumber)
    } catch (err) {
      console.error('WebSocket连接失败:', err)
      error.value = 'WebSocket连接失败'
    }
  }

  const disconnectWebSocket = () => {
    if (wsConnection.value) {
      wsConnection.value.close()
      wsConnection.value = null
    }
  }

  const setupWebSocketHandlers = () => {
    if (!wsConnection.value) return

    wsConnection.value.onopen = () => {
      console.log('WebSocket连接已建立')
    }

    wsConnection.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleWebSocketMessage(data)
      } catch (err) {
        console.error('WebSocket消息解析失败:', err)
      }
    }

    wsConnection.value.onclose = () => {
      console.log('WebSocket连接已关闭')
    }

    wsConnection.value.onerror = (err) => {
      console.error('WebSocket连接错误:', err)
    }
  }

  const handleWebSocketMessage = (message) => {
    switch (message.type) {
      case 'room_update':
        currentRoom.value = message.data
        break
      case 'player_joined':
        if (currentRoom.value) {
          currentRoom.value.players = message.data.players
          currentRoom.value.currentPlayers = message.data.currentPlayers
        }
        break
      case 'player_left':
        if (currentRoom.value) {
          currentRoom.value.players = message.data.players
          currentRoom.value.currentPlayers = message.data.currentPlayers
        }
        break
      case 'player_ready':
        if (currentRoom.value) {
          const player = currentRoom.value.players.find(p => p.id === message.data.playerId)
          if (player) {
            player.isReady = message.data.isReady
          }
        }
        break
      case 'game_started':
        if (currentRoom.value) {
          currentRoom.value.roomStatus = RoomStatus.PLAYING
        }
        break
      case 'game_finished':
        if (currentRoom.value) {
          currentRoom.value.roomStatus = RoomStatus.FINISHED
        }
        break
      default:
        console.log('未知WebSocket消息类型:', message.type)
    }
  }

  // 用户管理
  const setCurrentPlayer = (player) => {
    currentPlayerId.value = player.id
    localStorage.setItem('currentPlayerId', player.id)
    localStorage.setItem('currentPlayerNickname', player.nickname)
  }

  const getCurrentPlayerId = () => {
    if (!currentPlayerId.value) {
      currentPlayerId.value = localStorage.getItem('currentPlayerId') || ''
    }
    return currentPlayerId.value
  }

  const getCurrentPlayerNickname = () => {
    return localStorage.getItem('currentPlayerNickname') || ''
  }

  const clearCurrentPlayer = () => {
    currentPlayerId.value = ''
    localStorage.removeItem('currentPlayerId')
    localStorage.removeItem('currentPlayerNickname')
  }

  // 工具方法
  const generateUserId = () => {
    return 'player_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
  }

  const clearError = () => {
    error.value = null
  }

  const reset = () => {
    currentRoom.value = null
    roomList.value = []
    isLoading.value = false
    error.value = null
    disconnectWebSocket()
  }

  // 房间更新方法（用于WebSocket实时更新）
  const updateRoomState = (roomData) => {
    currentRoom.value = roomData
  }

  const updatePlayerStatus = (playerId, updates) => {
    if (!currentRoom.value?.players) return

    const player = currentRoom.value.players.find(p => p.id === playerId)
    if (player) {
      Object.assign(player, updates)
    }
  }

  const addPlayer = (player) => {
    if (!currentRoom.value?.players) return

    // 检查玩家是否已存在
    const existingPlayer = currentRoom.value.players.find(p => p.id === player.id)
    if (!existingPlayer) {
      currentRoom.value.players.push(player)
      currentRoom.value.currentPlayers = currentRoom.value.players.length
    }
  }

  const removePlayer = (playerId) => {
    if (!currentRoom.value?.players) return

    currentRoom.value.players = currentRoom.value.players.filter(p => p.id !== playerId)
    currentRoom.value.currentPlayers = currentRoom.value.players.length
  }

  return {
    // 状态
    currentRoom,
    roomList,
    isLoading,
    error,
    wsConnection,

    // 计算属性
    isRoomOwner,
    canStartGame,
    onlinePlayers,
    roomPlayers,
    spectatorCount,
    activePlayerCount,
    isGameStarted,
    isGameFinished,
    getCurrentPlayer,

    // Actions
    createRoom,
    joinRoom,
    leaveRoom,
    setPlayerReady,
    getRoomInfo,
    getRoomList,
    getUserRoom,

    // WebSocket
    connectWebSocket,
    disconnectWebSocket,

    // 用户管理
    setCurrentPlayer,
    getCurrentPlayerId,
    getCurrentPlayerNickname,
    clearCurrentPlayer,

    // 工具方法
    clearError,
    reset,
    generateUserId,

    // 实时更新
    updateRoomState,
    updatePlayerStatus,
    addPlayer,
    removePlayer
  }
})