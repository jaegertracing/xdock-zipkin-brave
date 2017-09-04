FROM openjdk:alpine

MAINTAINER Pavol Loffay <ploffay@redhat.com>

ENV APP_HOME /app/

COPY pom.xml $APP_HOME
COPY src $APP_HOME/src
COPY .mvn $APP_HOME/.mvn
COPY mvnw $APP_HOME

WORKDIR $APP_HOME
RUN ./mvnw package

EXPOSE 8080
EXPOSE 8081

CMD ["java", "-jar", "target/jaegertracing-xdock-brave-0.0.1-SNAPSHOT.jar"]
