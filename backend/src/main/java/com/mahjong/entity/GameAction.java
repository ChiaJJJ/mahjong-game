package com.mahjong.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 游戏操作记录实体类
 * 记录游戏中所有的操作历史
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Entity
@Table(name = "game_actions", indexes = {
    @Index(name = "idx_game_id", columnList = "game_id"),
    @Index(name = "idx_player_id", columnList = "player_id"),
    @Index(name = "idx_action_type", columnList = "action_type"),
    @Index(name = "idx_round_number", columnList = "round_number"),
    @Index(name = "idx_action_order", columnList = "action_order"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAction {

    /**
     * 操作ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属游戏
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_action_game"))
    private Game game;

    /**
     * 所属回合
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_round_id", foreignKey = @ForeignKey(name = "fk_action_round"))
    private GameRound gameRound;

    /**
     * 操作玩家ID
     */
    @Column(name = "player_id", nullable = false, length = 36)
    private String playerId;

    /**
     * 操作玩家昵称
     */
    @Column(name = "player_name", nullable = false, length = 20)
    private String playerName;

    /**
     * 玩家位置
     */
    @Column(name = "player_position", nullable = false)
    private Integer playerPosition;

    /**
     * 回合号
     */
    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    /**
     * 操作顺序
     */
    @Column(name = "action_order", nullable = false)
    private Integer actionOrder;

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    /**
     * 具体操作内容
     */
    @Column(name = "action_content", length = 100)
    private String actionContent;

    /**
     * 操作数据（JSON格式）
     */
    @Column(name = "action_data", columnDefinition = "JSON")
    private String actionData;

    /**
     * 相关牌的信息（JSON格式）
     */
    @Column(name = "tile_info", columnDefinition = "JSON")
    private String tileInfo;

    /**
     * 目标玩家ID（用于吃、碰、杠等操作）
     */
    @Column(name = "target_player_id", length = 36)
    private String targetPlayerId;

    /**
     * 目标玩家昵称
     */
    @Column(name = "target_player_name", length = 20)
    private String targetPlayerName;

    /**
     * 操作时间（毫秒）
     */
    @Column(name = "action_time_ms")
    private Long actionTimeMs;

    /**
     * 是否有效操作
     */
    @Column(name = "bool_valid", nullable = false)
    private Boolean boolValid = true;

    /**
     * 是否为AI操作
     */
    @Column(name = "ai_action", nullable = false)
    private Boolean aiAction = false;

    /**
     * 操作结果
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_result")
    private ActionResult actionResult;

    /**
     * 错误信息（如果操作失败）
     */
    @Column(name = "error_message", length = 200)
    private String errorMessage;

    /**
     * 操作IP地址
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * 设备标识
     */
    @Column(name = "device_id", length = 100)
    private String deviceId;

    /**
     * 会话ID
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 操作类型枚举
     */
    public enum ActionType {
        /**
         * 游戏开始
         */
        GAME_START("游戏开始"),

        /**
         * 摸牌
         */
        DRAW("摸牌"),

        /**
         * 出牌
         */
        DISCARD("出牌"),

        /**
         * 吃牌
         */
        CHI("吃牌"),

        /**
         * 碰牌
         */
        PENG("碰牌"),

        /**
         * 明杠
         */
        MINGANG("明杠"),

        /**
         * 暗杠
         */
        ANGANG("暗杠"),

        /**
         * 补杠
         */
        BUGANG("补杠"),

        /**
         * 胡牌
         */
        HU("胡牌"),

        /**
         * 过牌
         */
        PASS("过牌"),

        /**
         * 自摸
         */
        ZIMO("自摸"),

        /**
         * 抢杠胡
         */
        QIANGGANGHU("抢杠胡"),

        /**
         * 杠上炮
         */
        GANGSHANGPAO("杠上炮"),

        /**
         * 杠上开花
         */
        GANGSHANGKAI("杠上开花"),

        /**
         * 海底捞月
         */
        HAIDILAOYUE("海底捞月"),

        /**
         * 妙手回春
         */
        MIAOSHOUHUICHUN("妙手回春"),

        /**
         * 流局
         */
        LIUJU("流局"),

        /**
         * 重连
         */
        RECONNECT("重连"),

        /**
         * 离线
         */
        OFFLINE("离线"),

        /**
         * 重新开始
         */
        RESTART("重新开始");

        private final String description;

        ActionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 检查是否为牌操作
         */
        public boolean isTileAction() {
            return this == DRAW || this == DISCARD || this == CHI ||
                   this == PENG || this == MINGANG || this == ANGANG || this == BUGANG;
        }

        /**
         * 检查是否为胡牌操作
         */
        public boolean isWinAction() {
            return this == HU || this == ZIMO || this == QIANGGANGHU ||
                   this == GANGSHANGPAO || this == GANGSHANGKAI ||
                   this == HAIDILAOYUE || this == MIAOSHOUHUICHUN;
        }

        /**
         * 检查是否为杠牌操作
         */
        public boolean isGangAction() {
            return this == MINGANG || this == ANGANG || this == BUGANG;
        }
    }

    /**
     * 操作结果枚举
     */
    public enum ActionResult {
        /**
         * 成功
         */
        SUCCESS("成功"),

        /**
         * 失败
         */
        FAILED("失败"),

        /**
         * 超时
         */
        TIMEOUT("超时"),

        /**
         * 取消
         */
        CANCELLED("取消"),

        /**
         * 等待确认
         */
        PENDING("等待确认");

        private final String description;

        ActionResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 检查是否为成功状态
         */
        public boolean isSuccess() {
            return this == SUCCESS;
        }

        /**
         * 检查是否为失败状态
         */
        public boolean isFailed() {
            return this == FAILED || this == TIMEOUT;
        }
    }

    /**
     * 检查是否为有效操作
     */
    public boolean isValidAction() {
        return boolValid != null && boolValid;
    }

    /**
     * 检查是否为AI操作
     */
    public boolean isAIAction() {
        return aiAction != null && aiAction;
    }

    /**
     * 检查是否为牌操作
     */
    public boolean isTileAction() {
        return actionType != null && actionType.isTileAction();
    }

    /**
     * 检查是否为胡牌操作
     */
    public boolean isWinAction() {
        return actionType != null && actionType.isWinAction();
    }

    /**
     * 检查是否为杠牌操作
     */
    public boolean isGangAction() {
        return actionType != null && actionType.isGangAction();
    }

    /**
     * 检查操作是否成功
     */
    public boolean isActionSuccessful() {
        return actionResult != null && actionResult.isSuccess();
    }

    /**
     * 检查操作是否失败
     */
    public boolean isActionFailed() {
        return actionResult != null && actionResult.isFailed();
    }

    /**
     * 设置操作成功
     */
    public void setSuccess() {
        this.actionResult = ActionResult.SUCCESS;
        this.boolValid = true;
    }

    /**
     * 设置操作失败
     */
    public void setFailed(String errorMessage) {
        this.actionResult = ActionResult.FAILED;
        this.errorMessage = errorMessage;
        this.boolValid = false;
    }

    /**
     * 设置操作超时
     */
    public void setTimeout() {
        this.actionResult = ActionResult.TIMEOUT;
        this.boolValid = false;
        this.errorMessage = "操作超时";
    }

    /**
     * 获取操作持续时间
     */
    public Long getActionDuration() {
        return actionTimeMs != null ? actionTimeMs : 0L;
    }

    /**
     * 创建简单的游戏操作记录
     */
    public static GameAction createSimpleAction(Game game, String playerId, String playerName,
                                          Integer playerPosition, ActionType actionType,
                                          String actionContent) {
        return GameAction.builder()
                .game(game)
                .playerId(playerId)
                .playerName(playerName)
                .playerPosition(playerPosition)
                .actionType(actionType)
                .actionContent(actionContent)
                .boolValid(true)
                .actionResult(ActionResult.SUCCESS)
                .build();
    }

    /**
     * 创建牌操作记录
     */
    public static GameAction createTileAction(Game game, GameRound gameRound, String playerId,
                                          String playerName, Integer playerPosition,
                                          ActionType actionType, String tileInfo) {
        return GameAction.builder()
                .game(game)
                .gameRound(gameRound)
                .playerId(playerId)
                .playerName(playerName)
                .playerPosition(playerPosition)
                .roundNumber(gameRound != null ? gameRound.getRoundNumber() : 0)
                .actionType(actionType)
                .tileInfo(tileInfo)
                .boolValid(true)
                .actionResult(ActionResult.SUCCESS)
                .build();
    }

    /**
     * 创建带目标的操作记录
     */
    public static GameAction createTargetAction(Game game, String playerId, String playerName,
                                           Integer playerPosition, ActionType actionType,
                                           String targetPlayerId, String targetPlayerName) {
        return GameAction.builder()
                .game(game)
                .playerId(playerId)
                .playerName(playerName)
                .playerPosition(playerPosition)
                .actionType(actionType)
                .targetPlayerId(targetPlayerId)
                .targetPlayerName(targetPlayerName)
                .boolValid(true)
                .actionResult(ActionResult.SUCCESS)
                .build();
    }
}