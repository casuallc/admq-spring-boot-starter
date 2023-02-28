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
package io.github.casuallc.admq.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.naming.TopicName;

public class ApusicUtils {

    public static final String DEFAULT_TENANT = "public";
    public static final String DEFAULT_NAMESPACE = "default";

    public static String getProducerId(String topic, String producerName) {
        if (StringUtils.isBlank(producerName)) {
            return topic + "/" + topic;
        }
        return topic + "/" + producerName;
    }

    public static String getConsumerId(String topic, String consumerName) {
        if (StringUtils.isBlank(consumerName)) {
            return topic + "/" + topic;
        }
        return topic + "/" + consumerName;
    }

    public static String getFullTopic(String topic) {
        return TopicName.get(topic).getRestPath(false);
    }

    public static Schema getSchema(Class clazz) {
        if (clazz == byte[].class) {
            return Schema.BYTES;
        } else if (clazz == String.class) {
            return Schema.STRING;
        }

        return Schema.BYTES;
    }
}
