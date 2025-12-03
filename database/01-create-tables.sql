-- 河南麻将游戏数据库表结构
-- 创建时间: 2025-01-17

-- 创建数据库
CREATE DATABASE IF NOT EXISTS mahjong_game
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE mahjong_game;

-- 房间表
CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '房间ID',
    room_number VARCHAR(10) NOT NULL UNIQUE COMMENT '房间号',
    room_name VARCHAR(100) DEFAULT '' COMMENT '房间名称',
    creator_id VARCHAR(36) NOT NULL COMMENT '创建者ID',
    status ENUM('WAITING', 'PLAYING', 'FINISHED') DEFAULT 'WAITING' COMMENT '房间状态',
    max_players TINYINT DEFAULT 4 COMMENT '最大玩家数',
    current_players TINYINT DEFAULT 0 COMMENT '当前玩家数',
    game_config JSON COMMENT '游戏配置',
    allow_spectate BOOLEAN DEFAULT TRUE COMMENT '是否允许观战',
    spectator_count TINYINT DEFAULT 0 COMMENT '观战人数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    INDEX idx_room_number (room_number),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间信息表';

-- 玩家表
CREATE TABLE players (
    id VARCHAR(36) PRIMARY KEY COMMENT '玩家ID',
    room_id BIGINT NOT NULL COMMENT '所属房间ID',
    player_name VARCHAR(50) NOT NULL COMMENT '玩家昵称',
    player_avatar VARCHAR(200) DEFAULT '' COMMENT '玩家头像URL',
    player_position TINYINT DEFAULT 0 COMMENT '玩家位置(1-4)',
    player_status ENUM('ONLINE', 'OFFLINE', 'READY', 'PLAYING') DEFAULT 'ONLINE' COMMENT '玩家状态',
    spectator BOOLEAN DEFAULT FALSE COMMENT '是否为观战者',
    total_score INT DEFAULT 0 COMMENT '总分数',
    wins_count INT DEFAULT 0 COMMENT '获胜次数',
    ready_at TIMESTAMP NULL COMMENT '准备时间',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    INDEX idx_room_id (room_id),
    INDEX idx_player_name (player_name),
    INDEX idx_player_status (player_status),
    INDEX idx_last_active_at (last_active_at),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家信息表';

-- 游戏记录表
CREATE TABLE games (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '游戏ID',
    room_id BIGINT NOT NULL COMMENT '所属房间ID',
    game_number INT NOT NULL COMMENT '游戏局数',
    game_status ENUM('PREPARING', 'PLAYING', 'FINISHED') DEFAULT 'PREPARING' COMMENT '游戏状态',
    current_player_id VARCHAR(36) COMMENT '当前出牌玩家ID',
    current_round TINYINT DEFAULT 1 COMMENT '当前回合数',
    wall_tiles JSON COMMENT '牌墙信息',
    draw_pile JSON COMMENT '摸牌堆信息',
    discard_pile JSON COMMENT '弃牌堆信息',
    mixed_tile VARCHAR(10) COMMENT '混牌标识',
    round_number TINYINT DEFAULT 1 COMMENT '圈数',
    hand_number TINYINT DEFAULT 1 COMMENT '手数',
    dealer_id VARCHAR(36) COMMENT '庄家ID',
    wind_direction ENUM('EAST', 'SOUTH', 'WEST', 'NORTH') DEFAULT 'EAST' COMMENT '当前风位',
    game_result JSON COMMENT '游戏结果',
    started_at TIMESTAMP NULL COMMENT '开始时间',
    finished_at TIMESTAMP NULL COMMENT '结束时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_room_id (room_id),
    INDEX idx_game_status (game_status),
    INDEX idx_started_at (started_at),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏记录表';

-- 玩家手牌表
CREATE TABLE player_hands (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '手牌ID',
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    player_id VARCHAR(36) NOT NULL COMMENT '玩家ID',
    hand_tiles JSON NOT NULL COMMENT '手牌信息',
    draw_count TINYINT DEFAULT 0 COMMENT '摸牌次数',
    discard_count TINYINT DEFAULT 0 COMMENT '出牌次数',
    claim_count TINYINT DEFAULT 0 COMMENT '吃碰杠次数',
    score INT DEFAULT 0 COMMENT '本局得分',
    winner BOOLEAN DEFAULT FALSE COMMENT '是否获胜',
    win_type ENUM('SELF_DRAWN', 'DISCARD', 'KONG', 'ROB') COMMENT '胡牌类型',
    winning_tiles JSON COMMENT '胡牌组合',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_game_id (game_id),
    INDEX idx_player_id (player_id),
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家手牌表';

-- 游戏操作记录表
CREATE TABLE game_actions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '操作ID',
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    player_id VARCHAR(36) NOT NULL COMMENT '玩家ID',
    action_type ENUM('DRAW', 'DISCARD', 'PENG', 'GANG', 'CHI', 'HU', 'PASS') NOT NULL COMMENT '操作类型',
    action_data JSON COMMENT '操作详情',
    tile_info VARCHAR(20) COMMENT '相关牌信息',
    action_order INT NOT NULL COMMENT '操作顺序',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_game_id (game_id),
    INDEX idx_player_id (player_id),
    INDEX idx_action_type (action_type),
    INDEX idx_timestamp (timestamp),
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏操作记录表';

-- 游戏配置表
CREATE TABLE game_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_name VARCHAR(50) NOT NULL UNIQUE COMMENT '配置名称',
    base_score TINYINT DEFAULT 1 COMMENT '基础分',
    max_rounds TINYINT DEFAULT 8 COMMENT '最大局数',
    allow_peng BOOLEAN DEFAULT TRUE COMMENT '允许碰',
    allow_gang BOOLEAN DEFAULT TRUE COMMENT '允许杠',
    allow_chi BOOLEAN DEFAULT FALSE COMMENT '允许吃(河南麻将不允许)',
    require_tiles BOOLEAN DEFAULT TRUE COMMENT '是否必须达到规定胡牌番数',
    mixed_tile_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用混牌',
    auto_discard BOOLEAN DEFAULT FALSE COMMENT '自动出牌',
    think_time INT DEFAULT 30 COMMENT '思考时间(秒)',
    score_rules JSON COMMENT '计分规则',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_name (config_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏配置表';

-- 系统日志表
CREATE TABLE system_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    log_level ENUM('DEBUG', 'INFO', 'WARN', 'ERROR') DEFAULT 'INFO' COMMENT '日志级别',
    module_name VARCHAR(50) NOT NULL COMMENT '模块名称',
    operation_type VARCHAR(50) COMMENT '操作类型',
    player_id VARCHAR(36) COMMENT '相关玩家ID',
    room_id BIGINT COMMENT '相关房间ID',
    game_id BIGINT COMMENT '相关游戏ID',
    log_message TEXT NOT NULL COMMENT '日志消息',
    exception_info TEXT COMMENT '异常信息',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_log_level (log_level),
    INDEX idx_module_name (module_name),
    INDEX idx_player_id (player_id),
    INDEX idx_room_id (room_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 聊天消息表
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    player_id VARCHAR(36) COMMENT '发送者ID(系统消息为空)',
    message_type ENUM('TEXT', 'EMOJI', 'SYSTEM') DEFAULT 'TEXT' COMMENT '消息类型',
    message_content TEXT NOT NULL COMMENT '消息内容',
    player_name VARCHAR(20) COMMENT '发送者名称',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_room_id (room_id),
    INDEX idx_player_id (player_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 在线用户表
CREATE TABLE online_users (
    id VARCHAR(36) PRIMARY KEY COMMENT '用户ID',
    room_id BIGINT COMMENT '当前房间ID',
    player_name VARCHAR(20) NOT NULL COMMENT '玩家昵称',
    connection_id VARCHAR(100) NOT NULL COMMENT 'WebSocket连接ID',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    spectator BOOLEAN DEFAULT FALSE COMMENT '是否为观战者',
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后心跳时间',
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '连接时间',
    INDEX idx_room_id (room_id),
    INDEX idx_connection_id (connection_id),
    INDEX idx_last_heartbeat (last_heartbeat),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在线用户表';

-- 游戏统计表
CREATE TABLE game_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    player_id VARCHAR(36) NOT NULL COMMENT '玩家ID',
    total_games INT DEFAULT 0 COMMENT '总游戏局数',
    total_wins INT DEFAULT 0 COMMENT '总获胜局数',
    total_score INT DEFAULT 0 COMMENT '总得分',
    max_score INT DEFAULT 0 COMMENT '最高得分',
    min_score INT DEFAULT 0 COMMENT '最低得分',
    avg_score DECIMAL(8,2) DEFAULT 0 COMMENT '平均得分',
    total_peng_count INT DEFAULT 0 COMMENT '总碰牌次数',
    total_gang_count INT DEFAULT 0 COMMENT '总杠牌次数',
    win_rate DECIMAL(5,2) DEFAULT 0 COMMENT '胜率(%)',
    last_played_at TIMESTAMP NULL COMMENT '最后游戏时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_room_id (room_id),
    INDEX idx_player_id (player_id),
    INDEX idx_last_played_at (last_played_at),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏统计表';

-- 插入默认游戏配置
INSERT INTO game_configs (config_name, base_score, max_rounds, allow_peng, allow_gang, allow_chi, require_tiles, mixed_tile_enabled, auto_discard, think_time, score_rules) VALUES
('标准配置', 1, 8, TRUE, TRUE, FALSE, TRUE, TRUE, FALSE, 30, '{"base_score": 1, "max_score": 100}'),
('快速配置', 1, 4, TRUE, TRUE, FALSE, TRUE, TRUE, FALSE, 15, '{"base_score": 1, "max_score": 50}'),
('竞技配置', 2, 16, TRUE, TRUE, FALSE, TRUE, TRUE, FALSE, 60, '{"base_score": 2, "max_score": 200}');

-- 创建视图：房间详情视图
CREATE VIEW room_details AS
SELECT
    r.id,
    r.room_number,
    r.room_name,
    r.creator_id,
    r.status as room_status,
    r.max_players,
    r.current_players,
    r.allow_spectate,
    r.spectator_count,
    r.created_at,
    r.updated_at,
    r.expires_at,
    (SELECT COUNT(*) FROM players p WHERE p.room_id = r.id AND p.spectator = FALSE) as actual_players,
    (SELECT COUNT(*) FROM players p WHERE p.room_id = r.id AND p.spectator = TRUE) as actual_spectators,
    (SELECT COUNT(*) FROM players p WHERE p.room_id = r.id AND p.player_status = 'READY') as ready_players
FROM rooms r;

-- 创建视图：游戏统计视图
CREATE VIEW player_game_stats AS
SELECT
    gs.id,
    gs.room_id,
    gs.player_id,
    p.player_name,
    gs.total_games,
    gs.total_wins,
    gs.total_score,
    gs.max_score,
    gs.min_score,
    gs.avg_score,
    gs.total_peng_count,
    gs.total_gang_count,
    gs.win_rate,
    gs.last_played_at,
    gs.created_at,
    gs.updated_at
FROM game_statistics gs
JOIN players p ON gs.player_id = p.id;