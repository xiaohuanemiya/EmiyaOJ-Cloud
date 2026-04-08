FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY chat-service/target/chat-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 9050
ENTRYPOINT ["java", "-jar", "app.jar"]
