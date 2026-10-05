# Build context: project root (see docker-compose.yml)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY access-sdk access-sdk
COPY service-product service-product
COPY service-operator service-operator
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B -pl service-operator -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/service-operator/target/service-operator-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
