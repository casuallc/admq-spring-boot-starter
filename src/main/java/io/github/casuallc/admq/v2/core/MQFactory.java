/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.casuallc.admq.v2.core;

import io.github.casuallc.admq.utils.ApusicUtils;
import io.github.casuallc.admq.v2.annotation.MQListener;
import io.github.casuallc.admq.v2.listener.adapter.MessageListenerAdapter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MQFactory {

    @Autowired
    private PulsarClient pulsarClient;

    private Map<String, MQTemplate> producers = new ConcurrentHashMap<>();
    private Map<String, MQConsumer> consumers = new ConcurrentHashMap<>();

    PulsarClient getClient() {
        return pulsarClient;
    }

    /**
     * 创建producer并缓存.
     * @param topic
     * @param schema
     * @return
     * @param <T>
     */
    public <T> MQProducerBuilder<T> newProducerBuilder(String topic, Schema<T> schema) {
        return new MQProducerBuilder<T>(this, topic, schema);
    }

    /**
     * 根据topic和producerName获取缓存中的producer，如果为空则返回null.
     * @param topic
     * @param producerName
     * @return
     * @param <T>
     */
    public <T> MQTemplate<T> getProducer(String topic, String producerName) {
        String id = ApusicUtils.getProducerId(topic, producerName);
        return producers.get(id);
    }

    public <T> MQTemplate<T> createIfNotCached(String topic, String producerName, Schema<T> schema) {
        String id = ApusicUtils.getProducerId(topic, producerName);
        return producers.computeIfAbsent(id, a -> {
            try {
                return newProducerBuilder(topic, schema).producerName(producerName).build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void registerConsumer(MQListener listener, MessageListenerAdapter adapter) {
        String id = ApusicUtils.getConsumerId(listener.topic(), listener.consumerName());

        consumers.computeIfAbsent(id, a -> {
            try {
                String topic = ApusicUtils.getFullTopic(listener.topic());
                adapter.setTopic(topic);
                return new MQConsumer(createConsumer(adapter, topic, listener));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

//        if (consumer != null) {
//            log.info("[{}/{}] Consumer already registered.", listener.topic(), listener.consumerName());
//        }

    }

    public Consumer<?> createConsumer(MessageListenerAdapter adapter, String topic, MQListener listener) {
        try {
            ConsumerBuilder<?> builder = pulsarClient.newConsumer(ApusicUtils.getSchema(listener.schema()))
                    .consumerName(listener.consumerName())
                    .subscriptionInitialPosition(listener.subscriptionInitialPosition())
                    .subscriptionType(listener.subscriptionType())
                    .subscriptionName(listener.subscriptionName())
                    .topic(topic)
                    .receiverQueueSize(listener.receiverQueueSize())
                    .negativeAckRedeliveryDelay(listener.negativeAckRedeliveryDelay(), TimeUnit.MILLISECONDS)
                    .messageListener((MessageListener) (consumer, msg) -> {
                        try {
                            adapter.invoke(new MQMessage(msg), consumer);
                        } catch (Exception e) {
                            log.error("", e);
                            return;
                        }
                        if (listener.autoAck()) {
                            try {
                                consumer.acknowledge(msg);
                            } catch (PulsarClientException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            if (listener.enableRetry() && listener.ackTimeout() > 0) {
                builder.enableRetry(true)
                        .ackTimeout(listener.ackTimeout(), TimeUnit.MILLISECONDS);
            }

            if (StringUtils.isNoneBlank(listener.deadLetterTopic()) && listener.enableRetry()) {
                DeadLetterPolicy policy = DeadLetterPolicy.builder()
                        .deadLetterTopic(listener.deadLetterTopic())
                        .maxRedeliverCount(listener.maxRedeliverCount())
                        .build();
                builder.deadLetterPolicy(policy);
            }

            Consumer<?> consumer = builder.subscribe();

            log.info("Create consumer {}/{} successful.", topic, listener.consumerName());
            return consumer;
        } catch (Exception e) {
            log.error("Create consumer {}/{} failed.", topic, listener.consumerName(), e);

            return null;
        }
    }
}
