import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { gameApi } from '@/api/game'

/**
 * 游戏状态枚举
 */
export const GameState = {
  WAITING: 'WAITING',
  PLAYING: 'PLAYING',
  FINISHED: 'FINISHED'
}

/**
 * 玩家状态枚举
 */
export const PlayerStatus = {
  ONLINE: 'ONLINE',
  READY: 'READY',
  PLAYING: 'PLAYING',
  OFFLINE: 'OFFLINE'
}

/**
 * @typedef {Object} Player
 * @property {string} id - 玩家ID
 * @property {string} name - 玩家昵称
 * @property {string} avatar - 玩家头像
 * @property {number} position - 玩家位置(1-4)
 * @property {string} status - 玩家状态
 * @property {boolean} isSpectator - 是否观战者
 * @property {number} totalScore - 总分数
 * @property {number} winsCount - 获胜次数
 * @property {boolean} isReady - 是否准备
 */

/**
 * @typedef {Object} Room
 * @property {string} id - 房间ID
 * @property {string} roomNumber - 房间号
 * @property {string} roomName - 房间名称
 * @property {string} creatorId - 创建者ID
 * @property {number} maxPlayers - 最大玩家数
 * @property {number} currentPlayers - 当前玩家数
 * @property {number} spectatorCount - 观战人数
 * @property {string} status - 房间状态
 * @property {boolean} allowSpectate - 是否允许观战
 * @property {Player[]} players - 玩家列表
 * @property {Object} gameConfig - 游戏配置
 * @property {Date} createdAt - 创建时间
 */

/**
 * @typedef {Object} GameTile
 * @property {string} id - 牌ID
 * @property {string} type - 牌类型(bamboo, character, dot, honor)
 * @property {string} value - 牌值
 * @property {boolean} isMixed - 是否为混牌
 */

