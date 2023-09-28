# SpringBoot整合RabbitMQ实现延迟队列功能

![请添加图片描述](https://img-blog.csdnimg.cn/77cf07e6a61c43d389eafd03aa0cde2b.gif)
> 👨🏻‍💻 热爱摄影的程序员
> 👨🏻‍🎨 喜欢编码的设计师
> 🧕🏻 擅长设计的剪辑师
> 🧑🏻‍🏫 一位高冷无情的编码爱好者
> 大家好，我是 DevOps 工程师
> 欢迎分享 / 收藏 / 赞 / 在看！

延迟队列是一种常见的消息队列模式，用于在一定时间后处理消息。在本文中，我们将探讨如何使用 Spring Boot 和 RabbitMQ 实现延迟队列功能。

通常情况下，生产者将消息发送给普通交换机，由普通交换机发送给普通的队列给消费者进行消费，当消息被拒绝、消息 TTL 过期或者队列达到最大长度时，消息成为死信消息，将被丢弃给死信交换机处理，由死信交换机发送给死信队列进行下一步处理。

但是，实现死信队列上述步骤可以简化为：生产者直接将消息发送给死信交换机并设置过期时间，如果消息 TTL 达到，则被丢弃到死信队列，而此时监听着死信队列的消费者就可以及时地消费到消息。由此可以通过死信队列的机制实现延迟队列的功能。
![请添加图片描述](https://img-blog.csdnimg.cn/9d6665eff758451b9c72a009b115f78d.png)



首先，使用 Docker 构建好 RabbitMQ 容器服务。注意，笔者使用的是自行构建的带有延迟插件的镜像 `hongyoudan/rabbitmq-management-delayed:3.12`，大家可以使用笔者构建好的镜像直接创建容器，如果使用其他镜像需关注是否安装了延迟插件 `rabbitmq_delayed_message_exchange`，如果未安装，需自行安装，因为要实现 RabbitMQ 延迟队列的功能，需要开启该插件。

```bash
docker run -d \
--name rabbitmq \
-p 5672:5672 -p 15672:15672 \
hongyoudan/rabbitmq-management-delayed:3.12
```


初始化项目，使用 Spring 初始化器新建项目。

引入 Web 和 AMQP 两个依赖项：

```xml
<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-amqp</artifactId>
 </dependency>
 <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-web</artifactId>
 </dependency>
```

编写配置文件：

```yml
server:
  port: 3031

spring:
  rabbitmq:
    addresses: 127.0.0.1
    port: 5623
    username: guest
    password: guest
```


编写 RabbitMQ 配置类：

```java
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
```

通用常量类：

```java
/**
 * @Author: hayden
 * @Date: 2023-09-28
 * @Description: 常量类
 */
public class Constant {

    public static final String DELAY_QUEUE_NAME = "delay_queue";

    public static final String DELAY_EXCHANGE_NAME = "delay_exchange";

    public static final String DELAY_ROUTING_KEY = "delay_routing_key";
}
```

生产者：


```java
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
```

消费者：

```java
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
```


控制器：

```java
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
```

完整项目结构：

![在这里插入图片描述](https://img-blog.csdnimg.cn/c3ead87fc5564118b1a25611508f42c9.png)

使用接口测试工具 Apifox 进行测试。测试发送消息为：aaa，延迟时间为 5 秒。在控制台中可以看到在发送消息 5 秒后，消息才被消费。

![在这里插入图片描述](https://img-blog.csdnimg.cn/a6342c42cb944d2194e303e35b8b54dc.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/c2d3f237aa1a477c9191764ff4097c2f.png)
