# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
LABEL name="web-image-backend"
WORKDIR /app
COPY web-image-backend/pom.xml .
COPY web-image-backend/src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

