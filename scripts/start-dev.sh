#!/bin/bash

echo "=================================="
echo "河南麻将游戏 - 开发环境启动"
echo "=================================="

# 设置环境变量
export SPRING_PROFILES_ACTIVE=dev
export DB_USERNAME=root
export DB_PASSWORD=dev_password
export REDIS_HOST=localhost
export REDIS_PORT=6379

echo ""
echo "正在检查环境依赖..."

# 检查Node.js
if ! command -v node &> /dev/null; then
    echo "[ERROR] 请先安装 Node.js (16.0.0+)"
    exit 1
fi

# 检查Java
if ! command -v java &> /dev/null; then
    echo "[ERROR] 请先安装 Java (21+)"
    exit 1
fi

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "[ERROR] 请先安装 Maven (3.8.0+)"
    exit 1
fi

echo ""
echo "正在初始化数据库..."

# 初始化数据库
mysql -u root -pdev_password < database/init.sql
if [ $? -ne 0 ]; then
    echo "[WARNING] 数据库初始化失败，请检查MySQL连接"
else
    echo "[SUCCESS] 数据库初始化完成"
fi

echo ""
echo "正在启动后端服务..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

# 等待后端启动
echo "等待后端服务启动..."
sleep 30

echo ""
echo "正在安装前端依赖..."
cd ../frontend
npm install

echo ""
echo "正在启动前端开发服务器..."
npm run dev &
FRONTEND_PID=$!

echo ""
echo "=================================="
echo "服务启动完成！"
echo "前端地址: http://localhost:3000"
echo "后端地址: http://localhost:8080"
echo "API文档: http://localhost:8080/swagger-ui.html"
echo "=================================="
echo ""

# 捕获退出信号
trap 'echo "正在停止服务..."; kill $BACKEND_PID $FRONTEND_PID; exit' INT TERM

# 等待用户中断
echo "按 Ctrl+C 停止所有服务"
wait