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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import io.jaegertracing.xdock.zipkin.Application.ZipkinTracing;
import io.jaegertracing.xdock.zipkin.ApplicationTest.TestConfig;
import java.io.IOException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import zipkin.junit.ZipkinRule;
import zipkin2.Span;
import zipkin2.codec.Encoding;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * @author Pavol Loffay
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class,
        properties = {"zipkin.encoding = JSON"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ApplicationTest {

    @Configuration
    @Import(Application.class)
    static class TestConfig {
        @Bean
        public ZipkinRule zipkinRule() throws IOException {
            ZipkinRule zipkinRule = new ZipkinRule();
            zipkinRule.start(0);
            return zipkinRule;
        }

        @Bean
        public ZipkinTracing zipkinTracing(ZipkinRule zipkinRule) {
            return Application.zipkin2Tracing(zipkinRule.httpUrl() + "/api/v1/spans", "foo", SpanBytesEncoder.JSON_V1);
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    public ZipkinRule zipkin;

    @Test
    public void testListensOnPort8081() {
        new ZipkinRule().httpUrl();
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:8081",
                HttpMethod.HEAD, null, String.class);
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    public void testHealthEndpoint() {
        ResponseEntity<String> responseEntity = restTemplate.exchange("/", HttpMethod.HEAD, null, String.class);
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    public void testPostCreateTraceEndpoint() {
        String body = "{\"type\": \"foo\", \"operation\": \"bar\", \"count\": 1, \"tags\": {}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/create_traces", new HttpEntity<>(body, headers), String.class);
        assertEquals(200, responseEntity.getStatusCodeValue());

        await().until(() -> zipkin.getTraces().size() == 1);
        assertEquals(1, zipkin.getTraces().size());
        assertEquals(1, zipkin.getTraces().get(0).size());
        assertEquals("bar", zipkin.getTraces().get(0).get(0).name);
    }
}
