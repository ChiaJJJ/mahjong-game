# 河南麻将线上游戏

一个基于Web的河南麻将线上游戏平台，支持多人实时对战、观战模式，并专门针对移动端进行优化。

## 技术栈

### 前端
- **框架**: Vue 3.4+
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router 4
- **实时通信**: Socket.IO Client
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **CSS预处理器**: SCSS
- **类型检查**: JavaScript (JSDoc)

### 后端
- **框架**: Spring Boot 3.4.5
- **数据持久层**: JPA + MyBatis
- **数据库**: MySQL 8.0+
- **实时通信**: Socket.IO
- **缓存**: Redis
- **API文档**: Swagger OpenAPI 3
- **日志框架**: Logback
- **构建工具**: Maven

## 项目结构

```
mahjong-game/
├── frontend/                 # 前端项目
│   ├── src/
│   │   ├── api/            # API接口定义
│   │   ├── assets/         # 静态资源
│   │   ├── components/     # Vue组件
│   │   ├── pages/          # 页面组件
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # Pinia状态管理
│   │   ├── styles/         # 样式文件
│   │   ├── utils/          # 工具函数
│   │   ├── App.vue         # 根组件
│   │   └── main.js         # 入口文件
│   ├── public/             # 公共资源
│   ├── package.json        # 依赖配置
│   ├── vite.config.js      # Vite配置
│   └── index.html          # HTML模板
├── backend/                # 后端项目
│   ├── src/
│   │   └── main/java/com/mahjong/
│   │       ├── MahjongApplication.java  # 启动类
│   │       ├── config/                 # 配置类
│   │       ├── controller/             # 控制器层
│   │       ├── service/                # 业务逻辑层
│   │       ├── repository/             # 数据访问层(JPA)
│   │       ├── mapper/                 # MyBatis映射器
│   │       ├── entity/                 # 实体类
│   │       ├── dto/                    # 数据传输对象
│   │       ├── enums/                  # 枚举类
│   │       ├── utils/                  # 工具类
│   │       ├── exception/              # 异常处理
│   │       └── websocket/              # WebSocket处理
│   ├── src/main/resources/
│   │   ├── application.yml             # 应用配置
│   │   └── mapper/                    # MyBatis XML文件
│   └── pom.xml                        # Maven依赖配置
├── database/               # 数据库脚本
│   └── init.sql                        # 数据库初始化脚本
├── docs/                   # 文档目录
│   ├── 开发规范文档.md                # 开发规范
│   ├── 技术架构文档.md                # 技术架构
│   ├── 数据库表结构设计.md             # 数据库设计
│   └── 需求分析文档.md                # 需求分析
├── .gitignore              # Git忽略文件
└── README.md               # 项目说明文档
```

## 快速开始

### 环境要求

- **Node.js**: 16.0.0+
- **npm**: 8.0.0+
- **Java**: 21+
- **Maven**: 3.8.0+
- **MySQL**: 8.0+
- **Redis**: 6.0+

### 1. 克隆项目

```bash
git clone <repository-url>
cd mahjong-game
```

### 2. 数据库配置

1. 安装并启动MySQL服务
2. 创建数据库和初始化数据：

```bash
mysql -u root -p < database/init.sql
```

3. 确保Redis服务正在运行

### 3. 后端启动

1. 进入后端目录：

```bash
cd backend
```

2. 配置应用参数（可选）：

```bash
# 开发环境
export SPRING_PROFILES_ACTIVE=dev
export DB_USERNAME=root
export DB_PASSWORD=your_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

3. 启动后端应用：

```bash
mvn clean install
mvn spring-boot:run
```

后端应用将在 `http://localhost:8080` 启动
WebSocket服务将在 `http://localhost:8081` 启动

### 4. 前端启动

1. 进入前端目录：

```bash
cd frontend
```

2. 安装依赖：

```bash
npm install
```

3. 启动开发服务器：

```bash
npm run dev
```

前端应用将在 `http://localhost:3000` 启动

## 开发指南

### 前端开发

1. **代码规范**：遵循 [开发规范文档](docs/开发规范文档.md)
2. **组件开发**：参考Vue 3 Composition API最佳实践
3. **状态管理**：使用Pinia进行全局状态管理
4. **API调用**：统一使用Axios，API定义在 `src/api/` 目录

### 后端开发

1. **代码规范**：遵循 [开发规范文档](docs/开发规范文档.md)
2. **分层架构**：严格按照Controller -> Service -> Repository分层
3. **数据库操作**：JPA用于复杂查询，MyBatis用于简单CRUD
4. **WebSocket**：使用Socket.IO进行实时通信

### 数据库设计

详细的数据库设计请参考 [数据库表结构设计文档](docs/数据库表结构设计.md)

### API文档

启动后端服务后，可通过以下地址访问API文档：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## 功能特性

### 核心功能
- ✅ 房间创建和管理
- ✅ 实时多人对战
- ✅ 河南麻将规则实现
- ✅ 观战模式
- ✅ 移动端适配
- ✅ 断线重连
- ✅ 游戏统计
- ✅ 聊天功能

### 游戏规则
- ✅ 标准河南麻将规则
- ✅ 混牌（赖子）功能
- ✅ 碰、杠、胡操作
- ✅ 计分系统
- ✅ 包胡规则

## 环境变量配置

### 后端环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 运行环境 | `dev` |
| `DB_URL` | 数据库连接URL | `jdbc:mysql://localhost:3306/mahjong_db` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `password` |
| `REDIS_HOST` | Redis主机地址 | `localhost` |
| `REDIS_PORT` | Redis端口 | `6379` |
| `SERVER_PORT` | HTTP服务端口 | `8080` |
| `WEBSOCKET_PORT` | WebSocket服务端口 | `8081` |

## 监控和维护

### 应用监控

- **健康检查**: `http://localhost:8080/actuator/health`
- **应用信息**: `http://localhost:8080/actuator/info`
- **性能指标**: `http://localhost:8080/actuator/metrics`

### 日志管理

- **开发环境**: 控制台输出，详细日志
- **生产环境**: 文件输出，按大小滚动

## 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目仅用于学习和研究目的。

## 更新日志

### v1.0.0 (开发中)
- ✅ 基础项目框架搭建
- ✅ 前后端技术栈配置
- ✅ 数据库设计
- ✅ 基础功能实现
- 🔄 WebSocket实时通信
- 🔄 游戏核心逻辑
- 🔄 用户界面开发
