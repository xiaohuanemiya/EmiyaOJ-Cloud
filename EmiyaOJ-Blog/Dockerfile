FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY blog-service/target/blog-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 9040
ENTRYPOINT ["java", "-jar", "app.jar"]
