@echo off
echo ==================================
echo 河南麻将游戏 - 开发环境启动
echo ==================================

REM 设置环境变量
set SPRING_PROFILES_ACTIVE=dev
set DB_USERNAME=root
set DB_PASSWORD=dev_password
set REDIS_HOST=localhost
set REDIS_PORT=6379

echo.
echo 正在检查环境依赖...

REM 检查Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 请先安装 Node.js (16.0.0+)
    pause
    exit /b 1
)

REM 检查Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 请先安装 Java (21+)
    pause
    exit /b 1
)

REM 检查Maven
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 请先安装 Maven (3.8.0+)
    pause
    exit /b 1
)

echo.
echo 正在初始化数据库...

REM 初始化数据库
mysql -u root -pdev_password < database\init.sql
if %errorlevel% neq 0 (
    echo [WARNING] 数据库初始化失败，请检查MySQL连接
) else (
    echo [SUCCESS] 数据库初始化完成
)

echo.
echo 正在启动后端服务...
start "Backend Service" cmd /k "cd backend && mvn spring-boot:run"

REM 等待后端启动
timeout /t 30 /nobreak

echo.
echo 正在安装前端依赖...
cd frontend
call npm install

echo.
echo 正在启动前端开发服务器...
start "Frontend Dev Server" cmd /k "npm run dev"

echo.
echo ==================================
echo 服务启动完成！
echo 前端地址: http://localhost:3000
echo 后端地址: http://localhost:8080
echo API文档: http://localhost:8080/swagger-ui.html
echo ==================================
echo.
echo 按任意键关闭此窗口...
pause >nul