
XDOCK_YAML=docker-compose.yml

.PHONY: crossdock-fresh
crossdock-fresh:
	docker-compose -f $(XDOCK_YAML) down --rmi all
	docker-compose -f $(XDOCK_YAML) run crossdock

.PHONY: crossdock
crossdock:
	docker-compose -f $(XDOCK_YAML) kill zipkin-brave-json zipkin-brave-thrift
	docker-compose -f $(XDOCK_YAML) rm -f zipkin-brave-json zipkin-brave-thrift
	docker-compose -f $(XDOCK_YAML) build zipkin-brave-json zipkin-brave-thrift
	docker-compose -f $(XDOCK_YAML) run crossdock
