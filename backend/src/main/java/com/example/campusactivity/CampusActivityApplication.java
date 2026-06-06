package com.example.campusactivity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园活动报名系统 - 启动类
 * @author 课设导师
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.example.campusactivity.mapper")
public class CampusActivityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusActivityApplication.class, args);
    }

}
