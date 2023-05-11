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
package io.github.casuallc;

import io.github.casuallc.admq.v2.core.MQFactory;
import org.apache.pulsar.client.api.Schema;
import org.junit.Test;

public class ProducerTest {

    @Test
    public void testBuild() throws Exception {
        String topic = "topic01";
        String producerName = "topic01";
        MQFactory factory = new MQFactory();
        String message = "123";

        factory.createIfNotCached(factory.newProducerBuilder(topic, Schema.STRING), producerName)
                .send(message);
    }

}
