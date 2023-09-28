package com.hyd.myboot;

import com.hyd.myboot.producer.Producer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class MyBootRabbitMQApplicationTests {

    @Autowired
    private Producer producer;

    @Test
    void testSendMessage() {
        String message = "hello";
        int delayTime = 5000;
        producer.sendDelayMessage(message, delayTime);
    }

}
