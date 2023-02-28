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
package io.github.casuallc.admq;

import io.github.casuallc.admq.utils.ApusicUtils;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

@Component
@Slf4j
public class ConsumerPool implements BeanPostProcessor, EmbeddedValueResolverAware {

    private StringValueResolver stringValueResolver;

    @Autowired
    private PulsarClient pulsarClient;

    private final Map<String, Consumer<Object>> consumers = new ConcurrentHashMap<>();

    @Override
    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        this.stringValueResolver = stringValueResolver;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(AdmqConsumer.class) && bean instanceof ConsumerWrapper) {

            AdmqConsumer consumer = bean.getClass().getAnnotation(AdmqConsumer.class);

            String topic = ApusicUtils.getFullTopic(consumer.topic());

            ConsumerWrapper consumerWrapper = (ConsumerWrapper) bean;
            consumerWrapper.setTopic(topic);

            String id = ApusicUtils.getConsumerId(topic, consumer.consumerName());
            consumers.putIfAbsent(id, createConsumer(consumerWrapper, consumer));
        }

        return bean;
    }

    public Consumer<Object> createConsumer(ConsumerWrapper consumerWrapper, AdmqConsumer annotation) {
        try {
            ConsumerBuilder<Object> builder = pulsarClient.newConsumer(ApusicUtils.getSchema(annotation.schema()))
                    .consumerName(annotation.consumerName())
                    .subscriptionInitialPosition(annotation.subscriptionInitialPosition())
                    .subscriptionType(annotation.subscriptionType())
                    .subscriptionName(annotation.subscriptionName())
                    .topic(consumerWrapper.getTopic())
                    .receiverQueueSize(annotation.receiverQueueSize())
                    .negativeAckRedeliveryDelay(annotation.negativeAckRedeliveryDelay(), TimeUnit.MILLISECONDS)
                    .messageListener((MessageListener) (consumer, msg) -> {
                        try {
                            consumerWrapper.send(new AdmqMessage(msg, consumer));
                        } catch (Exception e) {
                            log.error("", e);
                            return;
                        }
                        if (annotation.autoAck()) {
                            try {
                                consumer.acknowledge(msg);
                            } catch (PulsarClientException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            if (annotation.enableRetry() && annotation.ackTimeout() > 0) {
                builder.enableRetry(true)
                        .ackTimeout(annotation.ackTimeout(), TimeUnit.MILLISECONDS);
            }

            if (StringUtils.isNoneBlank(annotation.deadLetterTopic()) && annotation.enableRetry()) {
                DeadLetterPolicy policy = DeadLetterPolicy.builder()
                        .deadLetterTopic(annotation.deadLetterTopic())
                        .maxRedeliverCount(annotation.maxRedeliverCount())
                        .build();
                builder.deadLetterPolicy(policy);
            }

            Consumer consumer = builder.subscribe();

            log.info("Create consumer {}/{} successful.", consumerWrapper.getTopic(), annotation.consumerName());
            return consumer;
        } catch (Exception e) {
            log.error("Create consumer {}/{} failed.", consumerWrapper.getTopic(), annotation.consumerName(), e);

            return null;
        }
    }
}
