package io.jaegertracing.xdock.zipkin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pavol Loffay
 */
@RestController
public class HealthController {

    @RequestMapping(value = "/", method = RequestMethod.HEAD)
    public String hello() {
        return "I'm fine!";
    }
}
