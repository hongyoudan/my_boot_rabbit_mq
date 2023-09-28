package com.hyd.myboot.controller;

import com.hyd.myboot.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: hayden
 * @Date: 2023-09-28
 * @Description: 控制器
 */
@RestController
public class SendMessageController {

    @Autowired
    private Producer producer;

    @GetMapping("/sendMessage")
    public String sendMessage(@RequestParam("message") String message, @RequestParam("delayTime") int delayTime) {
        return producer.sendDelayMessage(message, delayTime);
    }
}
