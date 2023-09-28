package com.hyd.myboot.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.hyd.myboot.constant.Constant.*;

/**
 * @Author: hayden
 * @Date: 2023-09-28
 * @Description: RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 定义延迟队列
     */
    @Bean
    public Queue delayedQueue() {
        // 参数：name 队列名称，durable 是否持久化，exclusive 是否排他，autoDelete 是否自动删除
        return new Queue(DELAY_QUEUE_NAME, true, false, false);
    }

    /**
     * 定义延迟交换机
     */
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        // 参数：name 交换机名称，type 交换机类型，durable 是否持久化，autoDelete 是否自动删除，arguments 参数
        return new CustomExchange(DELAY_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    /**
     * 将延迟队列绑定到延迟交换机
     */
    @Bean
    public Binding delayBinding(Queue delayedQueue, CustomExchange delayedExchange) {
        // 参数：队列，交换机，路由键
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(DELAY_ROUTING_KEY).noargs();
    }
}

