<template>
  <div class="not-found-container">
    <div class="content-card">
      <div class="error-icon">
        <el-icon size="80"><Warning /></el-icon>
      </div>
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>抱歉，您访问的页面不存在或已被移除</p>

      <div class="actions">
        <el-button type="primary" @click="$router.push('/')">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button @click="$router.go(-1)">
          <el-icon><ArrowLeft /></el-icon>
          返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Warning, HomeFilled, ArrowLeft } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.not-found-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.content-card {
  background: white;
  border-radius: 15px;
  padding: 3rem;
  text-align: center;
  max-width: 400px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);

  .error-icon {
    color: #f56c6c;
    margin-bottom: 1rem;
  }

  h1 {
    font-size: 4rem;
    color: #f56c6c;
    margin: 0 0 0.5rem 0;
  }

  h2 {
    font-size: 1.8rem;
    color: #333;
    margin: 0 0 1rem 0;
  }

  p {
    color: #666;
    margin-bottom: 2rem;
    line-height: 1.6;
  }

  .actions {
    display: flex;
    gap: 1rem;
    justify-content: center;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .content-card {
    padding: 2rem;
  }

  h1 {
    font-size: 3rem;
  }

  h2 {
    font-size: 1.5rem;
  }

  .actions {
    flex-direction: column;
    align-items: center;

    .el-button {
      width: 150px;
    }
  }
}
</style>