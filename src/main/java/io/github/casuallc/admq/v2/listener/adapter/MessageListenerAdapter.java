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
package io.github.casuallc.admq.v2.listener.adapter;

import io.github.casuallc.admq.exception.MQConsumerException;
import io.github.casuallc.admq.v2.core.MQConsumer;
import io.github.casuallc.admq.v2.core.MQMessage;
import java.lang.reflect.Method;
import org.apache.pulsar.client.api.Consumer;

public class MessageListenerAdapter {

    private static final int TYPE_AUTO_ACK = 1;
    private static final int TYPE_ = 2;

    private Object bean;
    private Method method;
    private int listenerType;
    private String topic;

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public void setHandleMethod(Method method) {
        this.method = method;
    }

    public void setListenerType(int listenerType) {
        this.listenerType = listenerType;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void invoke(MQMessage message, Consumer<?> consumer) throws MQConsumerException {
        try {
            method.setAccessible(true);
            if (listenerType == TYPE_AUTO_ACK) {
                method.invoke(bean, message);
            } else {
                method.invoke(bean, message, new MQConsumer(consumer));
            }
        } catch (Exception e) {
            throw new MQConsumerException("Consume failed.", e);
        }
    }

}
