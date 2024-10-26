# ./backend/authentication_server/Dockerfile
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar email-server.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "email-server.jar"]
