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

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uber.jaeger.crossdock.api.CreateTracesRequest;

import brave.Span;
import brave.Tracing;
import zipkin.reporter.AsyncReporter;

/**
 * @author Pavol Loffay
 */
@RestController
public class EndToEndBehaviourController {
    private static final Log log = LogFactory.getLog(EndToEndBehaviourController.class);

    @Autowired
    private Tracing tracing;

    @Autowired
    private AsyncReporter<zipkin.Span> reporter;

    @RequestMapping("create_traces")
    public ResponseEntity<?> createTraces(@RequestBody CreateTracesRequest request) {
        log.info("request " + request.toString());

        for (int i = 0; i < request.getCount(); i++) {
            Span span = tracing.tracer().newTrace().name(request.getOperation());
            Optional.of(request.getTags())
                    .ifPresent(stringStringMap -> stringStringMap.forEach((key, value) -> span.tag(key, value)));
            span.start();
            span.finish();
        }

        reporter.flush();
        return ResponseEntity.ok().build();
    }
}
