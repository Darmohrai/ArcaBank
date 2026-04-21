# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .

COPY config-server/pom.xml config-server/
COPY auth-service/pom.xml auth-service/
COPY core-finance/pom.xml core-finance/
COPY api-gateway/pom.xml api-gateway/
COPY common-proto/pom.xml common-proto/
COPY notification-service/pom.xml notification-service/

RUN mvn install -pl common-proto,common-lib -am -DskipTests || true

COPY config-server/src config-server/src
RUN mvn clean package -pl config-server -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/config-server/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
