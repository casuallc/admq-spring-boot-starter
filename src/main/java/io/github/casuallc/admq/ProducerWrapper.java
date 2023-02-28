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

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;

@Slf4j
public class ProducerWrapper {

    private Producer producer;

    void setProducer(Producer producer) {
        if (this.producer == null) {
            this.producer = producer;
        } else {
            log.warn("Producer already set.");
        }
    }

    protected void send0(Object message) throws PulsarClientException {
        producer.send(message);
    }

    protected void sendAsync0(Object message) {
        producer.sendAsync(message);
    }
}
