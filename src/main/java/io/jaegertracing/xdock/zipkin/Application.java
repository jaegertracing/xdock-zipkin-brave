/**
 * Copyright 2017-2019 The Jaeger Authors
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

import brave.Tracing;
import brave.sampler.Sampler;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * Application used in Jaeger crossdock test to verify that server can consume Zipkin data format.
 *
 * @author Pavol Loffay
 */
@SpringBootApplication
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final String SERVICE_NAME_PREFIX = "crossdock-zipkin-brave";

    public enum Encoding {
        JSON,
        THRIFT,
        PROTO
    }

    public interface ZipkinTracing {
        void flush();
        Tracing tracing();
    }

    public static void main(String []args) {
        SpringApplication.run(Application.class, args);
    }

    public String getServiceName() {
          String name = String.format("%s-%s", SERVICE_NAME_PREFIX, encoding).toLowerCase();
          if (spanBytesEncoder == SpanBytesEncoder.JSON_V2) {
              name += "-v2";
          }
          return name;
    }

    @Value("${zipkin.encoding}")
    private Encoding encoding;

    @Value("${zipkin.json.encoder:JSON_V1}")
    private SpanBytesEncoder spanBytesEncoder;

    @Value("${zipkin.url:http://jaeger-collector:9411}")
    private String zipkinUrl;

    @Bean
    public ZipkinTracing tracer() {
        if (spanBytesEncoder == SpanBytesEncoder.JSON_V2 || spanBytesEncoder == SpanBytesEncoder.PROTO3) {
            zipkinUrl += "/api/v2/spans";
        } else if (spanBytesEncoder == SpanBytesEncoder.JSON_V1) {
            zipkinUrl += "/api/v1/spans";
        }

        log.info("Zipkin URL = {}, Encoding = {}, JSON version = {}", zipkinUrl, encoding, spanBytesEncoder);

        /**
         * We have to split here because new artifacts zipkin2 does not support thrift encoding
         */
        if (encoding == Encoding.JSON) {
            return zipkin2Tracing(zipkinUrl, getServiceName(), spanBytesEncoder);
        } else if (encoding == Encoding.THRIFT) {
            return zipkinThriftTracing(zipkinUrl, getServiceName());
        } else if (encoding == Encoding.PROTO) {
            return zipkinProtoTracing(zipkinUrl, getServiceName());
        } else {
            throw new IllegalStateException("zipkin.encoding should be specified!");
        }
    }

    public static ZipkinTracing zipkinThriftTracing(String zipkinUrl, String serviceName) {
        Sender sender = OkHttpSender.newBuilder()
                .endpoint(zipkinUrl)
                .encoding(zipkin2.codec.Encoding.THRIFT)
                .build();
        AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
        Tracing tracing = Tracing.newBuilder()
            .localServiceName(serviceName)
            .sampler(Sampler.ALWAYS_SAMPLE)
            .traceId128Bit(true)
            .spanReporter(reporter)
            .build();
        return new ZipkinTracing() {
            @Override
            public void flush() {
                reporter.flush();
            }

            @Override
            public Tracing tracing() {
                return tracing;
            }
        };
    }

    public static ZipkinTracing zipkin2Tracing(String zipkinUrl, String serviceName, SpanBytesEncoder spanBytesEncoder) {
        Sender sender = OkHttpSender.newBuilder()
            .endpoint(zipkinUrl)
            .encoding(zipkin2.codec.Encoding.JSON)
            .build();
        AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build(spanBytesEncoder);
        Tracing tracing = Tracing.newBuilder()
            .localServiceName(serviceName)
            .sampler(Sampler.ALWAYS_SAMPLE)
            .traceId128Bit(true)
            .spanReporter(reporter)
            .build();
        return new ZipkinTracing() {
            @Override
            public void flush() {
                reporter.flush();
            }
            @Override
            public Tracing tracing() {
                return tracing;
            }
        };
    }

     public static ZipkinTracing zipkinProtoTracing(String zipkinUrl, String serviceName) {
         Sender sender = OkHttpSender.newBuilder()
                 .endpoint(zipkinUrl)
                 .encoding(zipkin2.codec.Encoding.PROTO3)
                 .build();
         AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
         Tracing tracing = Tracing.newBuilder()
                 .localServiceName(serviceName)
                 .sampler(Sampler.ALWAYS_SAMPLE)
                 .traceId128Bit(true)
                 .spanReporter(reporter)
                 .build();
         return new ZipkinTracing() {
             @Override
             public void flush() {
                 reporter.flush();
             }
             @Override
             public Tracing tracing() {
                 return tracing;
             }
         };
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
