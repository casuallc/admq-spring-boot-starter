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
package io.github.casuallc.admq.v2.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionType;

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MQListener {

    String topic();

    Class<?> schema() default byte[].class;

    boolean autoAck() default true;

    String subscriptionName();

    SubscriptionType subscriptionType() default SubscriptionType.Shared;

    long ackTimeout() default 5000;

    long negativeAckRedeliveryDelay() default 1000 * 60;

    int receiverQueueSize() default 1000;

    String consumerName() default "consumer";

    SubscriptionInitialPosition subscriptionInitialPosition() default SubscriptionInitialPosition.Latest;

    boolean enableRetry() default false;

    String deadLetterTopic() default "";

    int maxRedeliverCount() default 5;
}
