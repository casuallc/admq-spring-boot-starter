package io.github.casuallc.admq;

import io.github.casuallc.admq.exception.MQException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;

public class AdmqMessage {

    private MessageId id;
    private Object value;
    private Consumer consumer;

    Consumer getConsumer() {
        return consumer;
    }

    MessageId getId() {
        return id;
    }

    public AdmqMessage(Message msg, Consumer consumer) {
        this.id = msg.getMessageId();
        this.value = msg.getValue();
        this.consumer = consumer;
    }

    public Object getValue() {
        return value;
    }

    public void acknowledge() throws MQException {
        try {
            consumer.acknowledge(id);
        } catch (Exception e) {
            throw new MQException(e);
        }
    }
}
