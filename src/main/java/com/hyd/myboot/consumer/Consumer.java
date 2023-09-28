package com.hyd.myboot.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.hyd.myboot.constant.Constant.*;

/**
 * @Author: hayden
 * @Date: 2023-09-28
 * @Description: 消费者
 */
@Component
public class Consumer {

    @RabbitListener(queues = DELAY_QUEUE_NAME)
    public void processDelayMessage(String message) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("消费消息: " + message + "，当前时间：" + time);
    }
}
