package io.jaegertracing.xdock.zipkin;

import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.connector.Connector;
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
 * @author Pavol Loffay
 */
@SpringBootApplication
public class Main {
    private static final String SERVICE_NAME = "crossdock-zipkin-brave";
    private static final String ENCODING_ENV_VAR = "ENCODING";

    public static void main(String []args) {
        SpringApplication.run(Main.class, args);
    }

    public static class Configuration {
        private final Encoding encoding;

        public Configuration() {
            encoding = Encoding.valueOf(System.getenv(ENCODING_ENV_VAR));
        }

        public Encoding getEncoding() {
            return encoding;
        }

        public String getServiceName() {
            return String.format("%s-%s", SERVICE_NAME, encoding);
        }
    }

    @Bean
    public Configuration configuration() {
        return new Configuration();
    }

    @Bean
    public AsyncReporter<Span> reporter(Configuration configuration) {
        Sender sender = OkHttpSender.builder()
                .endpoint("http://test_driver:9411/api/v1/spans")
                .encoding(configuration.getEncoding())
                .build();
        return AsyncReporter.builder(sender).build();
    }

    @Bean
    public Tracing tracer(Reporter<Span> reporter, Configuration configuration) {
        return Tracing.newBuilder()
                .localServiceName(configuration.getServiceName())
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
