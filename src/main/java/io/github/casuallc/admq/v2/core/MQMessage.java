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

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;

public class MQMessage {
    private Message<?> message;

    private Object value;

    private MessageId id;

    public MQMessage(Message<?> message) {
        this.message = message;
        this.value = message.getValue();
        this.id = message.getMessageId();
    }

    public Object getValue() {
        return value;
    }

    MessageId getId() {
        return id;
    }
}
