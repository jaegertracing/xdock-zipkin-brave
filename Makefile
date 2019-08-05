
XDOCK_YAML=docker-compose.yml
JAEGER_COMPOSE_URL=https://raw.githubusercontent.com/jaegertracing/jaeger/master/docker-compose
XDOCK_JAEGER_YAML=jaeger-docker-compose.yml

BRAVE_SERVICES=zipkin-brave-json zipkin-brave-thrift zipkin-brave-proto

.PHONY: crossdock
crossdock: crossdock-download-jaeger
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) kill $(BRAVE_SERVICES)
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) rm $(BRAVE_SERVICES)
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) build $(BRAVE_SERVICES)
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) run crossdock

.PHONY: run-crossdock
run-crossdock:
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) run crossdock

.PHONY: crossdock-download-jaeger
crossdock-download-jaeger:
	curl -O $(JAEGER_COMPOSE_URL)/$(XDOCK_JAEGER_YAML)

.PHONY: crossdock-clean
crossdock-clean: crossdock-download-jaeger
	docker-compose -f $(XDOCK_JAEGER_YAML) -f $(XDOCK_YAML) down --rmi local

