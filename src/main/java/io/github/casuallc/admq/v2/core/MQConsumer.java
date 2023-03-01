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

import io.github.casuallc.admq.exception.MQConsumerException;
import io.github.casuallc.admq.v2.listener.adapter.MessageListenerAdapter;
import org.apache.pulsar.client.api.Consumer;

public class MQConsumer {

    private Consumer<?> consumer;

    private MessageListenerAdapter adapter;

    public MQConsumer(Consumer<?> consumer) {
        this.consumer = consumer;
    }

    public void setAdapter(MessageListenerAdapter adapter) {
        this.adapter = adapter;
    }

    public void ack(MQMessage message) throws MQConsumerException {
        try {
            consumer.acknowledge(message.getId());
        } catch (Exception e) {
            throw new MQConsumerException("Ack failed.", e);
        }
    }
}
