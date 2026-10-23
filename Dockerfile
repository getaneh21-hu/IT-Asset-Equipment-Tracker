
# Stage 1: Build the application with Maven and Java 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run the application using a lightweight Java 21 runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/it_asset_equipment_tracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
