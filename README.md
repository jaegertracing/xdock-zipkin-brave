[![Build Status][ci-img]][ci]

# Crossdock Zipkin Brave

This is a [crossdock](https://github.com/uber/jaeger/tree/master/crossdock) app to test Zipkin data format support in Jaeger server. 
It uses Brave to create spans which are reported to the server.

## Build Docker image

Docker image is automatically built once commit is pushed to the master branch. To build it manually run:
```bash
docker build -t jaegertracing/xdock-zipkin-brave:latest .
```

## Build and test
```bash
./mvnw test
  
make crossdock
```

## License
  
[Apache 2.0 License](./LICENSE).


   [ci-img]: https://travis-ci.org/jaegertracing/xdock-zipkin-brave.svg?branch=master
   [ci]: https://travis-ci.org/jaegertracing/xdock-zipkin-brave
