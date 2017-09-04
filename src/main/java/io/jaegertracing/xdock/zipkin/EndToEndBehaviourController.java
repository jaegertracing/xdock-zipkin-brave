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
