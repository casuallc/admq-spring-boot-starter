# admq-spring-boot-starter

金蝶Apusic分布式消息队列软件(Apusic Distributed Message Queue，ADMQ) 客户端SDK。

## 支持的功能
- 同步发送消息
- 异步发送消息
- 同步接收消息
- 异步接收消息
- 消息手动确认
- 消息消费失败重试
- 死信主题
- 自定义 Schema

## 使用方式
- 引入 maven 依赖
```xml
<dependency>
  <groupId>io.github.casuallc</groupId>
  <artifactId>admq-spring-boot-starter</artifactId>
  <version>2.4.4</version>
</dependency>

```
- 初始化生产者
```java
@Slf4j
@Component
public class MyProducer extends ProducerWrapper {

    @Autowired
    private MQFactory mqFactory;

    public void send(String topic, String msg) throws Exception {
        MQTemplate<String> template = mqFactory.createIfNotCached(topic, "service", Schema.STRING);
        template.send(msg);
        log.info("发送消息：{} 到主题：{}", msg, topic);
    }

    public void sendAsync(String topic, String msg) throws Exception {
        MQTemplate<String> template = mqFactory.createIfNotCached(topic, "service", Schema.STRING);
        template.sendAsync(msg, new MQProducerListener<String>() {

            @Override
            public void onSuccess(Object message) {
                log.info("发送消息：{} 到主题：{}", msg, topic);
            }

            @Override
            public void onError(Object message, Throwable exception) {
                log.error("发送消息：{} 到主题：{}", msg, topic, exception);
            }
        });
    }
}
```
- 初始化消费者
```java
@Slf4j
@Component
public class MyConsumer {

    @MQListener(topic = "topic101", schema = String.class, subscriptionName = "sub01",
            deadLetterTopic = "topic01_dead", autoAck = false, enableRetry = true)
    public void onReceive(MQMessage message, MQConsumer consumer) throws MQConsumerException {
        log.info("主题{}接收到消息 {}", "topic101", message.getValue());
    }

    @MQListener(topic = "topic102", schema = String.class, subscriptionName = "sub01")
    public void onReceive(MQMessage message) throws MQConsumerException {
        log.info("主题{}接收到消息 {}", "topic102", message.getValue());
    }

    @MQListener(topic = "topic103", schema = String.class, subscriptionName = "sub01", autoAck = false)
    public void onReceive2(MQMessage message, MQConsumer consumer) throws MQConsumerException {
        log.info("主题{}接收到消息 {}", "topic103", message.getValue());
        consumer.ack(message);
    }

    @MQListener(topic = "topic103", schema = String.class, subscriptionName = "sub01",
            subscriptionType = SubscriptionType.Exclusive)
    public void onReceiveMultiType(MQMessage message) throws MQConsumerException {
        log.info("主题{}接收到消息 {}", "topic103", message.getValue());
    }
}

```