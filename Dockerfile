
# Java 21 runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/it_asset_equipment_tracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
