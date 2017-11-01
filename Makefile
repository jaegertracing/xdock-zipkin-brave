
XDOCK_YAML=docker-compose.yml
JAEGER_COMPOSE_URL=https://raw.githubusercontent.com/jaegertracing/jaeger/master/docker-compose
XDOCK_JAEGER_YAML=jaeger-docker-compose.yml

.PHONY: crossdock
crossdock: crossdock-download-jaeger
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) kill zipkin-brave-json zipkin-brave-thrift
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) rm zipkin-brave-json zipkin-brave-thrift
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) build zipkin-brave-json zipkin-brave-thrift
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) run crossdock

.PHONY: crossdock-download-jaeger
crossdock-download-jaeger:
	curl -O $(JAEGER_COMPOSE_URL)/$(XDOCK_JAEGER_YAML)

.PHONY: crossdock-clean
crossdock-clean: crossdock-download-jaeger
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) down --rmi local

