# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .

COPY api-gateway/pom.xml api-gateway/
COPY core-finance/pom.xml core-finance/
COPY auth-service/pom.xml auth-service/
COPY common-proto/pom.xml common-proto/
COPY notification-service/pom.xml notification-service/

COPY common-proto/src common-proto/src
RUN mvn install -pl common-proto -am -DskipTests

COPY auth-service/src auth-service/src
RUN mvn clean package -pl auth-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
