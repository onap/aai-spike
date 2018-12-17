/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.spike;

import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.eclipse.jetty.util.security.Password;
import org.onap.aai.setup.AAIConfigTranslator;
import org.onap.aai.setup.ConfigTranslator;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Spike service Spring Boot Application
 */
@SpringBootApplication
@ComponentScan(basePackages = {"org.onap.aai.config", "org.onap.aai.setup", "org.onap.aai.spike"})
@PropertySource(value = "file:${schema.ingest.file}", ignoreResourceNotFound = true)
@PropertySource(value = "file:${edgeprops.ingest.file}", ignoreResourceNotFound = true)
@ImportResource({"file:${SERVICE_BEANS}/*.xml"})
public class SpikeApplication extends SpringBootServletInitializer {
    @Autowired
    private Environment env;

    public static void main(String[] args) {
        String keyStorePassword = System.getProperty("KEY_STORE_PASSWORD");
        if (keyStorePassword == null || keyStorePassword.isEmpty()) {
            throw new IllegalArgumentException("System Property KEY_STORE_PASSWORD not set");
        }

        HashMap<String, Object> props = new HashMap<>();
        props.put("server.ssl.key-store-password", Password.deobfuscate(keyStorePassword));
        props.put("schema.service.ssl.key-store-password", Password.deobfuscate(keyStorePassword));
        props.put("schema.service.ssl.trust-store-password", Password.deobfuscate(keyStorePassword));

        new SpikeApplication().configure(new SpringApplicationBuilder(SpikeApplication.class).properties(props))
                .run(args);
    }

    /**
     * Set required trust store system properties using values from application.properties
     */
    @PostConstruct
    public void setSystemProperties() {
        String trustStorePath = env.getProperty("server.ssl.key-store");
        if (trustStorePath != null) {
            String trustStorePassword = env.getProperty("server.ssl.key-store-password");

            if (trustStorePassword != null) {
                System.setProperty("javax.net.ssl.trustStore", trustStorePath);
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            } else {
                throw new IllegalArgumentException("Env property server.ssl.key-store-password not set");
            }
        }
    }

    @Bean
    @ConditionalOnExpression("'${schema.translator.list}'.contains('config')")
    public ConfigTranslator configTranslator(SchemaLocationsBean schemaLocationsBean, SchemaVersions schemaVersions) {
        return new AAIConfigTranslator(schemaLocationsBean, schemaVersions);
    }

}
