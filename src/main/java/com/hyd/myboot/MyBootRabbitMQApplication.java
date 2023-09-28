package com.hyd.myboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyBootRabbitMQApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBootRabbitMQApplication.class, args);
        System.out.println("启动成功");
    }

}