export const useGameStore = defineStore('game', () => {
  // 状态定义
  const currentRoom = ref(null)
  const currentGame = ref(null)
  const playerHand = ref([])
  const discardPile = ref([])
  const gameState = ref(GameState.WAITING)
  const currentPlayer = ref(0)
  const isSpectating = ref(false)
  const connectionStatus = ref('connected')
  const lastAction = ref(null)

  // 计算属性
  const isMyTurn = computed(() => {
    return (playerId) => currentGame.value?.currentPlayer === playerId
  })

  const canPlayTile = computed(() => {
    return gameState.value === GameState.PLAYING && !isSpectating.value
  })

  const isGameReady = computed(() => {
    return currentRoom.value?.players?.every(player => player.isReady) &&
           currentRoom.value?.players?.length >= 2
  })

  const handTileCount = computed(() => {
    return playerHand.value.length
  })

  const roomPlayers = computed(() => {
    return currentRoom.value?.players?.filter(player => !player.isSpectator) || []
  })

  const spectators = computed(() => {
    return currentRoom.value?.players?.filter(player => player.isSpectator) || []
  })

  // Actions
  /**
   * 创建房间
   * @param {Object} roomData - 房间数据
   * @returns {Promise<Room>}
   */
  const createRoom = async (roomData) => {
    try {
      const response = await gameApi.createRoom(roomData)
      currentRoom.value = response.data
      return response.data
    } catch (error) {
      console.error('创建房间失败:', error)
      throw new Error('创建房间失败')
    }
  }

  /**
   * 加入房间
   * @param {string} roomId - 房间号
   * @param {Object} playerInfo - 玩家信息
   * @returns {Promise<Room>}
   */
  const joinRoom = async (roomId, playerInfo) => {
    try {
      const response = await gameApi.joinRoom(roomId, playerInfo)
      currentRoom.value = response.data
      return response.data
    } catch (error) {
      console.error('加入房间失败:', error)
      throw new Error('加入房间失败')
    }
  }

  /**
   * 离开房间
   */
  const leaveRoom = async () => {
    try {
      if (currentRoom.value) {
        await gameApi.leaveRoom(currentRoom.value.roomNumber)
      }
      resetGame()
    } catch (error) {
      console.error('离开房间失败:', error)
    }
  }

  /**
   * 开始游戏
   */
  const startGame = async () => {
    try {
      if (!currentRoom.value) {
        throw new Error('没有选择房间')
      }

      const response = await gameApi.startGame(currentRoom.value.roomNumber)
      currentGame.value = response.data
      gameState.value = GameState.PLAYING
      playerHand.value = response.data.playerHand || []
      discardPile.value = response.data.discardPile || []
      currentPlayer.value = response.data.currentPlayer || 0

      return response.data
    } catch (error) {
      console.error('开始游戏失败:', error)
      throw new Error('开始游戏失败')
    }
  }

  /**
   * 玩家准备
   */
  const setPlayerReady = async () => {
    try {
      if (!currentRoom.value) {
        throw new Error('没有选择房间')
      }

      await gameApi.playerReady(currentRoom.value.roomNumber)

      // 更新本地状态
      const currentPlayer = currentRoom.value.players.find(p => p.id === getCurrentPlayerId())
      if (currentPlayer) {
        currentPlayer.isReady = true
      }
    } catch (error) {
      console.error('玩家准备失败:', error)
      throw new Error('准备失败')
    }
  }

  /**
   * 出牌
   * @param {GameTile} tile - 要出的牌
   */
  const playTile = async (tile) => {
    if (!canPlayTile.value) {
      throw new Error('当前不能出牌')
    }

    try {
      await gameApi.playTile(currentGame.value.id, tile.id)

      // 从手牌中移除
      const index = playerHand.value.findIndex(t => t.id === tile.id)
      if (index > -1) {
        playerHand.value.splice(index, 1)
      }

      // 添加到弃牌堆
      discardPile.value.push(tile)

      lastAction.value = { type: 'DISCARD', tile, timestamp: Date.now() }
    } catch (error) {
      console.error('出牌失败:', error)
      throw new Error('出牌失败')
    }
  }

  /**
   * 摸牌
   */
  const drawTile = async () => {
    try {
      const response = await gameApi.drawTile(currentGame.value.id)
      const tile = response.data.tile

      playerHand.value.push(tile)
      lastAction.value = { type: 'DRAW', tile, timestamp: Date.now() }

      return tile
    } catch (error) {
      console.error('摸牌失败:', error)
      throw new Error('摸牌失败')
    }
  }

  /**
   * 碰牌
   * @param {GameTile} tile - 要碰的牌
   */
  const claimPeng = async (tile) => {
    try {
      await gameApi.claimPeng(currentGame.value.id, tile.id)
      lastAction.value = { type: 'PENG', tile, timestamp: Date.now() }
    } catch (error) {
      console.error('碰牌失败:', error)
      throw new Error('碰牌失败')
    }
  }

  /**
   * 杠牌
   * @param {GameTile} tile - 要杠的牌
   */
  const claimGang = async (tile) => {
    try {
      await gameApi.claimGang(currentGame.value.id, tile.id)
      lastAction.value = { type: 'GANG', tile, timestamp: Date.now() }
    } catch (error) {
      console.error('杠牌失败:', error)
      throw new Error('杠牌失败')
    }
  }

  /**
   * 胡牌
   * @param {GameTile[]} winningTiles - 胡牌组合
   */
  const claimHu = async (winningTiles) => {
    try {
      await gameApi.claimHu(currentGame.value.id, winningTiles)
      gameState.value = GameState.FINISHED
      lastAction.value = { type: 'HU', tiles: winningTiles, timestamp: Date.now() }
    } catch (error) {
      console.error('胡牌失败:', error)
      throw new Error('胡牌失败')
    }
  }

  /**
   * 过牌
   */
  const pass = async () => {
    try {
      await gameApi.pass(currentGame.value.id)
      lastAction.value = { type: 'PASS', timestamp: Date.now() }
    } catch (error) {
      console.error('过牌失败:', error)
      throw new Error('过牌失败')
    }
  }

  /**
   * 重置游戏状态
   */
  const resetGame = () => {
    currentRoom.value = null
    currentGame.value = null
    playerHand.value = []
    discardPile.value = []
    gameState.value = GameState.WAITING
    currentPlayer.value = 0
    isSpectating.value = false
    lastAction.value = null
  }

  /**
   * 更新游戏状态
   * @param {Object} gameStateUpdate - 游戏状态更新
   */
  const updateGameState = (gameStateUpdate) => {
    if (gameStateUpdate.room) {
      currentRoom.value = gameStateUpdate.room
    }

    if (gameStateUpdate.game) {
      currentGame.value = gameStateUpdate.game
      gameState.value = gameStateUpdate.game.status
      currentPlayer.value = gameStateUpdate.game.currentPlayer
      discardPile.value = gameStateUpdate.game.discardPile || []
    }

    if (gameStateUpdate.playerHand) {
      playerHand.value = gameStateUpdate.playerHand
    }
  }

  /**
   * 设置连接状态
   * @param {string} status - 连接状态
   */
  const setConnectionStatus = (status) => {
    connectionStatus.value = status
  }

  /**
   * 获取当前玩家ID
   * @returns {string}
   */
  const getCurrentPlayerId = () => {
    // 这里应该从本地存储或其他地方获取当前玩家ID
    return localStorage.getItem('playerId') || null
  }

  return {
    // 状态
    currentRoom,
    currentGame,
    playerHand,
    discardPile,
    gameState,
    currentPlayer,
    isSpectating,
    connectionStatus,
    lastAction,

    // 计算属性
    isMyTurn,
    canPlayTile,
    isGameReady,
    handTileCount,
    roomPlayers,
    spectators,

    // Actions
    createRoom,
    joinRoom,
    leaveRoom,
    startGame,
    setPlayerReady,
    playTile,
    drawTile,
    claimPeng,
    claimGang,
    claimHu,
    pass,
    resetGame,
    updateGameState,
    setConnectionStatus,
    getCurrentPlayerId
  }
})