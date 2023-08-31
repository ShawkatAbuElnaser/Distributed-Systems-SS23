##
## Build stage
##
FROM maven:3.8.1-openjdk-17-slim AS build
LABEL stage build
RUN mkdir -p /app
COPY . /app/
WORKDIR /app
RUN mvn package

##
## Run stage
##
FROM openjdk:17.0-jdk-slim
EXPOSE 6543/udp
RUN mkdir -p /app
COPY --from=build /app/target/stock-market-1.0-SNAPSHOT.jar /app
CMD [ "java", "-jar", "/app/stock-market-1.0-SNAPSHOT.jar" ]