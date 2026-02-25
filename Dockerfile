# Backend build stage
FROM maven:3.9-eclipse-temurin-21 AS backend-build
LABEL name="web-image-backend"
WORKDIR /app
COPY web-image-backend/pom.xml .
COPY web-image-backend/src ./src
RUN mvn clean package -DskipTests

# Frontend build stage
FROM node:20 AS frontend-build
WORKDIR /app
COPY web-image-frontend/package.json web-image-frontend/package-lock.json ./
RUN npm install
COPY web-image-frontend/ ./
RUN npm run build

# Backend run stage
FROM eclipse-temurin:21-jre AS backend
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Frontend run stage
FROM nginxinc/nginx-unprivileged:stable-alpine AS frontend
COPY --from=frontend-build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN chmod go+r /etc/nginx/conf.d/default.conf
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]

