<template>
  <div class="home-container">
    <div class="hero-section">
      <h1>河南麻将</h1>
      <p class="subtitle">经典在线麻将游戏</p>
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="$router.push('/create-room')">
          <el-icon><Plus /></el-icon>
          创建房间
        </el-button>
        <el-button size="large" @click="$router.push('/join-room')">
          <el-icon><User /></el-icon>
          加入房间
        </el-button>
      </div>
    </div>

    <div class="features-section">
      <div class="feature-card">
        <div class="feature-icon">
          <el-icon size="48"><UserFilled /></el-icon>
        </div>
        <h3>好友开黑</h3>
        <p>创建私人房间，与好友一起游戏</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">
          <el-icon size="48"><Monitor /></el-icon>
        </div>
        <h3>实时对战</h3>
        <p>流畅的游戏体验，实时同步</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">
          <el-icon size="48"><Trophy /></el-icon>
        </div>
        <h3>河南规则</h3>
        <p>正宗的河南麻将规则</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { User, Plus, UserFilled, Monitor, Trophy } from '@element-plus/icons-vue'

// 首页逻辑
</script>

<style lang="scss" scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  box-sizing: border-box;
}

.hero-section {
  text-align: center;
  color: white;
  margin-bottom: 3rem;

  h1 {
    font-size: 3rem;
    margin-bottom: 1rem;
    text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
    letter-spacing: 2px;
  }

  .subtitle {
    font-size: 1.2rem;
    margin-bottom: 2rem;
    opacity: 0.9;
    letter-spacing: 1px;
  }
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;

  .el-button {
    padding: 12px 24px;
    font-size: 16px;
    border-radius: 8px;
    min-width: 140px;

    .el-icon {
      margin-right: 8px;
    }
  }
}

.features-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  max-width: 1000px;
  width: 100%;
}

.feature-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 2rem;
  text-align: center;
  color: white;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    background: rgba(255, 255, 255, 0.15);
  }

  .feature-icon {
    margin-bottom: 1rem;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    margin: 0 auto 1rem;

    .el-icon {
      color: rgba(255, 255, 255, 0.9);
    }
  }

  h3 {
    font-size: 1.5rem;
    margin-bottom: 1rem;
    font-weight: 600;
  }

  p {
    opacity: 0.9;
    line-height: 1.6;
    font-size: 0.95rem;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 1rem;
  }

  .hero-section h1 {
    font-size: 2.5rem;
  }

  .action-buttons {
    flex-direction: column;
    align-items: center;

    .el-button {
      width: 200px;
    }
  }

  .features-section {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .feature-card {
    padding: 1.5rem;
  }
}

@media (max-width: 480px) {
  .hero-section h1 {
    font-size: 2rem;
  }

  .action-buttons {
    .el-button {
      min-width: 160px;
    }
  }
}
</style>