import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:9980/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 游戏API接口
 */
export const gameApi = {
  /**
   * 创建房间
   * @param {Object} roomData - 房间数据
   * @returns {Promise}
   */
  createRoom: (roomData) => {
    return api.post('/rooms', roomData)
  },

  /**
   * 加入房间
   * @param {string} roomNumber - 房间号
   * @param {Object} playerInfo - 玩家信息
   * @returns {Promise}
   */
  joinRoom: (roomNumber, playerInfo) => {
    return api.post(`/rooms/${roomNumber}/join`, playerInfo)
  },

  /**
   * 离开房间
   * @param {string} roomNumber - 房间号
   * @returns {Promise}
   */
  leaveRoom: (roomNumber) => {
    return api.post(`/rooms/${roomNumber}/leave`)
  },

  /**
   * 开始游戏
   * @param {string} roomNumber - 房间号
   * @returns {Promise}
   */
  startGame: (roomNumber) => {
    return api.post(`/rooms/${roomNumber}/start`)
  },

  /**
   * 玩家准备
   * @param {string} roomNumber - 房间号
   * @returns {Promise}
   */
  playerReady: (roomNumber) => {
    return api.post(`/rooms/${roomNumber}/ready`)
  },

  /**
   * 出牌
   * @param {string} gameId - 游戏ID
   * @param {string} tileId - 牌ID
   * @returns {Promise}
   */
  playTile: (gameId, tileId) => {
    return api.post(`/games/${gameId}/play`, { tileId })
  },

  /**
   * 摸牌
   * @param {string} gameId - 游戏ID
   * @returns {Promise}
   */
  drawTile: (gameId) => {
    return api.post(`/games/${gameId}/draw`)
  },

  /**
   * 碰牌
   * @param {string} gameId - 游戏ID
   * @param {string} tileId - 牌ID
   * @returns {Promise}
   */
  claimPeng: (gameId, tileId) => {
    return api.post(`/games/${gameId}/peng`, { tileId })
  },

  /**
   * 杠牌
   * @param {string} gameId - 游戏ID
   * @param {string} tileId - 牌ID
   * @returns {Promise}
   */
  claimGang: (gameId, tileId) => {
    return api.post(`/games/${gameId}/gang`, { tileId })
  },

  /**
   * 胡牌
   * @param {string} gameId - 游戏ID
   * @param {Array} winningTiles - 胡牌组合
   * @returns {Promise}
   */
  claimHu: (gameId, winningTiles) => {
    return api.post(`/games/${gameId}/hu`, { winningTiles })
  },

  /**
   * 过牌
   * @param {string} gameId - 游戏ID
   * @returns {Promise}
   */
  pass: (gameId) => {
    return api.post(`/games/${gameId}/pass`)
  },

  /**
   * 获取房间信息
   * @param {string} roomNumber - 房间号
   * @returns {Promise}
   */
  getRoom: (roomNumber) => {
    return api.get(`/rooms/${roomNumber}`)
  },

  /**
   * 获取游戏状态
   * @param {string} gameId - 游戏ID
   * @returns {Promise}
   */
  getGameStatus: (gameId) => {
    return api.get(`/games/${gameId}`)
  }
}

export default gameApi