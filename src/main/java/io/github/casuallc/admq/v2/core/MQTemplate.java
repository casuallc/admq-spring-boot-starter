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

import io.github.casuallc.admq.exception.MQProducerException;
import io.github.casuallc.admq.v2.listener.MQProducerListener;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.TypedMessageBuilder;

public class MQTemplate<T> {

    private Producer<T> producer;

    public MQTemplate(Producer<T> producer) {
        this.producer = producer;
    }

    public void send(T message) throws MQProducerException {
        send(message, null);
    }

    public void sendAsync(T message, MQProducerListener<T> producerListener) throws MQProducerException {
        sendAsync(message, producerListener, null);
    }

    public void send(T message, MessageSendConfig config) throws MQProducerException {
        try {
            newMessage(message, config).send();
        } catch (Exception e) {
            throw new MQProducerException("message send failed.", e);
        }
    }

    public void sendAsync(T message, MQProducerListener<T> producerListener, MessageSendConfig config)
            throws MQProducerException {
        try {
            doSend(message, producerListener, config);
        } catch (Exception e) {
            throw new MQProducerException("message send failed.", e);
        }
    }

    private void doSend(T message, MQProducerListener<T> producerListener, MessageSendConfig config) {
        newMessage(message, config).sendAsync().thenAccept(messageId -> {
            if (producerListener != null) {
                producerListener.onSuccess(message);
            }
        }).exceptionally(e -> {
            if (producerListener != null) {
                producerListener.onError(message, e);
            }
            return null;
        });
    }

    private TypedMessageBuilder<T> newMessage(T message, MessageSendConfig config) {
        if (config == null) {
            return producer.newMessage().value(message);
        }

        TypedMessageBuilder<T> builder = producer.newMessage();
        if (config.getProperties() != null) {
            builder.properties(config.getProperties());
        }
        if (config.getKey() != null) {
            builder.key(config.getKey());
        }
        if (config.getSequenceId() != -1) {
            builder.sequenceId(config.getSequenceId());
        }
        if (config.getDeliverAtTime() != -1) {
            builder.deliverAt(config.getDeliverAtTime());
        }
        if (config.getDeliverAfterTime() != -1) {
            builder.deliverAfter(config.getDeliverAfterTime(), TimeUnit.SECONDS);
        }

        return builder.value(message);
    }
}
