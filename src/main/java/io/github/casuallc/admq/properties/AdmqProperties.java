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
package io.github.casuallc.admq.properties;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admq")
@Data
public class AdmqProperties {

    private String serviceUrl = "pulsar://localhost:6650";
    private boolean transactionEnabled = false;
    private String token;
    private String certFile;
    private String listenerName;

    public boolean validate() {
        if (StringUtils.isBlank(serviceUrl)) {
            return false;
        }
        return true;
    }
}
