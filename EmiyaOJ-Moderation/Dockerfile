FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY moderation-service/target/moderation-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 9060
ENTRYPOINT ["java", "-jar", "app.jar"]
