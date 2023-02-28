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

import io.github.casuallc.admq.properties.AdmqProperties;
import io.github.casuallc.admq.properties.ConsumerProperties;
import io.github.casuallc.admq.properties.ProducerProperties;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableConfigurationProperties({AdmqProperties.class, ProducerProperties.class, ConsumerProperties.class})
@Slf4j
public class AdmqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PulsarClient client(AdmqProperties properties) throws Exception {
        if (!properties.validate()) {
            throw new IllegalArgumentException("Params valid failed.");
        }

        ClientBuilder builder = PulsarClient.builder()
                .enableTransaction(properties.isTransactionEnabled())
                .connectionTimeout(3, TimeUnit.MINUTES);
        if (!StringUtils.isBlank(properties.getToken())) {
            builder.authentication(AuthenticationFactory.token(properties.getToken()));
        }
        if (!StringUtils.isBlank(properties.getCertFile())) {
            builder.serviceUrl("pulsar+ssl://" + properties.getServiceUrl());
            builder.tlsTrustCertsFilePath(properties.getCertFile())
                    .enableTlsHostnameVerification(false)
                    .allowTlsInsecureConnection(false);
        } else {
            builder.serviceUrl("pulsar://" + properties.getServiceUrl());
        }
        if (!StringUtils.isBlank(properties.getListenerName())) {
            builder.listenerName(properties.getListenerName());
        }
        // 设置状态采集间隔
        builder.statsInterval(10, TimeUnit.SECONDS);

        log.info("Admq client init.");
        return builder.build();
    }
}
