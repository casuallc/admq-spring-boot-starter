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

import java.util.Map;
import lombok.Data;

@Data
public class MessageSendConfig {

    /**
     * 消息属性
     */
    private Map<String, String> properties;

    /**
     * 消息key，用于routing
     */
    private String key;

    /**
     * 消息ID，如果设置需要递增
     */
    private long sequenceId = -1;

    /**
     * 消息延迟设置，在设置的时间发送给消费者
     */
    private long deliverAtTime = -1;

    /**
     * 消息延迟设置，在多少秒后发送给消费者。该延迟时间不是严格准确的，同时需要确保客户端时间和服务端时间一样。
     */
    private long deliverAfterTime = -1;
}
