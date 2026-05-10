FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -U -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -U package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/notification-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
