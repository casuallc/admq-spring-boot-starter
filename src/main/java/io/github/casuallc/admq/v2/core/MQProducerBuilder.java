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

import io.github.casuallc.admq.exception.MQClientException;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerAccessMode;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.Schema;

public class MQProducerBuilder<T> {

    @Getter
    private String topic;
    @Getter
    private String producerName;
    @Getter
    private ProducerAccessMode accessMode;
    @Getter
    private int maxPendingMessages;
    @Getter
    private boolean blockIfQueueFull;
    @Getter
    private int sendTimeout;
    @Getter
    private MessageRoutingMode messageRoutingMode;

    private MQFactory factory;
    private ProducerBuilder<T> builder;

    public MQProducerBuilder(MQFactory factory, String topic, Schema<T> schema) {
        this.topic = topic;
        this.factory = factory;
        this.builder = factory.getClient().newProducer(schema);
        this.builder.topic(topic);
    }

    public MQProducerBuilder<T> producerName(String producerName) {
        this.producerName = producerName;
        builder.producerName(producerName);
        return this;
    }

    /**
     * 设置发送模式
     * @param accessMode
     * @return
     */
    public MQProducerBuilder<T> accessMode(ProducerAccessMode accessMode) {
        this.accessMode = accessMode;
        builder.accessMode(accessMode);
        return this;
    }

    /**
     * 设置队列最大缓存大小
     * @param maxPendingMessages
     * @return
     */

    public MQProducerBuilder<T> maxPendingMessages(int maxPendingMessages) {
        this.maxPendingMessages = maxPendingMessages;
        builder.maxPendingMessages(maxPendingMessages);
        return this;
    }

    /**
     * 设置当发送队列满后是否阻塞
     * @param blockIfQueueFull
     * @return
     */
    public MQProducerBuilder<T> blockIfQueueFull(boolean blockIfQueueFull) {
        this.blockIfQueueFull = blockIfQueueFull;
        builder.blockIfQueueFull(blockIfQueueFull);
        return this;
    }

    /**
     * 设置消息发送超时，单位毫秒。
     * @param sendTimeout
     * @return
     */
    public MQProducerBuilder<T> sendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
        builder.sendTimeout(sendTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 设置路由策略
     * @param messageRoutingMode
     * @return
     */
    public MQProducerBuilder<T> messageRoutingMode(MessageRoutingMode messageRoutingMode) {
        this.messageRoutingMode = messageRoutingMode;
        builder.messageRoutingMode(messageRoutingMode);
        return this;
    }

    public MQTemplate<T> build() throws MQClientException {
        try {
            return new MQTemplate<T>(builder.create());
        } catch (Exception e) {
            throw new MQClientException("", e);
        }
    }
}
