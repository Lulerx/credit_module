package com.itle.credit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * create by Luler on 2023/2/2 17:41
 *
 * @description
 */
@SpringBootApplication
@MapperScan("com.itle.credit.mapper")
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
