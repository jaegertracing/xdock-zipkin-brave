/**
 * Copyright 2017 The Jaeger Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jaegertracing.xdock.zipkin;

import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

import brave.Tracing;
import brave.sampler.Sampler;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Encoding;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

/**
 * Application used in Jaeger crossdock test to verify that server can consume Zipkin data format.
 *
 * @author Pavol Loffay
 */
@SpringBootApplication
public class Application {
    private static final String SERVICE_NAME_PREFIX = "crossdock-zipkin-brave";

    public static void main(String []args) {
        SpringApplication.run(Application.class, args);
    }

    public String getServiceName() {
        return String.format("%s-%s", SERVICE_NAME_PREFIX, encoding).toLowerCase();
    }

    @Value("${zipkin.encoding}")
    private Encoding encoding;

    @Bean
    public AsyncReporter<Span> reporter() {
        Sender sender = OkHttpSender.builder()
                .endpoint("http://test_driver:9411/api/v1/spans")
                .encoding(encoding)
                .build();
        return AsyncReporter.builder(sender).build();
    }

    @Bean
    public Tracing tracer(Reporter<Span> reporter) {
        return Tracing.newBuilder()
                .localServiceName(getServiceName())
                .sampler(Sampler.ALWAYS_SAMPLE)
                .traceId128Bit(true)
                .reporter(reporter)
                .build();
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        Connector[] additionalConnectors = additionalConnector();
        if (additionalConnectors != null && additionalConnectors.length > 0) {
            tomcat.addAdditionalTomcatConnectors(additionalConnectors);
        }
        return tomcat;
    }

    private Connector[] additionalConnector() {
        List<Connector> result = new ArrayList<>();
        for (String port : new String[]{"8081"}) {
            Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            connector.setScheme("http");
            connector.setPort(Integer.valueOf(port));
            result.add(connector);
        }
        return result.toArray(new Connector[] {});
    }
}
