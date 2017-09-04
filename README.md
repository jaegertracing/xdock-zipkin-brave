# Crossdock Zipkin Brave

This is a [crossdock](https://github.com/uber/jaeger/tree/master/crossdock) app to test Zipkin data format support in Jaeger server. 
It uses Brave to create spans which are reported to the server.

## Build Docker image
Docker image is automatically build once commit is pushed to master.
```bash
docker build -t pavolloffay/xdock-zipkin-brave:latest .
docker push pavolloffay/xdock-zipkin-brave:latest
```
