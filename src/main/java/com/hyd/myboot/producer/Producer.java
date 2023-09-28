package com.hyd.myboot.producer;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.hyd.myboot.constant.Constant.*;

/**
 * @Author: hayden
 * @Date: 2023-09-28
 * @Description: 生产者
 */
@Component
public class Producer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public String sendDelayMessage(String message, int delayTime) {
        // 参数：交换机名称，路由键，消息内容，消息处理器（设置消息的延迟时间）
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE_NAME, DELAY_ROUTING_KEY, message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(delayTime);
                return message;
            }
        });
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("发送消息：" + message + "，延迟时间：" + delayTime + "ms" + "，当前时间：" + time);
        return "发送消息：" + message + "，延迟时间：" + delayTime + "ms" + "，当前时间：" + time;
    }
}
