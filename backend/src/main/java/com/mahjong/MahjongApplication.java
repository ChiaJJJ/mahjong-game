package com.mahjong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 河南麻将线上游戏主应用类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class MahjongApplication {

    public static void main(String[] args) {
        SpringApplication.run(MahjongApplication.class, args);
    }

}