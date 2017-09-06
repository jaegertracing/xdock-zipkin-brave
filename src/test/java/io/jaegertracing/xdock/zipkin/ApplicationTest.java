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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Pavol Loffay
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class,
        properties = {"zipkin.encoding = JSON"})
public class ApplicationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testListensOnPort8081() {
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
    }
}
