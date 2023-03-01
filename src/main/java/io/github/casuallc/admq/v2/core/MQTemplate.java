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
import org.apache.pulsar.client.api.Producer;

public class MQTemplate<T> {

    private Producer<T> producer;

    public MQTemplate(Producer<T> producer) {
        this.producer = producer;
    }

    public void send(T message) throws MQProducerException {
        try {
            producer.send(message);
        } catch (Exception e) {
            throw new MQProducerException("message send failed.", e);
        }
    }

    public void sendAsync(T message, MQProducerListener<T> producerListener) throws MQProducerException {
        try {
            doSend(message, producerListener);
        } catch (Exception e) {
            throw new MQProducerException("message send failed.", e);
        }
    }

//    public void sendAsync(T message) {
//
//    }

    private void doSend(T message, MQProducerListener<T> producerListener) {
        producer.sendAsync(message).thenAccept(messageId -> {
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
}
