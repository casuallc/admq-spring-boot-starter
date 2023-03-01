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
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerAccessMode;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.Schema;

public class MQProducerBuilder<T> {

    private MQFactory factory;
    private ProducerBuilder<T> builder;

    public MQProducerBuilder(MQFactory factory, String topic, Schema<T> schema) {
        this.factory = factory;
        this.builder = factory.getClient().newProducer(schema);
        this.builder.topic(topic);
    }

    public MQProducerBuilder<T> producerName(String producerName) {
        builder.producerName(producerName);
        return this;
    }

    public MQProducerBuilder<T> accessMode(ProducerAccessMode accessMode) {
        builder.accessMode(accessMode);
        return this;
    }

    public MQProducerBuilder<T> maxPendingMessages(int maxPendingMessages) {
        builder.maxPendingMessages(maxPendingMessages);
        return this;
    }

    public MQProducerBuilder<T> blockIfQueueFull(boolean blockIfQueueFull) {
        builder.blockIfQueueFull(blockIfQueueFull);
        return this;
    }

    public MQProducerBuilder<T> sendTimeout(int sendTimeout) {
        builder.sendTimeout(sendTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    public MQProducerBuilder<T> messageRoutingMode(MessageRoutingMode messageRoutingMode) {
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
