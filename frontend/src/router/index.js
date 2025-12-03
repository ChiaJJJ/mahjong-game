import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/Home.vue'),
    meta: {
      title: '河南麻将 - 首页'
    }
  },
  {
    path: '/room/:roomId',
    name: 'GameRoom',
    component: () => import('@/pages/GameRoom.vue'),
    props: true,
    meta: {
      title: '游戏房间',
      requiresAuth: false,
      keepAlive: true
    }
  },
  {
    path: '/spectate/:roomId',
    name: 'Spectate',
    component: () => import('@/pages/Spectate.vue'),
    props: true,
    meta: {
      title: '观战模式'
    }
  },
  {
    path: '/create-room',
    name: 'CreateRoom',
    component: () => import('@/pages/CreateRoom.vue'),
    meta: {
      title: '创建房间'
    }
  },
  {
    path: '/join-room',
    name: 'JoinRoom',
    component: () => import('@/pages/JoinRoom.vue'),
    meta: {
      title: '加入房间'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/pages/NotFound.vue'),
    meta: {
      title: '页面未找到'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }

  next()
})

export default router