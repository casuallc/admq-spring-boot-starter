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
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

@Component
@Slf4j
public class ProducerPool implements BeanPostProcessor, EmbeddedValueResolverAware {

    private StringValueResolver stringValueResolver;

    @Autowired
    private PulsarClient pulsarClient;

    private final Map<String, Producer<Object>> producers = new ConcurrentHashMap<>();


    @Override
    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        this.stringValueResolver = stringValueResolver;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean.getClass().isAnnotationPresent(AdmqProducer.class) && bean instanceof ProducerWrapper) {

            AdmqProducer producer = bean.getClass().getAnnotation(AdmqProducer.class);
            String topic = ApusicUtils.getFullTopic(producer.topic());
            ProducerWrapper producerWrapper = (ProducerWrapper) bean;
            String id = ApusicUtils.getProducerId(topic, producer.producerName());

            producers.putIfAbsent(id, createProducer(producer, topic));
            producerWrapper.setProducer(producers.get(id));
        }

        return bean;
    }

    private Producer<Object> createProducer(AdmqProducer annotation, String topic) {
        try {
            Producer<Object> producer = pulsarClient.newProducer(ApusicUtils.getSchema(annotation.schema()))
                    .topic(topic)
                    .producerName(annotation.producerName())
                    .accessMode(annotation.accessMode())
                    .create();

            log.info("Create producer {}/{} successful.", topic, annotation.producerName());
            return producer;
        } catch (Exception e) {
            log.error("Create producer {}/{} failed.", topic, annotation.producerName(), e);
            return null;
        }
    }
}
