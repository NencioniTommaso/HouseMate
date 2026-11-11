FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copies your entire repository (including the shared module) into the container
COPY . .

# Builds the backend and its dependencies (like 'shared')
RUN mvn clean package -pl backend -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copies ONLY the compiled .jar file from the build stage
COPY --from=build /app/backend/target/*.jar app.jar

EXPOSE 8080

# Starts the application
ENTRYPOINT ["java", "-jar", "app.jar"]
