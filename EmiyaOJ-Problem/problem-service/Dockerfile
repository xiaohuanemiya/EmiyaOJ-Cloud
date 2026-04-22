FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="EmiyaOJ"

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 9020

ENTRYPOINT ["java", "-jar", "app.jar"]
